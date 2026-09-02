package cn.yanque.modules.aitexttosql.service;

import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlPermissionMapper;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlColumnEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRolePermissionEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlTableEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlRolePermissionSaveReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlRolePermissionRes;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlSchemaTreeRes;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import cn.yanque.modules.roles.pojo.entity.SysRoleEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理端 Text-to-SQL 数据权限配置服务。
 */
@Service
public class TextToSqlPermissionService {
    private final TextToSqlPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;

    public TextToSqlPermissionService(TextToSqlPermissionMapper permissionMapper, SysRoleMapper roleMapper) {
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * 查询 Text-to-SQL 权限配置页需要的表字段树。
     *
     * 数据来源分两层：
     * 1. information_schema.tables：读取当前数据库里的真实表；
     * 2. information_schema.columns：读取这些表下面的真实字段。
     *
     * 这里不再单独维护 ai_text_to_sql_table。
     * Text-to-SQL 最终能不能查某张表、某个字段，由角色字段授权和 DDL Resource 安全策略决定。
     *
     * 返回结构是：业务域 -> 表 -> 字段。
     * 前端左侧选角色后，中间和右侧就用这棵树来勾选字段权限。
     */
    public List<TextToSqlSchemaTreeRes> schemaTree() {
        List<TextToSqlTableEntity> tables = permissionMapper.selectActiveTables();
        List<TextToSqlColumnEntity> columns = permissionMapper.selectActiveColumns();

        // domainMap 用来按业务域分组，例如 order、student、teaching。
        Map<String, TextToSqlSchemaTreeRes> domainMap = new LinkedHashMap<>();
        // tableMap 用来快速把 information_schema 读出来的字段挂回对应表。
        Map<Long, TextToSqlSchemaTreeRes.TableNode> tableMap = new LinkedHashMap<>();

        // 先创建业务域和表节点。这里不处理字段，避免表字段混在一起看不清。
        for (TextToSqlTableEntity table : tables) {
            TextToSqlSchemaTreeRes domain = domainMap.computeIfAbsent(table.getBusinessDomain(), key -> newDomain(table));
            TextToSqlSchemaTreeRes.TableNode tableNode = new TextToSqlSchemaTreeRes.TableNode();
            tableNode.setId(table.getId());
            tableNode.setBusinessDomain(table.getBusinessDomain());
            tableNode.setTableName(table.getTableName());
            tableNode.setTableComment(table.getTableComment());
            tableNode.setStatus(table.getStatus());
            domain.getTables().add(tableNode);
            tableMap.put(table.getId(), tableNode);
        }

        // 再把数据库真实字段挂到表节点下面。
        // 如果字段所属 tableId 没有对应表节点，就直接跳过。
        for (TextToSqlColumnEntity column : columns) {
            TextToSqlSchemaTreeRes.TableNode tableNode = tableMap.get(column.getTableId());
            if (tableNode == null) {
                continue;
            }
            TextToSqlSchemaTreeRes.ColumnNode columnNode = new TextToSqlSchemaTreeRes.ColumnNode();
            columnNode.setId(column.getId());
            columnNode.setTableId(column.getTableId());
            columnNode.setBusinessDomain(column.getBusinessDomain());
            columnNode.setTableName(column.getTableName());
            columnNode.setColumnName(column.getColumnName());
            columnNode.setColumnComment(column.getColumnComment());
            columnNode.setStatus(column.getStatus());
            tableNode.getColumns().add(columnNode);
        }
        return new ArrayList<>(domainMap.values());
    }

    /**
     * 查询某个角色已经拥有的 Text-to-SQL 字段权限。
     */
    public TextToSqlRolePermissionRes rolePermission(Long roleId) {
        ensureRoleExists(roleId);
        List<TextToSqlRolePermissionEntity> rows = permissionMapper.selectByRoleId(roleId);
        TextToSqlRolePermissionRes res = new TextToSqlRolePermissionRes();
        res.setRoleId(roleId);

        for (TextToSqlRolePermissionEntity row : rows) {
            TextToSqlRolePermissionRes.ColumnGrant grant = new TextToSqlRolePermissionRes.ColumnGrant();
            grant.setBusinessDomain(row.getBusinessDomain());
            grant.setTableName(row.getTableName());
            grant.setColumnName(row.getColumnName());
            grant.setGranted(true);
            res.getGrants().add(grant);
        }
        return res;
    }

    /**
     * 保存某个角色的 Text-to-SQL 字段权限。
     *
     * 当前采用全量覆盖：先删除旧授权，再插入本次提交的授权字段。
     */
    @Transactional
    public void saveRolePermission(Long roleId, TextToSqlRolePermissionSaveReq req) {
        SysRoleEntity role = ensureRoleExists(roleId);
        List<TextToSqlRolePermissionEntity> grants = buildGrantedRows(role, req);
        validateColumnsExist(grants);

        permissionMapper.deleteByRoleId(roleId);
        if (!grants.isEmpty()) {
            permissionMapper.insertRolePermissions(grants);
        }
    }

    /**
     * 创建业务域节点。
     */
    private TextToSqlSchemaTreeRes newDomain(TextToSqlTableEntity table) {
        TextToSqlSchemaTreeRes domain = new TextToSqlSchemaTreeRes();
        domain.setBusinessDomain(table.getBusinessDomain());
        domain.setBusinessDomainName(table.getBusinessDomainName());
        return domain;
    }

    /**
     * 确认角色存在，并返回角色实体。
     */
    private SysRoleEntity ensureRoleExists(Long roleId) {
        SysRoleEntity role = roleId == null ? null : roleMapper.selectById(roleId);
        if (role == null) {
            throw BusinessException.of(CommonErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    /**
     * 把前端勾选的字段权限转换成数据库授权行。
     *
     * 这里只保存 granted=true 的字段，并按 businessDomain/tableName/columnName 去重。
     */
    private List<TextToSqlRolePermissionEntity> buildGrantedRows(SysRoleEntity role, TextToSqlRolePermissionSaveReq req) {
        List<TextToSqlRolePermissionEntity> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        if (req.getGrants() == null) {
            return result;
        }

        for (TextToSqlRolePermissionSaveReq.ColumnGrant grant : req.getGrants()) {
            if (!Boolean.TRUE.equals(grant.getGranted())) {
                continue;
            }
            String businessDomain = trim(grant.getBusinessDomain());
            String tableName = trim(grant.getTableName());
            String columnName = trim(grant.getColumnName());
            String key = businessDomain + "|" + tableName + "|" + columnName;
            if (!seen.add(key)) {
                continue;
            }
            TextToSqlRolePermissionEntity row = new TextToSqlRolePermissionEntity();
            row.setRoleId(role.getId());
            row.setRoleCode(role.getRoleCode());
            row.setBusinessDomain(businessDomain);
            row.setTableName(tableName);
            row.setColumnName(columnName);
            row.setStatus("ACTIVE");
            result.add(row);
        }
        return result;
    }

    /**
     * 校验授权字段必须存在于当前数据库表字段目录中。
     */
    private void validateColumnsExist(List<TextToSqlRolePermissionEntity> grants) {
        if (grants.isEmpty()) {
            return;
        }
        if (permissionMapper.countColumns(grants) != grants.size()) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "授权字段不在Text-to-SQL表字段目录中。");
        }
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
