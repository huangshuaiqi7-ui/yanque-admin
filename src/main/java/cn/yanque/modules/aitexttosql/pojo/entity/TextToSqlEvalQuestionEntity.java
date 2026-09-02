package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Text-to-SQL 评测样本。
 */
@Data
public class TextToSqlEvalQuestionEntity {
    private Long id;
    private String question;
    private String businessDomain;
    private String evalTarget;
    private String sampleCategory;
    private String sourceType;
    private Long sourceRunId;
    private String judgeNote;
    private String remark;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 列表页展示用，数据库里不单独存。
     */
    private Integer assertionCount;
}
