package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Text-to-SQL 运行记录查询条件。
 */
@Data
public class TextToSqlRunPageReq {
    private String keyword;
    private String conversationId;
    private String sourceType;
    private String status;
    private String feedbackResult;

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(1000)
    private Integer pageSize = 10;
}
