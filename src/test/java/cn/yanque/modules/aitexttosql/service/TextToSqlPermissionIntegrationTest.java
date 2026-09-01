package cn.yanque.modules.aitexttosql.service;

import cn.yanque.modules.aitexttosql.mapper.TextToSqlPermissionMapper;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRolePermissionEntity;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlExecuteReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlValidationResult;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlPermissionCheckResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Text-to-SQL 权限集成测试。
 *
 * 这个测试会连 application-dev.yml 配置的真实数据库，用来验证 MyBatis Mapper 和权限数据。
 */
@SpringBootTest
@ActiveProfiles("dev")
class TextToSqlPermissionIntegrationTest {
    @Autowired
    private TextToSqlPermissionMapper permissionMapper;

    @Autowired
    private TextToSqlPermissionChecker permissionChecker;

    @Autowired
    private TextToSqlSqlValidator sqlValidator;

    @Test
    void advisorPermissionsCanBeLoadedAndCheckedFromDatabase() {
        List<TextToSqlRolePermissionEntity> grants = permissionMapper.selectByRoleCodes(List.of("ADVISOR"));

        System.out.println("ADVISOR Text-to-SQL grants size = " + grants.size());
        for (TextToSqlRolePermissionEntity grant : grants) {
            System.out.println(grant);
        }

        assertTrue(grants.size() > 1, "ADVISOR 应该从数据库查出多条字段授权。");
        assertNotNull(grants.get(0).getId(), "权限主键 id 应该能被 MyBatis 映射出来。");
        assertNotNull(grants.get(0).getRoleId(), "roleId 应该能被 MyBatis 映射出来。");
        assertTrue(grants.stream().anyMatch(grant -> grant.getId() != null && grant.getRoleId() != null),
                "至少应该有一条权限完整映射出 id 和 roleId。");

        TextToSqlRolePermissionEntity firstGrant = grants.get(0);
        TextToSqlExecuteReq req = new TextToSqlExecuteReq();
        req.setRoleCodes(List.of("ADVISOR"));

        TextToSqlPermissionCheckResult result = permissionChecker.check(req, SqlValidationResult.success(
                "SELECT " + firstGrant.getColumnName() + " FROM " + firstGrant.getTableName(),
                List.of(firstGrant.getTableName()),
                Map.of(firstGrant.getTableName(), List.of(firstGrant.getColumnName()))
        ));

        assertTrue(result.isAllowed(), result.getReason());
        assertFalse(result.getDeniedTables().contains(firstGrant.getTableName()));
        assertFalse(result.getDeniedColumns().contains(firstGrant.getColumnName()));
    }

    @Test
    void singleTableJsonDdlCanBeUsedBySqlValidator() {
        String ddl = """
                {
                  "table_name": "order_payment",
                  "columns": [
                    {"name": "order_no", "query_policy": "ALLOW"},
                    {"name": "student_phone", "query_policy": "MASK"},
                    {"name": "unique_order_no", "query_policy": "DENY"},
                    {"name": "status", "query_policy": "ALLOW"}
                  ]
                }
                """;

        SqlValidationResult validResult = sqlValidator.validate(
                "select count(distinct op.order_no) from order_payment op where op.status = 'SUCCESS'",
                ddl
        );
        assertTrue(validResult.isValid(), validResult.getReason());
        assertTrue(validResult.getUsedTables().contains("order_payment"));
        assertTrue(validResult.getUsedColumnMap().get("order_payment").contains("order_no"));
        assertTrue(validResult.getUsedColumnMap().get("order_payment").contains("status"));

        SqlValidationResult aliasResult = sqlValidator.validate(
                "select op.order_no from order_payment op where op.status = 'SUCCESS'",
                ddl
        );
        assertTrue(aliasResult.isValid(), aliasResult.getReason());
        assertTrue(aliasResult.getUsedColumnMap().get("order_payment").contains("order_no"));
        assertTrue(aliasResult.getUsedColumnMap().get("order_payment").contains("status"));

        SqlValidationResult unqualifiedResult = sqlValidator.validate(
                "select order_no from order_payment where status = 'SUCCESS'",
                ddl
        );
        assertFalse(unqualifiedResult.isValid(), "裸字段必须被拦截。");

        SqlValidationResult maskResult = sqlValidator.validate(
                "select op.student_phone from order_payment op where op.status = 'SUCCESS'",
                ddl
        );
        assertFalse(maskResult.isValid(), "MASK 字段不能直接返回。");

        SqlValidationResult denyResult = sqlValidator.validate(
                "select op.order_no from order_payment op where op.unique_order_no = 'x'",
                ddl
        );
        assertFalse(denyResult.isValid(), "DENY 字段任何位置都不能使用。");
    }
}
