package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存角色 Text-to-SQL 数据权限。
 */
@Data
public class TextToSqlRolePermissionSaveReq {
    @Valid
    private List<ColumnGrant> grants = new ArrayList<>();

    @Data
    public static class ColumnGrant {
        @NotBlank(message = "业务域不能为空")
        private String businessDomain;

        @NotBlank(message = "表名不能为空")
        private String tableName;

        @NotBlank(message = "字段名不能为空")
        private String columnName;

        @NotNull(message = "是否授权不能为空")
        private Boolean granted;
    }
}
