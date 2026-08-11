package cn.yanque.modules.roles.pojo.vo.resvo;

import cn.yanque.modules.permissions.pojo.vo.resvo.PermissionTreeRes;
import lombok.Data;

import java.util.List;

@Data
public class RoleDetailRes extends RoleRes {
    private List<Long> permissionIds;
    private List<PermissionTreeRes> permissions;
}
