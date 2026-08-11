package cn.yanque.modules.permissions.pojo.vo.resvo;

import lombok.Data;

import java.util.Date;

@Data
public class PermissionRes {
    private Long id;
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private String apiPath;
    private Integer sortNum;
    private String description;
    private String status;
    private String statusDesc;
    private Date createdAt;
    private Date updatedAt;
}
