package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Text-to-SQL 评测断言编辑项。
 */
@Data
public class TextToSqlEvalAssertionReq {
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
    private String remark;
}
