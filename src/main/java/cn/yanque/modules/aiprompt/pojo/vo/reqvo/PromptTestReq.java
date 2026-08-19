package cn.yanque.modules.aiprompt.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 提示词测试请求参数。
 */
@Data
public class PromptTestReq {
    /** 系统提示词模板ID。 */
    @NotNull(message = "系统提示词模板不能为空")
    private Long systemTemplateId;

    /** 系统提示词版本ID。 */
    @NotNull(message = "系统提示词版本不能为空")
    private Long systemVersionId;

    /** 用户提示词模板ID。 */
    @NotNull(message = "用户提示词模板不能为空")
    private Long userTemplateId;

    /** 用户提示词版本ID。 */
    @NotNull(message = "用户提示词版本不能为空")
    private Long userVersionId;

    /** 用户提示词变量。 */
    private Map<String, String> variables;

    /** 测试使用的模型。 */
    @NotBlank(message = "测试模型不能为空")
    private String model;
}
