package cn.yanque.modules.aitexttosql.service;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlEvalAssertionMapper;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlEvalQuestionMapper;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlRunMapper;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalQuestionEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRunEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalAssertionReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalQuestionPageReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalQuestionSaveReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlEvalQuestionRes;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Text-to-SQL 评测样本服务。
 */
@Service
public class TextToSqlEvalQuestionService {
    private static final String SOURCE_MANUAL = "MANUAL";
    private static final String SOURCE_RUN_HISTORY = "RUN_HISTORY";
    private static final String SOURCE_FEEDBACK = "FEEDBACK";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String TARGET_END_TO_END = "END_TO_END";
    private static final String CATEGORY_NORMAL = "NORMAL";

    private static final Set<String> SUPPORT_TARGETS = Set.of(
            TARGET_END_TO_END, "INTENT_RECOGNITION", "TABLE_SELECTION", "SQL_GENERATION", "CLARIFICATION", "RESULT_SUMMARY"
    );
    private static final Set<String> SUPPORT_CATEGORIES = Set.of(CATEGORY_NORMAL, "BOUNDARY", "REGRESSION", "AMBIGUOUS", "NEGATIVE");
    private static final Set<String> SUPPORT_STATUS = Set.of(STATUS_DRAFT, "ACTIVE", "DISABLED");
    private static final Set<String> SUPPORT_OPERATORS = Set.of("EQ", "CONTAINS", "NOT_CONTAINS", "EXISTS", "NOT_EMPTY", "REGEX", "SEMANTIC");

    private final TextToSqlEvalQuestionMapper questionMapper;
    private final TextToSqlEvalAssertionMapper assertionMapper;
    private final TextToSqlRunMapper runMapper;

    public TextToSqlEvalQuestionService(TextToSqlEvalQuestionMapper questionMapper,
                                        TextToSqlEvalAssertionMapper assertionMapper,
                                        TextToSqlRunMapper runMapper) {
        this.questionMapper = questionMapper;
        this.assertionMapper = assertionMapper;
        this.runMapper = runMapper;
    }

