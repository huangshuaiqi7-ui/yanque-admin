package cn.yanque.modules.permissions.pojo.vo.resvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PermissionTreeRes {
    private Long id;
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private String apiPath;
    private Integer sortNum;
    private String status;
    private Boolean assigned;
    private List<PermissionTreeRes> children = new ArrayList<>();
}
