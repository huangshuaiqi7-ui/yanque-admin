package cn.yanque.modules.aitexttosql.service;

import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlExplainPlanRow;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.SqlExplainResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextToSqlExplainCheckerTest {

    private final TextToSqlExplainChecker checker = new TextToSqlExplainChecker(null);

    @Test
    void derivedTableWithoutIndexShouldNotBeDenied() {
        SqlExplainResult result = checker.evaluatePlans(List.of(
                plan("PRIMARY", "<derived3>", "system", null),
                plan("PRIMARY", "<derived2>", "ALL", null),
                plan("DERIVED", "op", "ref", "idx_status"),
                plan("DERIVED", "op", "ref", "idx_status")
        ));

        assertTrue(result.isAllowed(), result.getMessage());
    }

    @Test
    void realTableWithoutIndexShouldBeDenied() {
        SqlExplainResult result = checker.evaluatePlans(List.of(
                plan("SIMPLE", "order_payment", "ALL", null)
        ));

        assertFalse(result.isAllowed());
    }

    private SqlExplainPlanRow plan(String selectType, String tableName, String accessType, String usedKey) {
        SqlExplainPlanRow row = new SqlExplainPlanRow();
        row.setSelectType(selectType);
        row.setTableName(tableName);
        row.setAccessType(accessType);
        row.setUsedKey(usedKey);
        return row;
    }
}
