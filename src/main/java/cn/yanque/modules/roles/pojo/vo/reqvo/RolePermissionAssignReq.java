package cn.yanque.modules.roles.pojo.vo.reqvo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionAssignReq {
    @NotNull(message = "权限ID集合不能为空")
    private List<Long> permissionIds;
}
