package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Text-to-SQL 评测断言。
 */
@Data
public class TextToSqlEvalAssertionEntity {
    private Long id;
    private Long evalQuestionId;
    private String actualKey;
    private String operator;
    private String expectedValue;
    private Boolean required;
    private BigDecimal weight;
    private String failureType;
    private String referenceAnswer;
    private String keyPoints;
    private String forbiddenPoints;
    private Integer minScore;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
