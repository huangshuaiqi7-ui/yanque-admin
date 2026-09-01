package cn.yanque.modules.aitexttosql.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Text-to-SQL 可查询表目录。
 */
@Data
public class TextToSqlTableEntity {
    private Long id;
    private String businessDomain;
    private String businessDomainName;
    private String tableName;
    private String tableComment;
    private String status;
    private Integer sortNum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
