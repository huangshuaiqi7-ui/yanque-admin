package cn.yanque.modules.roles.pojo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysRoleEntity {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
