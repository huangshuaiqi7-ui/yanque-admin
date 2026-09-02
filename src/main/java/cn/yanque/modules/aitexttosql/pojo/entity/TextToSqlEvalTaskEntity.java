package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Text-to-SQL 评测任务。
 */
@Data
public class TextToSqlEvalTaskEntity {
    private Long id;
    private String name;
    private String businessDomain;
    private String evalTarget;
    private String sampleCategory;
    private String status;
    private Integer totalCount;
    private Integer passCount;
    private Integer failCount;
    private Integer interruptedCount;
    private BigDecimal passRate;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMs;
    private String errorMessage;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
