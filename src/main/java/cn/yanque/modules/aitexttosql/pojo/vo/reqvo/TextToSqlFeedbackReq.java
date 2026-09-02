package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Text-to-SQL 运行结果反馈。
 */
@Data
public class TextToSqlFeedbackReq {
    @NotBlank(message = "反馈结果不能为空")
    private String feedbackResult;

    @Size(max = 1000, message = "反馈说明不能超过1000字")
    private String feedbackComment;
}