    public PageResult<TextToSqlEvalQuestionRes> page(TextToSqlEvalQuestionPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<TextToSqlEvalQuestionEntity> rows = questionMapper.selectPage(
                StrUtil.trim(req.getKeyword()),
                StrUtil.trim(req.getBusinessDomain()),
                StrUtil.trim(req.getEvalTarget()),
                StrUtil.trim(req.getSampleCategory()),
                StrUtil.trim(req.getSourceType()),
                StrUtil.trim(req.getStatus()));
        PageInfo<TextToSqlEvalQuestionEntity> info = new PageInfo<>(rows);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(),
                rows.stream().map(this::toResWithoutAssertions).toList());
    }

    public TextToSqlEvalQuestionRes detail(Long id) {
        TextToSqlEvalQuestionEntity entity = questionMapper.selectById(id);
        if (entity == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return toRes(entity, assertionMapper.selectByEvalQuestionId(id));
    }

    @Transactional
    public Long create(TextToSqlEvalQuestionSaveReq req, Long createdBy) {
        TextToSqlEvalQuestionEntity entity = new TextToSqlEvalQuestionEntity();
        entity.setQuestion(requireText(req.getQuestion(), "评测问题不能为空"));
        entity.setBusinessDomain(blankToNull(req.getBusinessDomain()));
        entity.setEvalTarget(normalizeTarget(req.getEvalTarget()));
        entity.setSampleCategory(normalizeCategory(req.getSampleCategory()));
        entity.setSourceType(SOURCE_MANUAL);
        entity.setJudgeNote(blankToNull(req.getJudgeNote()));
        entity.setRemark(blankToNull(req.getRemark()));
        entity.setStatus(normalizeStatus(req.getStatus()));
        entity.setCreatedBy(createdBy);
        questionMapper.insert(entity);
        replaceAssertions(entity.getId(), req.getAssertions());
        return entity.getId();
    }

    @Transactional
    public void update(Long id, TextToSqlEvalQuestionSaveReq req) {
        detail(id);
        TextToSqlEvalQuestionEntity entity = new TextToSqlEvalQuestionEntity();
        entity.setId(id);
        entity.setQuestion(requireText(req.getQuestion(), "评测问题不能为空"));
        entity.setBusinessDomain(blankToNull(req.getBusinessDomain()));
        entity.setEvalTarget(normalizeTarget(req.getEvalTarget()));
        entity.setSampleCategory(normalizeCategory(req.getSampleCategory()));
        entity.setJudgeNote(blankToNull(req.getJudgeNote()));
        entity.setRemark(blankToNull(req.getRemark()));
        entity.setStatus(normalizeStatus(req.getStatus()));
        questionMapper.updateById(entity);
        replaceAssertions(id, req.getAssertions());
    }

    @Transactional
    public Long createFromRun(Long runId, Long createdBy) {
        TextToSqlEvalQuestionEntity existed = questionMapper.selectBySourceRunId(runId);
        if (existed != null) {
            return existed.getId();
        }

        TextToSqlRunEntity run = runMapper.selectById(runId);
        if (run == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        JSONObject state = parseObject(run.getStateSnapshotJson());

        TextToSqlEvalQuestionEntity entity = new TextToSqlEvalQuestionEntity();
        entity.setQuestion(firstText(valueByPath(state, "request.question"), run.getOriginalQuestion()));
        entity.setBusinessDomain(textValue(valueByPath(state, "intent_result.business_domain")));
        entity.setEvalTarget(TARGET_END_TO_END);
        entity.setSampleCategory(StrUtil.isBlank(run.getFeedbackResult()) ? CATEGORY_NORMAL : "REGRESSION");
        entity.setSourceType(StrUtil.isBlank(run.getFeedbackResult()) ? SOURCE_RUN_HISTORY : SOURCE_FEEDBACK);
        entity.setSourceRunId(runId);
        entity.setJudgeNote(blankToNull(run.getFeedbackComment()));
        entity.setStatus(STATUS_DRAFT);
        entity.setCreatedBy(createdBy);
        questionMapper.insert(entity);
        replaceAssertions(entity.getId(), buildAssertionsFromState(state));
        return entity.getId();
    }

    private TextToSqlEvalQuestionRes toResWithoutAssertions(TextToSqlEvalQuestionEntity entity) {
        return toRes(entity, null);
    }

    private TextToSqlEvalQuestionRes toRes(TextToSqlEvalQuestionEntity entity, List<TextToSqlEvalAssertionEntity> assertions) {
        TextToSqlEvalQuestionRes res = new TextToSqlEvalQuestionRes();
        res.setId(entity.getId());
        res.setQuestion(entity.getQuestion());
        res.setBusinessDomain(entity.getBusinessDomain());
        res.setEvalTarget(entity.getEvalTarget());
        res.setSampleCategory(entity.getSampleCategory());
        res.setSourceType(entity.getSourceType());
        res.setSourceRunId(entity.getSourceRunId());
        res.setJudgeNote(entity.getJudgeNote());
        res.setRemark(entity.getRemark());
        res.setStatus(entity.getStatus());
        res.setCreatedBy(entity.getCreatedBy());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        res.setAssertionCount(entity.getAssertionCount());
        res.setAssertions(assertions == null ? List.of() : assertions);
        return res;
    }

    private void replaceAssertions(Long evalQuestionId, List<TextToSqlEvalAssertionReq> reqList) {
        assertionMapper.deleteByEvalQuestionId(evalQuestionId);
        List<TextToSqlEvalAssertionEntity> assertions = buildAssertions(evalQuestionId, reqList);
        if (!assertions.isEmpty()) {
            assertionMapper.insertBatch(assertions);
        }
    }

    private List<TextToSqlEvalAssertionEntity> buildAssertions(Long evalQuestionId, List<TextToSqlEvalAssertionReq> reqList) {
        if (reqList == null || reqList.isEmpty()) {
            return List.of();
        }
        List<TextToSqlEvalAssertionEntity> result = new ArrayList<>();
        for (int i = 0; i < reqList.size(); i++) {
            TextToSqlEvalAssertionReq req = reqList.get(i);
            String operator = normalizeOperator(req.getOperator());
            String actualKey = requireText(req.getActualKey(), "State取值路径不能为空");
            String expectedValue = blankToNull(req.getExpectedValue());
            if (needsExpectedValue(operator) && expectedValue == null) {
                throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "客观断言必须填写期望值");
            }

            TextToSqlEvalAssertionEntity entity = new TextToSqlEvalAssertionEntity();
            entity.setEvalQuestionId(evalQuestionId);
            entity.setActualKey(actualKey);
            entity.setOperator(operator);
            entity.setExpectedValue("SEMANTIC".equals(operator) ? null : expectedValue);
            entity.setRequired(req.getRequired() == null || req.getRequired());
            entity.setWeight(req.getWeight() == null ? BigDecimal.ONE : req.getWeight());
            entity.setFailureType(blankToNull(req.getFailureType()));
            entity.setReferenceAnswer(blankToNull(req.getReferenceAnswer()));
            entity.setKeyPoints(blankToNull(req.getKeyPoints()));
            entity.setForbiddenPoints(blankToNull(req.getForbiddenPoints()));
            entity.setMinScore(req.getMinScore());
            entity.setSortOrder(i + 1);
            entity.setRemark(blankToNull(req.getRemark()));
            result.add(entity);
        }
        return result;
    }

    private List<TextToSqlEvalAssertionReq> buildAssertionsFromState(JSONObject state) {
        List<TextToSqlEvalAssertionReq> result = new ArrayList<>();
        addAssertion(result, "intent_result.business_domain", "EQ", textValue(valueByPath(state, "intent_result.business_domain")), "INTENT_ERROR");
        addArrayAssertions(result, "selected_tables", valueByPath(state, "selected_tables"), "TABLE_SELECTION_ERROR");
        if (StrUtil.isNotBlank(textValue(valueByPath(state, "sql_generation_result.action")))) {
            addAssertion(result, "sql_generation_result.action", "EQ", "SQL_READY", "SQL_GENERATION_ERROR");
        }
        if (StrUtil.isNotBlank(textValue(valueByPath(state, "executed_sql")))) {
            addAssertion(result, "executed_sql", "NOT_EMPTY", null, "SQL_GENERATION_ERROR");
        }
        if (StrUtil.isNotBlank(textValue(valueByPath(state, "answer")))) {
            addAssertion(result, "answer", "SEMANTIC", null, "ANSWER_QUALITY_ERROR");
        }
        return result;
    }

    private void addArrayAssertions(List<TextToSqlEvalAssertionReq> result, String actualKey, Object value, String failureType) {
        if (value instanceof JSONArray array) {
            for (Object item : array) {
                addAssertion(result, actualKey, "CONTAINS", textValue(item), failureType);
            }
            return;
        }
        addAssertion(result, actualKey, "CONTAINS", textValue(value), failureType);
    }

    private void addAssertion(List<TextToSqlEvalAssertionReq> result, String actualKey, String operator, String expectedValue, String failureType) {
        if (!"NOT_EMPTY".equals(operator) && !"SEMANTIC".equals(operator) && StrUtil.isBlank(expectedValue)) {
            return;
        }
        TextToSqlEvalAssertionReq req = new TextToSqlEvalAssertionReq();
        req.setActualKey(actualKey);
        req.setOperator(operator);
        req.setExpectedValue(expectedValue);
        req.setFailureType(failureType);
        req.setRequired(true);
        result.add(req);
    }

    private JSONObject parseObject(String text) {
        if (StrUtil.isBlank(text) || "null".equals(text)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(text);
        } catch (JSONException exception) {
            return new JSONObject();
        }
    }

    private Object valueByPath(JSONObject state, String path) {
        Object current = state;
        for (String key : path.split("\\.")) {
            if (!(current instanceof JSONObject object)) {
                return null;
            }
            current = object.get(key);
        }
        return current;
    }

    private String normalizeTarget(String value) {
        String target = StrUtil.blankToDefault(StrUtil.trim(value), TARGET_END_TO_END);
        if (!SUPPORT_TARGETS.contains(target)) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "评测目标不支持");
        }
        return target;
    }

    private String normalizeCategory(String value) {
        String category = StrUtil.blankToDefault(StrUtil.trim(value), CATEGORY_NORMAL);
        if (!SUPPORT_CATEGORIES.contains(category)) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "样本场景不支持");
        }
        return category;
    }

    private String normalizeStatus(String value) {
        String status = StrUtil.blankToDefault(StrUtil.trim(value), STATUS_DRAFT);
        if (!SUPPORT_STATUS.contains(status)) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "样本状态不支持");
        }
        return status;
    }

    private String normalizeOperator(String value) {
        String operator = StrUtil.blankToDefault(StrUtil.trim(value), "EQ");
        if (!SUPPORT_OPERATORS.contains(operator)) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "断言方式不支持");
        }
        return operator;
    }

    private boolean needsExpectedValue(String operator) {
        return !"EXISTS".equals(operator) && !"NOT_EMPTY".equals(operator) && !"SEMANTIC".equals(operator);
    }

    private String requireText(String value, String message) {
        String text = blankToNull(value);
        if (text == null) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, message);
        }
        return text;
    }

    private String firstText(Object first, String fallback) {
        String firstValue = textValue(first);
        return StrUtil.isBlank(firstValue) ? requireText(fallback, "运行记录缺少问题，不能生成样本") : firstValue;
    }

    private String textValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String blankToNull(String value) {
        String text = StrUtil.trim(value);
        return StrUtil.isBlank(text) ? null : text;
    }
}
