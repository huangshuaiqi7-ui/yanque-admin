package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Text-to-SQL 评测样本保存请求。
 */
@Data
public class TextToSqlEvalQuestionSaveReq {
    @NotBlank(message = "评测问题不能为空")
    private String question;

    private String businessDomain;
    private String evalTarget;
    private String sampleCategory;
    private String judgeNote;
    private String remark;
    private String status;
    private List<TextToSqlEvalAssertionReq> assertions;
}
