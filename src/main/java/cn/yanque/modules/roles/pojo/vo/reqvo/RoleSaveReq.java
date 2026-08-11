package cn.yanque.modules.roles.pojo.vo.reqvo;

import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.validation.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleSaveReq {
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码长度不能超过64个字符")
    private String roleCode;
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过64个字符")
    private String roleName;
    @Size(max = 255, message = "角色描述长度不能超过255个字符")
    private String description;
    @EnumValue(enumClass = CommonStatusEnum.class, message = "状态只能是ACTIVE或INACTIVE")
    private String status;
    private List<Long> permissionIds;
}
