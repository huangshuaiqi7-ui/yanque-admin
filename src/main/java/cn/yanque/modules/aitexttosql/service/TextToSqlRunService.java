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

    public void saveFailure(String conversationId, String errorMessage, Long durationMs) {
        TextToSqlRunEntity entity = new TextToSqlRunEntity();
        entity.setConversationId(conversationId);
        entity.setStatus("FAILED");
        entity.setErrorMessage(errorMessage);
        entity.setDurationMs(durationMs);
        mapper.updateFailure(entity);
    }

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

    public TextToSqlRunEntity detail(Long id) {
        TextToSqlRunEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return entity;
    }

    private String resolveStatus(JSONObject response) {
        if (StrUtil.isNotBlank(firstString(response, "errorMessage", "error_message"))) {
            return "FAILED";
        }
        if (firstBoolean(response, "needClarification", "need_clarification")) {
            return "WAITING_CLARIFICATION";
        }
        return "COMPLETED";
    }

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
