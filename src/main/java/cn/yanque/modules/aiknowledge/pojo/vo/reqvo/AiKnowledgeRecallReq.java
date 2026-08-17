package cn.yanque.modules.aiknowledge.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiKnowledgeRecallReq {
    @NotBlank(message = "召回问题不能为空")
    private String query;

    private String mode = "HYBRID";

    @Min(value = 1, message = "topK不能小于1")
    @Max(value = 20, message = "topK不能大于20")
    private Integer topK = 5;
}
