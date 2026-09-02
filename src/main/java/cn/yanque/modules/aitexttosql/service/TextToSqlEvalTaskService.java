package cn.yanque.modules.aitexttosql.service;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlEvalAssertionMapper;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlEvalAssertionResultMapper;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlEvalQuestionMapper;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlEvalResultMapper;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlEvalTaskMapper;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlRunMapper;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionResultEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalQuestionEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalResultEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalTaskEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRunEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlAnalyzeReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalContinueReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalResultPageReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalTaskCreateReq;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlEvalTaskPageReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlEvalResultRes;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlEvalTaskDetailRes;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Text-to-SQL 评测任务服务。
 */
@Service
public class TextToSqlEvalTaskService {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String RESULT_PASSED = "PASSED";
    private static final String RESULT_FAILED = "FAILED";
    private static final String RESULT_INTERRUPTED = "INTERRUPTED";
    private static final BigDecimal SCORE_PASS = new BigDecimal("100.00");
    private static final BigDecimal SCORE_FAIL = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final TextToSqlEvalTaskMapper taskMapper;
    private final TextToSqlEvalQuestionMapper questionMapper;
    private final TextToSqlEvalAssertionMapper assertionMapper;
    private final TextToSqlEvalResultMapper resultMapper;
    private final TextToSqlEvalAssertionResultMapper assertionResultMapper;
    private final TextToSqlRunMapper runMapper;
    private final TextToSqlRunService runService;
    private final TextToSqlPythonClient pythonClient;
    private final SysRoleMapper roleMapper;
    private final Executor evalExecutor;

    public TextToSqlEvalTaskService(TextToSqlEvalTaskMapper taskMapper,
                                    TextToSqlEvalQuestionMapper questionMapper,
                                    TextToSqlEvalAssertionMapper assertionMapper,
                                    TextToSqlEvalResultMapper resultMapper,
                                    TextToSqlEvalAssertionResultMapper assertionResultMapper,
                                    TextToSqlRunMapper runMapper,
                                    TextToSqlRunService runService,
                                    TextToSqlPythonClient pythonClient,
                                    SysRoleMapper roleMapper,
                                    @Qualifier("aiTextToSqlEvalExecutor") Executor evalExecutor) {
        this.taskMapper = taskMapper;
        this.questionMapper = questionMapper;
        this.assertionMapper = assertionMapper;
        this.resultMapper = resultMapper;
        this.assertionResultMapper = assertionResultMapper;
        this.runMapper = runMapper;
        this.runService = runService;
        this.pythonClient = pythonClient;
        this.roleMapper = roleMapper;
        this.evalExecutor = evalExecutor;
    }

