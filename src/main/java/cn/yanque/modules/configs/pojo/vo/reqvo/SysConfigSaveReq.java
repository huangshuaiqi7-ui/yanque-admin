package cn.yanque.modules.configs.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysConfigSaveReq {
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String k;

    @NotBlank(message = "配置值不能为空")
    @Size(max = 500, message = "配置值长度不能超过500个字符")
    private String v;
}
