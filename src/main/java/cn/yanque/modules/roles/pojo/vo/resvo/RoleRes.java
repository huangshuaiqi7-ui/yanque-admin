package cn.yanque.modules.roles.pojo.vo.resvo;

import lombok.Data;

import java.util.Date;

@Data
public class RoleRes {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private String status;
    private String statusDesc;
    private Date createdAt;
    private Date updatedAt;
}
