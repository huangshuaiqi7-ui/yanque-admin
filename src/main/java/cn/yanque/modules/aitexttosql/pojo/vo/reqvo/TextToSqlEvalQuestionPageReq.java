package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Text-to-SQL 评测样本查询条件。
 */
@Data
public class TextToSqlEvalQuestionPageReq {
    private String keyword;
    private String businessDomain;
    private String evalTarget;
    private String sampleCategory;
    private String sourceType;
    private String status;

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(1000)
    private Integer pageSize = 10;
}
