package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Text-to-SQL 评测断言编辑项。
 *
 * 一条评测样本可以配置多条断言。
 * 评测任务重跑样本后，会从本次运行的 State 中取 actualKey 对应的值，
 * 再按 operator 和 expectedValue 判断这条断言是否通过。
 */
@Data
public class TextToSqlEvalAssertionReq {
    /**
     * State 取值路径。
     *
     * 支持点路径，例如：
     * intent_result.business_domain
     * selected_tables
     * sql_generation_result.action
     * executed_sql
     * answer
     */
    private String actualKey;

    /**
     * 判断方式。
     *
     * 常用值：
     * EQ：实际值等于 expectedValue
     * CONTAINS：实际数组或文本包含 expectedValue
     * NOT_CONTAINS：实际数组或文本不包含 expectedValue
     * EXISTS：字段存在
     * NOT_EMPTY：字段非空
     * REGEX：实际文本匹配 expectedValue 正则
     * SEMANTIC：调用大模型做语义判断
     */
    private String operator;

    /**
     * 客观断言的期望值。
     *
     * EQ、CONTAINS、NOT_CONTAINS、REGEX 需要填写。
     * EXISTS、NOT_EMPTY、SEMANTIC 不需要填写。
     */
    private String expectedValue;

    /**
     * 是否必过。
     *
     * true：这条断言失败后，样本最终失败。
     * false：只记录断言结果，不影响样本最终通过。
     */
    private Boolean required;

    /**
     * 权重。
     *
     * 当前主要保留给后续评分扩展；现阶段样本分数按断言得分平均计算。
     */
    private BigDecimal weight;

    /**
     * 失败归因。
     *
     * 给研发排查和后续统计用，例如：
     * INTENT_ERROR
     * TABLE_SELECTION_ERROR
     * SQL_GENERATION_ERROR
     * CLARIFICATION_ERROR
     * ANSWER_QUALITY_ERROR
     * STATE_ASSERT_ERROR
     */
    private String failureType;

    /**
     * 语义判断参考答案。
     *
     * operator = SEMANTIC 时使用，提供给大模型作为判断参考。
     */
    private String referenceAnswer;

    /**
     * 语义判断必须覆盖的关键点。
     *
     * operator = SEMANTIC 时使用。
     * 多个要点按行填写。
     */
    private String keyPoints;

    /**
     * 语义判断不能出现的内容。
     *
     * operator = SEMANTIC 时使用。
     * 多个禁止点按行填写。
     */
    private String forbiddenPoints;

    /**
     * 语义判断最低通过分。
     *
     * operator = SEMANTIC 时使用；例如 80 表示大模型评分至少 80 分才通过。
     */
    private Integer minScore;

    /**
     * 备注。
     *
     * 给人工整理样本时补充说明，不参与自动判断。
     */
    private String remark;
}
