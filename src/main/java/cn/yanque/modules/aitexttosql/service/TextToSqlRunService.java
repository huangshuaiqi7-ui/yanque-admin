package cn.yanque.modules.aitexttosql.service;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlRunMapper;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRunEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlFeedbackReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlRunPageReq;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Text-to-SQL 运行记录服务。
 */
@Service
public class TextToSqlRunService {
    private static final String FEEDBACK_GOOD = "GOOD";
    private static final String FEEDBACK_BAD = "BAD";

    private final TextToSqlRunMapper mapper;

    public TextToSqlRunService(TextToSqlRunMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 创建一条运行记录，并标记为 RUNNING。
     *
     * sourceType 用来区分来源：用户手动分析是 USER，评测任务重跑是 EVAL。
     */
    public Long createRunning(String conversationId, String question, Long userId, String sourceType) {
        TextToSqlRunEntity entity = new TextToSqlRunEntity();
        entity.setConversationId(conversationId);
        entity.setSourceType(StrUtil.blankToDefault(sourceType, "USER"));
        entity.setOriginalQuestion(question);
        entity.setStatus("RUNNING");
        entity.setCreatedBy(userId);
        mapper.insert(entity);
        return entity.getId();
    }

    /**
     * 保存 Python Text-to-SQL 返回的最终结果。
     *
     * stateSnapshot/stateHistory 是运行记录详情和评测断言的核心数据：
     * stateSnapshot 保存最后一次完整 State，stateHistory 保存 LangGraph 每一步节点写入。
     */
    public void saveResult(String conversationId, JSONObject response, Long durationMs) {
        JSONObject stateSnapshot = objectOf(response, "stateSnapshot", "state_snapshot");
        Object stateHistory = first(response, "stateHistory", "state_history");

        TextToSqlRunEntity entity = new TextToSqlRunEntity();
        entity.setConversationId(conversationId);
        entity.setErrorMessage(firstString(response, "errorMessage", "error_message"));
        entity.setStateSnapshotJson(jsonString(stateSnapshot == null ? response : stateSnapshot));
        entity.setStateHistoryJson(jsonString(stateHistory));
        entity.setDurationMs(durationMs);
        entity.setStatus(resolveStatus(response));
        mapper.updateResult(entity);
    }

    /**
     * Python 调用异常时保存失败状态。
     */
    public void saveFailure(String conversationId, String errorMessage, Long durationMs) {
        TextToSqlRunEntity entity = new TextToSqlRunEntity();
        entity.setConversationId(conversationId);
        entity.setStatus("FAILED");
        entity.setErrorMessage(errorMessage);
        entity.setDurationMs(durationMs);
        mapper.updateFailure(entity);
    }

    /**
     * 分页查询运行记录。
     *
     * 运行记录页会用到关键词、会话、来源、状态和反馈结果筛选。
     */
    public PageResult<TextToSqlRunEntity> page(TextToSqlRunPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<TextToSqlRunEntity> rows = mapper.selectPage(
                StrUtil.trim(req.getKeyword()),
                StrUtil.trim(req.getConversationId()),
                StrUtil.trim(req.getSourceType()),
                StrUtil.trim(req.getStatus()),
                StrUtil.trim(req.getFeedbackResult()));
        PageInfo<TextToSqlRunEntity> info = new PageInfo<>(rows);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(), rows);
    }

    /**
     * 保存用户对本次结果的最新反馈。
     *
     * v1 只保留最新一次反馈；BAD 必须写说明，方便后面整理评测样本。
     */
    public TextToSqlRunEntity saveFeedback(Long runId, TextToSqlFeedbackReq req) {
        detail(runId);

        String feedbackResult = StrUtil.trim(req.getFeedbackResult());
        String feedbackComment = StrUtil.trim(req.getFeedbackComment());
        if (!FEEDBACK_GOOD.equals(feedbackResult) && !FEEDBACK_BAD.equals(feedbackResult)) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "反馈结果只能是GOOD或BAD");
        }
        if (FEEDBACK_BAD.equals(feedbackResult) && StrUtil.isBlank(feedbackComment)) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "结果有问题时请填写反馈说明");
        }

        mapper.updateFeedbackById(runId, feedbackResult, FEEDBACK_BAD.equals(feedbackResult) ? feedbackComment : null);
        return detail(runId);
    }

    /**
     * 查询单条运行记录，不存在时抛业务异常。
     */
    public TextToSqlRunEntity detail(Long id) {
        TextToSqlRunEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return entity;
    }

    /**
     * 根据 Python 响应推导运行状态。
     *
     * 有 errorMessage 表示失败；需要澄清时不是完成，而是等待用户补充。
     */
    private String resolveStatus(JSONObject response) {
        if (StrUtil.isNotBlank(firstString(response, "errorMessage", "error_message"))) {
            return "FAILED";
        }
        if (firstBoolean(response, "needClarification", "need_clarification")) {
            return "WAITING_CLARIFICATION";
        }
        return "COMPLETED";
    }

    /**
     * 从同一个 JSON 中兼容读取 camelCase 和 snake_case 字段。
     */
    private Object first(JSONObject obj, String... keys) {
        if (obj == null) {
            return null;
        }
        for (String key : keys) {
            Object value = obj.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstString(JSONObject obj, String... keys) {
        Object value = first(obj, keys);
        return value == null ? null : String.valueOf(value);
    }

    private Boolean firstBoolean(JSONObject obj, String... keys) {
        Object value = first(obj, keys);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value == null ? false : Boolean.parseBoolean(String.valueOf(value));
    }

    /**
     * 从 JSON 里读取对象字段。
     *
     * 如果字段已经是字符串形式 JSON，也会解析成 JSONObject。
     */
    private JSONObject objectOf(JSONObject obj, String... keys) {
        Object value = first(obj, keys);
        if (value instanceof JSONObject jsonObject) {
            return jsonObject;
        }
        if (value instanceof String text && StrUtil.isNotBlank(text)) {
            return JSON.parseObject(text);
        }
        return null;
    }

    private String jsonString(Object value) {
        if (value == null) {
            return "null";
        }
        return JSON.toJSONString(value);
    }
}
