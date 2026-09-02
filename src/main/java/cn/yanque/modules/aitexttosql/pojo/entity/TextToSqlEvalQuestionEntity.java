package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Text-to-SQL 评测样本。
 *
 * 一条样本描述一个需要反复回归的问题。
 * 样本本身只保存问题、分类和来源运行记录 ID；
 * 具体怎么判断通过，放在 ai_text_to_sql_eval_assertion 表里。
 */
@Data
public class TextToSqlEvalQuestionEntity {
    /**
     * 样本 ID。
     */
    private Long id;

    /**
     * 评测问题。
     *
     * 评测任务会拿这个问题重新跑一遍 Text-to-SQL。
     */
    private String question;

    /**
     * 业务环境。
     *
     * 例如 order、student、teaching。
     * 创建评测任务时可以按这个字段筛选样本。
     */
    private String businessDomain;

    /**
     * 评测目标。
     *
     * 例如 END_TO_END、INTENT_RECOGNITION、TABLE_SELECTION、SQL_GENERATION。
     * 用来说明这条样本主要想测哪一段能力。
     */
    private String evalTarget;

    /**
     * 样本场景。
     *
     * 例如 NORMAL、BOUNDARY、REGRESSION、AMBIGUOUS、NEGATIVE。
     */
    private String sampleCategory;

    /**
     * 样本来源。
     *
     * MANUAL：人工录入。
     * RUN_HISTORY：从运行记录加入。
     * FEEDBACK：从带反馈的运行记录沉淀。
     */
    private String sourceType;

    /**
     * 来源运行记录 ID。
     *
     * 只保存 ID，不复制运行记录里的 State 和节点历史。
     * 需要查看来源详情时，实时查询 ai_text_to_sql_run。
     */
    private Long sourceRunId;

    /**
     * 人工判断说明。
     *
     * 用来记录这条样本重点判断什么，或当时反馈里指出的问题。
     */
    private String judgeNote;

    /**
     * 备注。
     *
     * 给样本维护人员补充说明，不参与自动评测。
     */
    private String remark;

    /**
     * 样本状态。
     *
     * DRAFT：草稿，不参与评测。
     * ACTIVE：正式样本，可以被评测任务选择。
     * DISABLED：停用，不参与评测。
     */
    private String status;

    /**
     * 创建人用户 ID。
     */
    private Long createdBy;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 断言数量。
     *
     * 列表页展示用，由查询 SQL 统计出来，数据库里不单独存。
     */
    private Integer assertionCount;
}
