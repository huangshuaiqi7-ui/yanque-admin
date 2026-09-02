package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Text-to-SQL 单条断言评测结果。
 */
@Data
public class TextToSqlEvalAssertionResultEntity {
    private Long id;
    private Long evalResultId;
    private Long evalAssertionId;
    private String actualKey;
    private String operator;
    private String expectedValue;
    private String actualValue;
    private Boolean required;
    private Boolean passed;
    private BigDecimal score;
    private String failureType;
    private String reason;
    private LocalDateTime createdAt;
}
