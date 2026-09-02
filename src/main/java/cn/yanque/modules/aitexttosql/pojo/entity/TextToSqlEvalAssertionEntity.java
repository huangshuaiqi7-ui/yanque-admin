package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Text-to-SQL 评测断言。
 *
 * 一条断言就是一条判断标准。
 * 评测任务重跑样本后，会从本次运行的 State 中取 actualKey 对应的值，
 * 再根据 operator、expectedValue 或语义判断配置计算是否通过。
 */
@Data
public class TextToSqlEvalAssertionEntity {
    /**
     * 断言 ID。
     */
    private Long id;

    /**
     * 所属评测样本 ID。
     *
     * 对应 ai_text_to_sql_eval_question.id。
     */
    private Long evalQuestionId;

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
     * EQ：实际值等于 expectedValue。
     * CONTAINS：实际数组或文本包含 expectedValue。
     * NOT_CONTAINS：实际数组或文本不包含 expectedValue。
     * EXISTS：字段存在。
     * NOT_EMPTY：字段非空。
     * REGEX：实际文本匹配 expectedValue 正则。
     * SEMANTIC：调用大模型做语义判断。
     */
    private String operator;

    /**
     * 客观断言的期望值。
     *
     * EQ、CONTAINS、NOT_CONTAINS、REGEX 使用这个字段。
     * EXISTS、NOT_EMPTY、SEMANTIC 不使用这个字段。
     */
    private String expectedValue;

    /**
     * 是否必过。
     *
     * true：这条断言失败后，样本最终失败。
     * false：只记录这条断言的结果，不影响样本最终通过。
     */
    private Boolean required;

    /**
     * 权重。
     *
     * 当前样本分数按断言得分平均计算，权重先保留给后续评分策略扩展。
     */
    private BigDecimal weight;

    /**
     * 失败归因。
     *
     * 用于研发排查和统计，例如：
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
     * operator = SEMANTIC 时传给大模型裁判。
     */
    private String referenceAnswer;

    /**
     * 语义判断必须覆盖的关键点。
     *
     * operator = SEMANTIC 时使用，多个要点按行保存。
     */
    private String keyPoints;

    /**
     * 语义判断不能出现的内容。
     *
     * operator = SEMANTIC 时使用，多个禁止点按行保存。
     */
    private String forbiddenPoints;

    /**
     * 语义判断最低通过分。
     *
     * operator = SEMANTIC 时使用，例如 80 表示至少 80 分才通过。
     */
    private Integer minScore;

    /**
     * 排序值。
     *
     * 同一个样本下按这个字段展示断言顺序。
     */
    private Integer sortOrder;

    /**
     * 备注。
     *
     * 给维护人员补充说明，不参与自动判断。
     */
    private String remark;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
