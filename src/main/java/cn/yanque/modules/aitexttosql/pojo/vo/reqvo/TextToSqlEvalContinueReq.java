package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 继续执行中断的评测样本。
 */
@Data
public class TextToSqlEvalContinueReq {
    /**
     * 用户对澄清问题的补充回答。
     */
    @NotBlank(message = "请输入澄清回答")
    private String clarificationAnswer;
}
