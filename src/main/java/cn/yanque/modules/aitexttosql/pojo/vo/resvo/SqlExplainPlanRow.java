package cn.yanque.modules.aitexttosql.pojo.vo.resvo;

import lombok.Data;

/**
 * MySQL EXPLAIN 返回的一行执行计划。
 *
 * 一条 SQL 可能访问多张表，所以 EXPLAIN 通常会返回多行。
 */
@Data
public class SqlExplainPlanRow {
    /** SELECT 层级编号。简单 SQL 通常是 1。 */
    private String id;
    /** 查询类型，例如 SIMPLE、PRIMARY、SUBQUERY。 */
    private String selectType;
    /** 当前执行计划行对应的表。 */
    private String tableName;
    /** 访问类型。常见从好到差大致是 const、eq_ref、ref、range、index、ALL。 */
    private String accessType;
    /** 优化器认为可能用到的索引。 */
    private String possibleKeys;
    /** 实际选择使用的索引。为空时通常表示没有使用索引。 */
    private String usedKey;
    /** 使用的索引长度。 */
    private String keyLength;
    /** 和索引比较的列或常量。 */
    private String ref;
    /** MySQL 预估需要扫描的行数。 */
    private Long rows;
    /** MySQL 预估过滤后保留的百分比。 */
    private String filtered;
    /** 额外信息，例如 Using where、Using temporary、Using filesort。 */
    private String extra;
    /** 是否使用临时表。第一版只展示，不拦截。 */
    private boolean usingTemporary;
    /** 是否发生文件排序。第一版只展示，不拦截。 */
    private boolean usingFilesort;
}
