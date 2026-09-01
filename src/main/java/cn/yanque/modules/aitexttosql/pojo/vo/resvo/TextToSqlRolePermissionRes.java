package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色 Text-to-SQL 数据权限详情。
 */
@Data
public class TextToSqlRolePermissionRes {
    private Long roleId;
    private List<ColumnGrant> grants = new ArrayList<>();

    @Data
    public static class ColumnGrant {
        private String businessDomain;
        private String tableName;
        private String columnName;
        private Boolean granted;
    }
}
