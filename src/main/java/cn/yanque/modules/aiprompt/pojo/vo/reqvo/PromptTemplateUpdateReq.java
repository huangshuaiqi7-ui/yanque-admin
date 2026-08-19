package cn.yanque.modules.aiprompt.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PromptTemplateUpdateReq {
    /** 提示词名称，给管理端用户识别模板用途。 */
    @NotBlank(message = "提示词名称不能为空")
    @Size(max = 100, message = "提示词名称长度不能超过100个字符")
    private String name;

    /** 所属 Agent 编码，例如 student_chat_agent。 */
    @NotBlank(message = "Agent不能为空")
    @Size(max = 100, message = "Agent长度不能超过100个字符")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Agent只能包含小写字母、数字和下划线")
    private String agentCode;

    /** 提示词类型，只允许 SYSTEM 或 USER。 */
    @NotBlank(message = "提示词类型不能为空")
    @Size(max = 20, message = "提示词类型长度不能超过20个字符")
    private String promptType;

    /** 使用场景，可选值为 CHAT、RAG、SUMMARY、JUDGE、STRUCTURED_EXTRACT。 */
    @Size(max = 50, message = "场景编码长度不能超过50个字符")
    private String sceneCode;

    /** 提示词模板说明。 */
    @Size(max = 500, message = "说明长度不能超过500个字符")
    private String description;
}
