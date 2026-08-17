package cn.yanque.modules.aiknowledge.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiKnowledgeQaReq {
    @NotBlank(message = "问答问题不能为空")
    @Size(max = 1000, message = "问答问题不能超过1000个字符")
    private String question;

    private String recallMode = "HYBRID";

    @Min(value = 1, message = "topK不能小于1")
    @Max(value = 20, message = "topK不能大于20")
    private Integer topK = 5;
}
