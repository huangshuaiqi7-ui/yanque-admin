package cn.yanque.modules.users.pojo.vo.reqvo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleAssignReq {
    @NotNull(message = "角色ID集合不能为空")
    private List<Long> roleIds;
}
