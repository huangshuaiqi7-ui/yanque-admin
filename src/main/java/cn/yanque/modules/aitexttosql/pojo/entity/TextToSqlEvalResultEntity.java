package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Text-to-SQL 单条样本评测结果。
 */
@Data
public class TextToSqlEvalResultEntity {
    private Long id;
    private Long evalTaskId;
    private Long evalQuestionId;
    private Long runRecordId;
    private String question;
    private String resultStatus;
    private Boolean passed;
    private BigDecimal score;
    private String stateSnapshotJson;
    private String stateHistoryJson;
    private String errorMessage;
    private Long durationMs;
    private LocalDateTime createdAt;
}
