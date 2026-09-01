package cn.yanque.modules.aitexttosql.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Text-to-SQL 执行请求。
 */
@Data
public class TextToSqlExecuteReq {
    @NotBlank(message = "SQL不能为空")
    private String sql;

    @NotBlank(message = "表DDL上下文不能为空")
    private String tableDdlContext;

    private Long userId;
    /**
     * Java 查询中心正式使用的角色编码字段。
     */
    private List<String> roleCodes = new ArrayList<>();
    /**
     * 兼容 Python 总接口里的 roles 命名，避免直接调用内部接口时字段名传错导致角色为空。
     */
    private List<String> roles = new ArrayList<>();
    private String businessDomain;
    private Integer maxRows = 100;
}
