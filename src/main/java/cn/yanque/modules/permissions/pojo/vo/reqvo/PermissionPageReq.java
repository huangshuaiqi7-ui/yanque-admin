package cn.yanque.modules.permissions.pojo.vo.reqvo;

import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.enums.PermissionTypeEnum;
import cn.yanque.commons.validation.EnumValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PermissionPageReq {
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 1000, message = "每页条数不能超过1000")
    private Integer pageSize = 10;
    private String keyword;
    @Min(value = 0, message = "父权限ID不能小于0")
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    @EnumValue(enumClass = PermissionTypeEnum.class, message = "权限类型只能是API、MENU或BUTTON")
    private String permissionType;
    @EnumValue(enumClass = CommonStatusEnum.class, message = "状态只能是ACTIVE或INACTIVE")
    private String status;
}
