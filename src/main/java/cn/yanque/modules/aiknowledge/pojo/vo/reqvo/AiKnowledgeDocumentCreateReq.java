package cn.yanque.modules.aiknowledge.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiKnowledgeDocumentCreateReq {
    @NotBlank(message = "文档名称不能为空")
    @Size(max = 200, message = "文档名称长度不能超过200个字符")
    private String name;

    @NotBlank(message = "文档编码不能为空")
    @Size(max = 64, message = "文档编码长度不能超过64个字符")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "文档编码只能包含小写字母、数字和下划线")
    private String code;

    @NotBlank(message = "文档对象Key不能为空")
    @Size(max = 500, message = "文档对象Key长度不能超过500个字符")
    private String objectKey;

    private Long fileSize;
}
