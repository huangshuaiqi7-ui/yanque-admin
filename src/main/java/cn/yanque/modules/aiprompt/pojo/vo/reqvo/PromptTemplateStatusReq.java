package cn.yanque.modules.aiprompt.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptTemplateStatusReq {
    /** 启用状态，只允许 ACTIVE 或 INACTIVE。 */
    @NotBlank(message = "状态不能为空")
    @Size(max = 20, message = "状态长度不能超过20个字符")
    private String status;
}