    /**
     * 创建评测任务并立即异步执行。
     *
     * 前端勾选了样本时只跑勾选样本；没有勾选时，按业务环境、评测目标、样本场景筛选 ACTIVE 样本。
     */
    public Long create(TextToSqlEvalTaskCreateReq req, Long userId) {
        List<TextToSqlEvalQuestionEntity> questions = selectedQuestions(req.getEvalQuestionIds(), req.getBusinessDomain(), req.getEvalTarget(), req.getSampleCategory());
        if (questions.isEmpty()) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "没有可评测样本");
        }

        TextToSqlEvalTaskEntity task = new TextToSqlEvalTaskEntity();
        task.setName(StrUtil.trim(req.getName()));
        task.setBusinessDomain(blankToNull(req.getBusinessDomain()));
        task.setEvalTarget(blankToNull(req.getEvalTarget()));
        task.setSampleCategory(blankToNull(req.getSampleCategory()));
        task.setStatus(STATUS_PENDING);
        task.setCreatedBy(userId);
        taskMapper.insert(task);
        evalExecutor.execute(() -> runTask(task.getId(), userId, questions.stream().map(TextToSqlEvalQuestionEntity::getId).toList()));
        return task.getId();
    }

    /**
     * 分页查询评测任务列表。
     */
    public PageResult<TextToSqlEvalTaskEntity> page(TextToSqlEvalTaskPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<TextToSqlEvalTaskEntity> rows = taskMapper.selectPage(
                StrUtil.trim(req.getKeyword()),
                StrUtil.trim(req.getBusinessDomain()),
                StrUtil.trim(req.getEvalTarget()),
                StrUtil.trim(req.getSampleCategory()),
                StrUtil.trim(req.getStatus()));
        PageInfo<TextToSqlEvalTaskEntity> info = new PageInfo<>(rows);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(), rows);
    }

    /**
     * 查询评测任务详情。
     *
     * resultCount 表示已经落库的样本结果数，前端可以用它观察后台任务执行进度。
     */
    public TextToSqlEvalTaskDetailRes detail(Long id) {
        TextToSqlEvalTaskEntity task = requireTask(id);
        TextToSqlEvalTaskDetailRes res = new TextToSqlEvalTaskDetailRes();
        res.setTask(task);
        res.setResultCount(resultMapper.countByTaskId(id));
        return res;
    }

    /**
     * 分页查询某个任务下的样本执行结果。
     *
     * 失败列表、结果详情抽屉、断言结果展示都从这里取数据。
     */
    public PageResult<TextToSqlEvalResultRes> results(Long taskId, TextToSqlEvalResultPageReq req) {
        requireTask(taskId);
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<TextToSqlEvalResultEntity> rows = resultMapper.selectByTaskId(taskId, req.getPassed(), StrUtil.trim(req.getResultStatus()));
        PageInfo<TextToSqlEvalResultEntity> info = new PageInfo<>(rows);
        return new PageResult<>(info.getTotal(), info.getPageNum(), info.getPageSize(),
                rows.stream().map(this::toResultRes).toList());
    }

    /**
     * 继续执行中断的评测结果。
     *
     * 这里复用原运行记录的 conversationId，把澄清回答传回 Python LangGraph，
     * 然后覆盖当前 eval_result 和断言结果，不新增一条评测结果。
     */
    public TextToSqlEvalResultRes continueInterruptedResult(Long resultId, TextToSqlEvalContinueReq req, Long userId) {
        TextToSqlEvalResultEntity result = requireResult(resultId);
        if (!RESULT_INTERRUPTED.equals(result.getResultStatus())) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "只有中断的评测结果才能继续执行");
        }

        TextToSqlRunEntity run = requireRun(result.getRunRecordId());
        TextToSqlEvalQuestionEntity question = requireQuestion(result.getEvalQuestionId());
        long start = System.currentTimeMillis();
        JSONObject response = null;
        String errorMessage = null;
        Long durationMs;

        try {
            TextToSqlAnalyzeReq analyzeReq = new TextToSqlAnalyzeReq();
            analyzeReq.setQuestion(question.getQuestion());
            analyzeReq.setConversationId(run.getConversationId());
            analyzeReq.setClarificationAnswer(StrUtil.trim(req.getClarificationAnswer()));
            response = pythonClient.analyze(analyzeReq, userId, roleMapper.selectRoleCodesByUserId(userId));
            durationMs = System.currentTimeMillis() - start;
            runService.saveResult(run.getConversationId(), response, durationMs);
            run = runMapper.selectById(run.getId());
        } catch (RuntimeException exception) {
            durationMs = System.currentTimeMillis() - start;
            errorMessage = exception.getMessage();
            runService.saveFailure(run.getConversationId(), errorMessage, durationMs);
            run = runMapper.selectById(run.getId());
        }

        JSONObject state = parseObject(run == null ? null : run.getStateSnapshotJson());
        boolean interrupted = isInterrupted(run, response);
        List<TextToSqlEvalAssertionResultEntity> assertionResults = checkAssertions(
                assertionMapper.selectByEvalQuestionId(question.getId()), question.getQuestion(), state, response, errorMessage);
        boolean passed = errorMessage == null && !assertionResults.isEmpty() && assertionResults.stream()
                .filter(item -> Boolean.TRUE.equals(item.getRequired()))
                .allMatch(item -> Boolean.TRUE.equals(item.getPassed()));

        result.setRunRecordId(run == null ? result.getRunRecordId() : run.getId());
        result.setResultStatus(resolveResultStatus(passed, interrupted));
        result.setPassed(passed);
        result.setScore(averageScore(assertionResults));
        result.setStateSnapshotJson(run == null ? jsonString(response) : run.getStateSnapshotJson());
        result.setStateHistoryJson(run == null ? null : run.getStateHistoryJson());
        result.setErrorMessage(errorMessage == null && interrupted ? interruptedMessage(response) : errorMessage);
        result.setDurationMs(durationMs);
        resultMapper.updateById(result);

        assertionResultMapper.deleteByEvalResultId(result.getId());
        assertionResults.forEach(item -> item.setEvalResultId(result.getId()));
        if (!assertionResults.isEmpty()) {
            assertionResultMapper.insertBatch(assertionResults);
        }
        refreshTaskCounts(result.getEvalTaskId());
        return toResultRes(resultMapper.selectById(result.getId()));
    }

    /**
     * 后台执行评测任务。
     *
     * 任务维度只记录汇总统计；每条样本的详细运行结果由 runQuestion 单独落库。
     */
    private void runTask(Long taskId, Long userId, List<Long> questionIds) {
        long taskStart = System.currentTimeMillis();
        TextToSqlEvalTaskEntity task = taskMapper.selectById(taskId);
        if (task == null) {
            return;
        }
        List<TextToSqlEvalQuestionEntity> questions = selectedQuestions(questionIds, task.getBusinessDomain(), task.getEvalTarget(), task.getSampleCategory());
        taskMapper.updateRunning(taskId, questions.size());

        int passCount = 0;
        int failCount = 0;
        int interruptedCount = 0;
        try {
            for (TextToSqlEvalQuestionEntity question : questions) {
                TextToSqlEvalResultEntity result = runQuestion(taskId, question, userId);
                if (Boolean.TRUE.equals(result.getPassed())) {
                    passCount++;
                } else if (RESULT_INTERRUPTED.equals(result.getResultStatus())) {
                    interruptedCount++;
                } else {
                    failCount++;
                }
            }
            finishTask(taskId, STATUS_COMPLETED, passCount, failCount, interruptedCount, taskStart, null);
        } catch (RuntimeException exception) {
            finishTask(taskId, STATUS_FAILED, passCount, failCount, interruptedCount, taskStart, exception.getMessage());
        }
    }

    /**
     * 执行单条评测样本。
     *
     * 每条样本都会重新调用 Python Text-to-SQL，并生成一条 sourceType=EVAL 的运行记录。
     */
    private TextToSqlEvalResultEntity runQuestion(Long taskId, TextToSqlEvalQuestionEntity question, Long userId) {
        long start = System.currentTimeMillis();
        String conversationId = "eval-" + taskId + "-" + question.getId() + "-" + UUID.randomUUID();
        Long runId = runService.createRunning(conversationId, question.getQuestion(), userId, "EVAL");
        JSONObject response = null;
        TextToSqlRunEntity run = null;
        String errorMessage = null;
        Long durationMs;

        try {
            TextToSqlAnalyzeReq req = new TextToSqlAnalyzeReq();
            req.setQuestion(question.getQuestion());
            req.setConversationId(conversationId);
            response = pythonClient.analyze(req, userId, roleMapper.selectRoleCodesByUserId(userId));
            durationMs = System.currentTimeMillis() - start;
            runService.saveResult(conversationId, response, durationMs);
            run = runMapper.selectById(runId);
        } catch (RuntimeException exception) {
            durationMs = System.currentTimeMillis() - start;
            errorMessage = exception.getMessage();
            runService.saveFailure(conversationId, errorMessage, durationMs);
            run = runMapper.selectById(runId);
        }

        // 断言只看本次重跑产生的数据；如果 Python 异常，下面会把所有断言标记为失败。
        JSONObject state = parseObject(run == null ? null : run.getStateSnapshotJson());
        boolean interrupted = isInterrupted(run, response);
        List<TextToSqlEvalAssertionResultEntity> assertionResults = checkAssertions(
                assertionMapper.selectByEvalQuestionId(question.getId()), question.getQuestion(), state, response, errorMessage);
        // required=true 的断言全部通过，样本才算通过；非必过断言只记录，不影响最终 passed。
        boolean passed = errorMessage == null && !assertionResults.isEmpty() && assertionResults.stream()
                .filter(item -> Boolean.TRUE.equals(item.getRequired()))
                .allMatch(item -> Boolean.TRUE.equals(item.getPassed()));
        BigDecimal score = averageScore(assertionResults);

        TextToSqlEvalResultEntity result = new TextToSqlEvalResultEntity();
        result.setEvalTaskId(taskId);
        result.setEvalQuestionId(question.getId());
        result.setRunRecordId(runId);
        result.setQuestion(question.getQuestion());
        result.setResultStatus(resolveResultStatus(passed, interrupted));
        result.setPassed(passed);
        result.setScore(score);
        result.setStateSnapshotJson(run == null ? jsonString(response) : run.getStateSnapshotJson());
        result.setStateHistoryJson(run == null ? null : run.getStateHistoryJson());
        result.setErrorMessage(errorMessage == null && interrupted ? interruptedMessage(response) : errorMessage);
        result.setDurationMs(durationMs);
        resultMapper.insert(result);

        assertionResults.forEach(item -> item.setEvalResultId(result.getId()));
        if (!assertionResults.isEmpty()) {
            assertionResultMapper.insertBatch(assertionResults);
        }
        return result;
    }

    /**
     * 执行一组断言并生成断言结果。
     *
     * actualKey 会先从 State 快照取值；如果取不到，再从 Python 总响应里兜底取值。
     */
    private List<TextToSqlEvalAssertionResultEntity> checkAssertions(List<TextToSqlEvalAssertionEntity> assertions,
                                                                     String question,
                                                                     JSONObject state,
                                                                     JSONObject response,
                                                                     String errorMessage) {
        List<TextToSqlEvalAssertionResultEntity> results = new ArrayList<>();
        for (TextToSqlEvalAssertionEntity assertion : assertions) {
            Object actual = valueByPath(state, assertion.getActualKey());
            if (actual == null && response != null) {
                actual = valueByPath(response, assertion.getActualKey());
            }
            AssertionCheck check = errorMessage == null
                    ? checkAssertion(assertion, question, actual)
                    : new AssertionCheck(false, SCORE_FAIL, "Text-to-SQL运行失败：" + errorMessage);

            TextToSqlEvalAssertionResultEntity result = new TextToSqlEvalAssertionResultEntity();
            result.setEvalAssertionId(assertion.getId());
            result.setActualKey(assertion.getActualKey());
            result.setOperator(assertion.getOperator());
            result.setExpectedValue(assertion.getExpectedValue());
            result.setActualValue(jsonString(actual));
            result.setRequired(assertion.getRequired());
            result.setPassed(check.passed());
            result.setScore(check.score());
            result.setFailureType(assertion.getFailureType());
            result.setReason(check.reason());
            results.add(result);
        }
        return results;
    }

    /**
     * 按断言方式检查实际值。
     *
     * 客观断言在 Java 本地判断；SEMANTIC 调用 YanQue-AI 的大模型裁判接口。
     */
    private AssertionCheck checkAssertion(TextToSqlEvalAssertionEntity assertion, String question, Object actual) {
        String operator = assertion.getOperator();
        String expected = StrUtil.nullToEmpty(assertion.getExpectedValue());
        String actualText = textValue(actual);
        return switch (operator) {
            case "EQ" -> pass(actualText.equals(expected), "实际值等于期望值", "实际值不等于期望值");
            case "CONTAINS" -> pass(contains(actual, expected), "实际值包含期望值", "实际值不包含期望值");
            case "NOT_CONTAINS" -> pass(!contains(actual, expected), "实际值未包含禁用值", "实际值包含了禁用值");
            case "EXISTS" -> pass(actual != null, "字段存在", "字段不存在");
            case "NOT_EMPTY" -> pass(StrUtil.isNotBlank(actualText), "字段非空", "字段为空");
            case "REGEX" -> regexCheck(actualText, expected);
            case "SEMANTIC" -> semanticCheck(assertion, question, actualText);
            default -> new AssertionCheck(false, SCORE_FAIL, "不支持的判断方式：" + operator);
        };
    }

    /**
     * 调用大模型做自然语言语义判断。
     *
     * 页面填写的参考答案、关键点、禁用点、最低分会原样传给 Python judge。
     */
    private AssertionCheck semanticCheck(TextToSqlEvalAssertionEntity assertion, String question, String actualText) {
        if (StrUtil.isBlank(actualText)) {
            return new AssertionCheck(false, SCORE_FAIL, "语义判断字段为空");
        }
        try {
            JSONObject judged = pythonClient.semanticJudge(
                    question,
                    actualText,
                    assertion.getReferenceAnswer(),
                    assertion.getKeyPoints(),
                    assertion.getForbiddenPoints(),
                    assertion.getMinScore());
            boolean passed = judged.getBooleanValue("passed");
            BigDecimal score = BigDecimal.valueOf(judged.getIntValue("score")).setScale(2, RoundingMode.HALF_UP);
            String reason = StrUtil.blankToDefault(judged.getString("reason"), passed ? "语义判断通过" : "语义判断未通过");
            return new AssertionCheck(passed, score, reason);
        } catch (RuntimeException exception) {
            return new AssertionCheck(false, SCORE_FAIL, "语义大模型判断失败：" + exception.getMessage());
        }
    }

    /**
     * 正则断言。
     *
     * 使用 DOTALL，让正则可以匹配多行文本，例如完整 SQL 或完整回答。
     */
    private AssertionCheck regexCheck(String actualText, String expected) {
        try {
            boolean matches = Pattern.compile(expected, Pattern.DOTALL).matcher(actualText).find();
            return pass(matches, "实际值匹配正则", "实际值不匹配正则");
        } catch (PatternSyntaxException exception) {
            return new AssertionCheck(false, SCORE_FAIL, "正则表达式不合法：" + exception.getMessage());
        }
    }

    /**
     * 把布尔检查结果转换成统一的断言结果。
     */
    private AssertionCheck pass(boolean passed, String passReason, String failReason) {
        return new AssertionCheck(passed, passed ? SCORE_PASS : SCORE_FAIL, passed ? passReason : failReason);
    }

    /**
     * CONTAINS/NOT_CONTAINS 的匹配逻辑。
     *
     * 数组字段按元素精确匹配；普通字段按字符串包含匹配。
     */
    private boolean contains(Object actual, String expected) {
        if (actual instanceof JSONArray array) {
            return array.stream().anyMatch(item -> textValue(item).equals(expected));
        }
        return textValue(actual).contains(expected);
    }

    /**
     * 样本分数取所有断言得分的平均值。
     *
     * required=false 的断言也参与评分，因为它仍然代表一个可观察质量项。
     */
    private BigDecimal averageScore(List<TextToSqlEvalAssertionResultEntity> results) {
        if (results.isEmpty()) {
            return SCORE_FAIL;
        }
        BigDecimal total = results.stream()
                .map(TextToSqlEvalAssertionResultEntity::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(results.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 汇总并结束评测任务。
     */
    /**
     * 判断本次 Text-to-SQL 是否停在澄清中断。
     *
     * 评测里中断不是普通失败：它可能是澄清类样本的预期结果，也可能是端到端样本需要单独排查的问题。
     */
    private boolean isInterrupted(TextToSqlRunEntity run, JSONObject response) {
        if (run != null && "WAITING_CLARIFICATION".equals(run.getStatus())) {
            return true;
        }
        return response != null && firstBoolean(response, "needClarification", "need_clarification");
    }

    /**
     * 根据断言结果和运行状态生成样本结果状态。
     */
    private String resolveResultStatus(boolean passed, boolean interrupted) {
        if (passed) {
            return RESULT_PASSED;
        }
        return interrupted ? RESULT_INTERRUPTED : RESULT_FAILED;
    }

    /**
     * 中断时记录澄清问题，方便结果列表直接看原因。
     */
    private String interruptedMessage(JSONObject response) {
        String question = firstString(response, "clarificationQuestion", "clarification_question");
        if (StrUtil.isBlank(question)) {
            return "Text-to-SQL进入澄清中断。";
        }
        return "Text-to-SQL进入澄清中断：" + question;
    }

    private void finishTask(Long taskId, String status, int passCount, int failCount, int interruptedCount, long start, String errorMessage) {
        int total = passCount + failCount + interruptedCount;
        BigDecimal passRate = total == 0
                ? SCORE_FAIL
                : BigDecimal.valueOf(passCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        taskMapper.updateFinished(taskId, status, passCount, failCount, interruptedCount, passRate, System.currentTimeMillis() - start, errorMessage);
    }

    /**
     * 按筛选条件查询所有 ACTIVE 样本。
     */
    private List<TextToSqlEvalQuestionEntity> activeQuestions(String businessDomain, String evalTarget, String sampleCategory) {
        return questionMapper.selectActiveForEval(StrUtil.trim(businessDomain), StrUtil.trim(evalTarget), StrUtil.trim(sampleCategory));
    }

    /**
     * 解析本次任务要跑哪些样本。
     *
     * ids 不为空代表用户手动勾选样本；否则使用筛选条件自动选择样本。
     */
    private List<TextToSqlEvalQuestionEntity> selectedQuestions(List<Long> ids, String businessDomain, String evalTarget, String sampleCategory) {
        if (ids != null && !ids.isEmpty()) {
            return questionMapper.selectActiveByIds(ids);
        }
        return activeQuestions(businessDomain, evalTarget, sampleCategory);
    }

    /**
     * 查询任务，不存在时抛业务异常。
     */
    private TextToSqlEvalTaskEntity requireTask(Long id) {
        TextToSqlEvalTaskEntity task = taskMapper.selectById(id);
        if (task == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return task;
    }

    /**
     * 查询单条评测结果，不存在时抛业务异常。
     */
    private TextToSqlEvalResultEntity requireResult(Long id) {
        TextToSqlEvalResultEntity result = resultMapper.selectById(id);
        if (result == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return result;
    }

    /**
     * 查询评测样本，不存在时抛业务异常。
     */
    private TextToSqlEvalQuestionEntity requireQuestion(Long id) {
        TextToSqlEvalQuestionEntity question = questionMapper.selectById(id);
        if (question == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return question;
    }

    /**
     * 查询运行记录，不存在时抛业务异常。
     */
    private TextToSqlRunEntity requireRun(Long id) {
        TextToSqlRunEntity run = id == null ? null : runMapper.selectById(id);
        if (run == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return run;
    }

    /**
     * 重新统计任务下所有结果，刷新任务汇总。
     */
    private void refreshTaskCounts(Long taskId) {
        List<TextToSqlEvalResultEntity> results = resultMapper.selectByTaskId(taskId, null, null);
        int passCount = 0;
        int failCount = 0;
        int interruptedCount = 0;
        for (TextToSqlEvalResultEntity item : results) {
            if (Boolean.TRUE.equals(item.getPassed())) {
                passCount++;
            } else if (RESULT_INTERRUPTED.equals(item.getResultStatus())) {
                interruptedCount++;
            } else {
                failCount++;
            }
        }
        int total = passCount + failCount + interruptedCount;
        BigDecimal passRate = total == 0
                ? SCORE_FAIL
                : BigDecimal.valueOf(passCount * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        taskMapper.updateCounts(taskId, passCount, failCount, interruptedCount, passRate);
    }

    /**
     * 组装前端结果详情。
     *
     * 单条样本结果会携带断言结果列表，方便前端展开查看失败原因。
     */
    private TextToSqlEvalResultRes toResultRes(TextToSqlEvalResultEntity entity) {
        TextToSqlEvalResultRes res = new TextToSqlEvalResultRes();
        res.setId(entity.getId());
        res.setEvalTaskId(entity.getEvalTaskId());
        res.setEvalQuestionId(entity.getEvalQuestionId());
        res.setRunRecordId(entity.getRunRecordId());
        res.setQuestion(entity.getQuestion());
        res.setResultStatus(entity.getResultStatus());
        res.setPassed(entity.getPassed());
        res.setScore(entity.getScore());
        res.setStateSnapshotJson(entity.getStateSnapshotJson());
        res.setStateHistoryJson(entity.getStateHistoryJson());
        res.setErrorMessage(entity.getErrorMessage());
        res.setDurationMs(entity.getDurationMs());
        res.setCreatedAt(entity.getCreatedAt());
        res.setAssertionResults(assertionResultMapper.selectByEvalResultId(entity.getId()));
        return res;
    }

    /**
     * 安全解析 State JSON。
     */
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

    /**
     * 从 JSON 对象中按点路径取值。
     */
    private Object valueByPath(Object root, String path) {
        Object current = root;
        for (String key : path.split("\\.")) {
            if (!(current instanceof JSONObject object)) {
                return null;
            }
            current = object.get(key);
        }
        return current;
    }

    /**
     * 从响应里兼容读取 camelCase 和 snake_case 布尔字段。
     */
    private boolean firstBoolean(JSONObject obj, String... keys) {
        Object value = first(obj, keys);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    /**
     * 从响应里兼容读取 camelCase 和 snake_case 文本字段。
     */
    private String firstString(JSONObject obj, String... keys) {
        Object value = first(obj, keys);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 按多个候选字段名读取第一个非空值。
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

    /**
     * 把任意实际值转成用于断言比较的文本。
     */
    private String textValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String text) {
            return text;
        }
        if (value instanceof JSONObject || value instanceof JSONArray) {
            return JSON.toJSONString(value);
        }
        return String.valueOf(value);
    }

    /**
     * 把任意实际值转成落库展示用的 JSON 字符串。
     */
    private String jsonString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text;
        }
        return JSON.toJSONString(value);
    }

    /**
     * 空字符串统一存 null，方便 SQL 筛选条件判断。
     */
    private String blankToNull(String value) {
        return StrUtil.blankToDefault(StrUtil.trim(value), null);
    }

    private record AssertionCheck(boolean passed, BigDecimal score, String reason) {
    }
}
