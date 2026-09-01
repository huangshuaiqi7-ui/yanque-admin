package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Text-to-SQL 可配置表字段树。
 */
@Data
public class TextToSqlSchemaTreeRes {
    private String businessDomain;
    private String businessDomainName;
    private List<TableNode> tables = new ArrayList<>();

    @Data
    public static class TableNode {
        private Long id;
        private String businessDomain;
        private String tableName;
        private String tableComment;
        private String status;
        private List<ColumnNode> columns = new ArrayList<>();
    }

    @Data
    public static class ColumnNode {
        private Long id;
        private Long tableId;
        private String businessDomain;
        private String tableName;
        private String columnName;
        private String columnComment;
        private String status;
    }
}
