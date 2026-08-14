package cn.yanque.modules.aiknowledge.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiKnowledgeBaseCreateReq {
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 100, message = "知识库名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "知识库编码不能为空")
    @Size(max = 64, message = "知识库编码长度不能超过64个字符")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "知识库编码只能包含小写字母、数字和下划线")
    private String code;

    @Size(max = 500, message = "知识库说明长度不能超过500个字符")
    private String description;
}
