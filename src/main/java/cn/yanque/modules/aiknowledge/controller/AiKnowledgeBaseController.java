package cn.yanque.modules.aiknowledge.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseCreateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBasePageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseStatusReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseUpdateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeBaseRes;
import cn.yanque.modules.aiknowledge.service.AiKnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

/**
 * 管理端知识库列表接口。
 *
 * 当前只负责知识库本身的 CRUD 和启禁用；文档上传、分段和向量入库后续放到独立接口。
 */
@RestController
@RequestMapping("/api/ai/knowledge-bases")
public class AiKnowledgeBaseController {
    private final AiKnowledgeBaseService service;

    /**
     * 创建知识库 Controller。
     *
     * @param service 知识库业务服务
     */
    public AiKnowledgeBaseController(AiKnowledgeBaseService service) {
        this.service = service;
    }

    /**
     * 分页查询知识库列表。
     *
     * @param req 查询条件，支持关键词、启用状态和分页参数
     * @return 知识库分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<AiKnowledgeBaseRes>> page(@Valid AiKnowledgeBasePageReq req) {
        return ApiResponse.success(service.page(req));
    }

    /**
     * 查询知识库详情。
     *
     * @param id 知识库ID
     * @return 知识库详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AiKnowledgeBaseRes> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }

    /**
     * 新建知识库。
     *
     * @param req 新建参数，包含名称、唯一编码和说明
     * @return 新建后的知识库ID
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody AiKnowledgeBaseCreateReq req) {
        return ApiResponse.success(Map.of("id", service.create(req)));
    }

    /**
     * 编辑知识库基础信息。
     *
     * @param id 知识库ID
     * @param req 编辑参数，只允许修改名称和说明，编码不允许修改
     * @return 当前知识库ID
     */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Long>> update(@PathVariable Long id, @Valid @RequestBody AiKnowledgeBaseUpdateReq req) {
        service.update(id, req);
        return ApiResponse.success(Map.of("id", id));
    }

    /**
     * 启用或禁用知识库。
     *
     * @param id 知识库ID
     * @param req 状态参数，只允许 ACTIVE 或 INACTIVE
     * @return 当前知识库ID和更新后的状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long id, @Valid @RequestBody AiKnowledgeBaseStatusReq req) {
        service.updateStatus(id, req);
        return ApiResponse.success(Map.of("id", id, "status", req.getStatus().trim().toUpperCase(Locale.ROOT)));
    }

    /**
     * 知识库删除按产品要求做物理删除；后续接入文档和 Milvus 后，需要在 service 中补充级联清理。
     *
     * @param id 知识库ID
     * @return 被删除的知识库ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Long>> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(Map.of("id", id));
    }
}
