package cn.yanque.modules.aitexttosql.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlRolePermissionSaveReq;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlRolePermissionRes;
import cn.yanque.modules.aitexttosql.pojo.vo.resvo.TextToSqlSchemaTreeRes;
import cn.yanque.modules.aitexttosql.service.TextToSqlPermissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端 Text-to-SQL 角色数据权限配置接口。
 */
@RestController
@RequestMapping("/api/ai/text-to-sql")
public class TextToSqlPermissionController {
    private final TextToSqlPermissionService service;

    public TextToSqlPermissionController(TextToSqlPermissionService service) {
        this.service = service;
    }

    @GetMapping("/schema-tree")
    public ApiResponse<List<TextToSqlSchemaTreeRes>> schemaTree() {
        return ApiResponse.success(service.schemaTree());
    }

    @GetMapping("/permissions/roles/{roleId}")
    public ApiResponse<TextToSqlRolePermissionRes> rolePermission(@PathVariable Long roleId) {
        return ApiResponse.success(service.rolePermission(roleId));
    }

    @PutMapping("/permissions/roles/{roleId}")
    public ApiResponse<Map<String, Long>> saveRolePermission(
            @PathVariable Long roleId,
            @Valid @RequestBody TextToSqlRolePermissionSaveReq req
    ) {
        service.saveRolePermission(roleId, req);
        return ApiResponse.success(Map.of("roleId", roleId));
    }
}
