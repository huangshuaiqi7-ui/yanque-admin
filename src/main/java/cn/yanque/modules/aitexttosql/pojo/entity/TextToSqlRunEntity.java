package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Text-to-SQL 单次运行记录。
 */
@Data
public class TextToSqlRunEntity {
    private Long id;
    private String conversationId;
    private String sourceType;
    private String originalQuestion;
    private String status;
    private String errorMessage;
    private String stateSnapshotJson;
    private String stateHistoryJson;
    private Long durationMs;
    private String feedbackResult;
    private String feedbackComment;
    private LocalDateTime feedbackAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
