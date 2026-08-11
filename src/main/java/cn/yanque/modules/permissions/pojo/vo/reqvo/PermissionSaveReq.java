package cn.yanque.modules.permissions.pojo.vo.reqvo;

import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.enums.PermissionTypeEnum;
import cn.yanque.commons.validation.EnumValue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionSaveReq {
    @NotNull(message = "父权限ID不能为空")
    @Min(value = 0, message = "父权限ID不能小于0")
    private Long parentId = 0L;
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    private String permissionCode;
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    private String permissionName;
    @NotBlank(message = "权限类型不能为空")
    @EnumValue(enumClass = PermissionTypeEnum.class, message = "权限类型只能是API、MENU或BUTTON")
    private String permissionType;
    @Size(max = 255, message = "API路径长度不能超过255个字符")
    private String apiPath;
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortNum = 0;
    @Size(max = 255, message = "权限描述长度不能超过255个字符")
    private String description;
    @EnumValue(enumClass = CommonStatusEnum.class, message = "状态只能是ACTIVE或INACTIVE")
    private String status;
}
