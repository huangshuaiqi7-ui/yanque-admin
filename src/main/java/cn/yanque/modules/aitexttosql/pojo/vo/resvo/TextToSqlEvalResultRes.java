package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionResultEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Text-to-SQL 样本评测结果。
 */
@Data
public class TextToSqlEvalResultRes {
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
    private List<TextToSqlEvalAssertionResultEntity> assertionResults;
}
