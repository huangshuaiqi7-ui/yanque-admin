package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Text-to-SQL 评测结果查询条件。
 */
@Data
public class TextToSqlEvalResultPageReq {
    private Boolean passed;
    private String resultStatus;

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(1000)
    private Integer pageSize = 10;
}
