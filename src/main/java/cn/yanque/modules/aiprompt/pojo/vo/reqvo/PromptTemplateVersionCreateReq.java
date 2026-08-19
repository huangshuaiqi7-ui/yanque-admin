package cn.yanque.modules.aiprompt.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptTemplateVersionCreateReq {
    /** 提示词内容。 */
    @NotBlank(message = "提示词内容不能为空")
    private String content;

    /** 变量说明 JSON 字符串，允许为空。 */
    private String variables;

    /** 本次修改说明。 */
    @Size(max = 500, message = "变更说明长度不能超过500个字符")
    private String changeNote;
}
