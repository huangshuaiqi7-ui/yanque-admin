package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色 Text-to-SQL 字段授权。
 */
@Data
public class TextToSqlRolePermissionEntity {
    private Long id;
    private Long roleId;
    private String roleCode;
    private String businessDomain;
    private String tableName;
    private String columnName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
