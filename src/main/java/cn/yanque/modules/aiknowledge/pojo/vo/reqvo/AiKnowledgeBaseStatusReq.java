package cn.yanque.modules.aiknowledge.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiKnowledgeBaseStatusReq {
    @NotBlank(message = "知识库状态不能为空")
    private String status;
}
