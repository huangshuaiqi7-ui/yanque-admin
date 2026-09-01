package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Text-to-SQL 可查询字段目录。
 */
@Data
public class TextToSqlColumnEntity {
    private Long id;
    private Long tableId;
    private String businessDomain;
    private String businessDomainName;
    private String tableName;
    private String tableComment;
    private String columnName;
    private String columnComment;
    private String status;
    private Integer sortNum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
