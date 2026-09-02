package cn.yanque.modules.aitexttosql.service;

import cn.yanque.commons.constant.RbacConstants;
import cn.yanque.modules.aitexttosql.mapper.TextToSqlPermissionMapper;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRolePermissionEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlExecuteReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlValidationResult;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlPermissionCheckResult;
import cn.yanque.modules.roles.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Text-to-SQL 角色数据权限校验。
 *
 * SQL 结构安全由 TextToSqlSqlValidator 负责；这里只判断“当前角色能不能看这些表和字段”。
 */
@Service
public class TextToSqlPermissionChecker {
    private final TextToSqlPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;

    public TextToSqlPermissionChecker(TextToSqlPermissionMapper permissionMapper, SysRoleMapper roleMapper) {
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
    }

    /**
     * 校验当前请求有没有权限执行这条 SQL。
     *
     * 入参里的 validation 是前一步 AST 校验结果，里面已经提取好了 SQL 用到的表和字段：
     * - usedTables：SQL FROM / JOIN 等位置用到的真实表名；
     * - usedColumnMap：SQL 使用的字段，按真实表名分组。
     *
     * 这里不再判断 DENY / MASK 字段策略，因为字段安全策略以 DDL Resource 为准，
     * 已经由 TextToSqlSqlValidator 在前一步处理。这里仅判断角色有没有表、字段访问权限。
     */
    public TextToSqlPermissionCheckResult check(TextToSqlExecuteReq req, SqlValidationResult validation) {
        // 内部接口会优先接收 Python 传来的 roleCodes；如果没传，再按 userId 从系统角色表查询。
        List<String> roleCodes = resolveRoleCodes(req);

        // 超级管理员仍然要经过 SQL AST / DDL 安全校验，但跳过角色数据权限配置。
        if (roleCodes.contains(RbacConstants.SUPER_ADMIN_ROLE)) {
            return TextToSqlPermissionCheckResult.allowed();
        }
        if (roleCodes.isEmpty()) {
            return TextToSqlPermissionCheckResult.denied("当前用户没有可用角色，不能执行Text-to-SQL查询。");
        }

        // 多角色权限取并集：只要任意一个角色授权了某个字段，就认为当前用户拥有该字段权限。
        List<TextToSqlRolePermissionEntity> grants = permissionMapper.selectByRoleCodes(roleCodes);
        if (grants == null || grants.isEmpty()) {
            return TextToSqlPermissionCheckResult.denied("当前角色没有配置Text-to-SQL数据权限。");
        }

        // 先校验表，再校验字段。表都不允许访问时，不需要继续判断字段。
        // 不按 businessDomain 硬过滤，因为一个问题可能从订单表关联到学生、班级等其他业务域。
        TextToSqlPermissionCheckResult tableResult = checkTables(validation.getUsedTables(), grants);
        if (!tableResult.isAllowed()) {
            return tableResult;
        }

        // 字段权限只判断“角色有没有授权”，不判断字段是否敏感；敏感策略由 DDL Resource 控制。
        TextToSqlPermissionCheckResult columnResult = checkColumns(validation, grants);
        if (!columnResult.isAllowed()) {
            return columnResult;
        }

        return TextToSqlPermissionCheckResult.allowed();
    }

    /**
     * 解析本次请求的角色编码。
     *
     * Python 调内部查询中心时会带 roleCodes；如果没带，就按 userId 从数据库查询。
     */
    private List<String> resolveRoleCodes(TextToSqlExecuteReq req) {
        List<String> roleCodes = cleanValues(req.getRoleCodes());
        if (roleCodes.isEmpty()) {
            roleCodes = cleanValues(req.getRoles());
        }
        if (!roleCodes.isEmpty() || req.getUserId() == null) {
            return roleCodes;
        }
        return cleanValues(roleMapper.selectRoleCodesByUserId(req.getUserId()));
    }

    /**
     * 校验 SQL 使用的表是否都在当前角色授权范围内。
     */
    private TextToSqlPermissionCheckResult checkTables(List<String> usedTables, List<TextToSqlRolePermissionEntity> grants) {
        Set<String> allowedTables = new LinkedHashSet<>();
        for (TextToSqlRolePermissionEntity grant : grants) {
            allowedTables.add(normalize(grant.getTableName()));
        }

        List<String> deniedTables = new ArrayList<>();
        for (String table : usedTables) {
            if (!allowedTables.contains(normalize(table))) {
                deniedTables.add(table);
            }
        }

        if (deniedTables.isEmpty()) {
            return TextToSqlPermissionCheckResult.allowed();
        }
        TextToSqlPermissionCheckResult result = TextToSqlPermissionCheckResult.denied(
                "当前角色没有表权限：" + String.join("、", deniedTables) + "。"
        );
        result.setDeniedTables(deniedTables);
        return result;
    }

    /**
     * 校验 SQL 使用的字段是否都在当前角色授权范围内。
     */
    private TextToSqlPermissionCheckResult checkColumns(SqlValidationResult validation, List<TextToSqlRolePermissionEntity> grants) {
        Set<String> allowedColumnKeys = new LinkedHashSet<>();
        for (TextToSqlRolePermissionEntity grant : grants) {
            String table = normalize(grant.getTableName());
            String column = normalize(grant.getColumnName());
            allowedColumnKeys.add(table + "." + column);
        }

        List<String> deniedColumns = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : validation.getUsedColumnMap().entrySet()) {
            String table = normalize(entry.getKey());
            for (String column : entry.getValue()) {
                String columnKey = table + "." + normalize(column);
                if (!allowedColumnKeys.contains(columnKey)) {
                    deniedColumns.add(entry.getKey() + "." + column);
                }
            }
        }

        if (deniedColumns.isEmpty()) {
            return TextToSqlPermissionCheckResult.allowed();
        }
        TextToSqlPermissionCheckResult result = TextToSqlPermissionCheckResult.denied(
                "当前角色没有字段权限：" + String.join("、", deniedColumns) + "。"
        );
        result.setDeniedColumns(deniedColumns);
        return result;
    }

    /**
     * 清理角色编码列表。
     *
     * 去掉空值、去重，并统一转大写，避免大小写导致权限判断失败。
     */
    private List<String> cleanValues(List<String> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            String cleaned = value == null ? "" : value.trim();
            if (StringUtils.hasText(cleaned) && !result.contains(cleaned)) {
                result.add(cleaned.toUpperCase(Locale.ROOT));
            }
        }
        return result;
    }

    /**
     * 表名和字段名比较时统一转小写。
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
