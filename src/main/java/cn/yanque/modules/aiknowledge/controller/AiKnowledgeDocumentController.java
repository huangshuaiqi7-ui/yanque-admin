package cn.yanque.modules.aiknowledge.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.pojo.vo.resvo.PresignUploadRes;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentChunkPageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentCreateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentPageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentPresignReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkDetailRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentCreateRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentRes;
import cn.yanque.modules.aiknowledge.service.AiKnowledgeDocumentService;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/ai/knowledge-bases/{knowledgeBaseId}/documents")
public class AiKnowledgeDocumentController {
    private final AiKnowledgeDocumentService service;

    public AiKnowledgeDocumentController(AiKnowledgeDocumentService service) {
        this.service = service;
    }

    /**
     * 分页查询当前知识库下的文档列表。
     *
     * @param knowledgeBaseId 知识库ID
     * @param req 查询条件
     * @return 文档分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<AiKnowledgeDocumentRes>> page(@PathVariable Long knowledgeBaseId,
                                                                 @Valid AiKnowledgeDocumentPageReq req) {
        return ApiResponse.success(service.page(knowledgeBaseId, req));
    }

    /**
     * 为当前知识库下的 Markdown 文档生成 TOS 上传预签名。
     *
     * @param knowledgeBaseId 知识库ID
     * @param req 文件信息
     * @return TOS 上传预签名信息
     */
    @PostMapping("/presign-upload")
    public ApiResponse<PresignUploadRes> presignUpload(@PathVariable Long knowledgeBaseId,
                                                       @Valid @RequestBody AiKnowledgeDocumentPresignReq req) {
        return ApiResponse.success(service.presignUpload(knowledgeBaseId, req));
    }

    /**
     * 新增文档记录并异步触发切分入库。
     *
     * @param knowledgeBaseId 知识库ID
     * @param req 文档元数据
     * @return 新建文档ID和初始入库状态
     */
    @PostMapping
    public ApiResponse<AiKnowledgeDocumentCreateRes> create(@PathVariable Long knowledgeBaseId,
                                                            @Valid @RequestBody AiKnowledgeDocumentCreateReq req) {
        return ApiResponse.success(service.create(knowledgeBaseId, req));
    }

    /**
     * 分页查询当前文档在 Milvus 中的 chunk 摘要。
     *
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param req 分页条件
     * @return chunk 分页结果
     */
    @GetMapping("/{documentId}/chunks")
    public ApiResponse<PageResult<AiKnowledgeDocumentChunkRes>> pageChunks(@PathVariable Long knowledgeBaseId,
                                                                           @PathVariable Long documentId,
                                                                           @Valid AiKnowledgeDocumentChunkPageReq req) {
        return ApiResponse.success(service.pageChunks(knowledgeBaseId, documentId, req));
    }

    /**
     * 查询当前文档在 Milvus 中的指定 chunk 完整内容。
     *
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param chunkIndex chunk序号
     * @return chunk 完整内容
     */
    @GetMapping("/{documentId}/chunks/{chunkIndex}")
    public ApiResponse<AiKnowledgeDocumentChunkDetailRes> chunkDetail(@PathVariable Long knowledgeBaseId,
                                                                      @PathVariable Long documentId,
                                                                      @PathVariable @Min(0) Integer chunkIndex) {
        return ApiResponse.success(service.chunkDetail(knowledgeBaseId, documentId, chunkIndex));
    }

    /**
     * 重新切分并覆盖当前文档的向量数据。
     *
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @return 操作结果
     */
    @PostMapping("/{documentId}/rebuild")
    public ApiResponse<Void> rebuild(@PathVariable Long knowledgeBaseId, @PathVariable Long documentId) {
        service.rebuild(knowledgeBaseId, documentId);
        return ApiResponse.success();
    }

    /**
     * 物理删除当前文档，并同步删除 Milvus 向量。
     *
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @return 操作结果
     */
    @DeleteMapping("/{documentId}")
    public ApiResponse<Void> delete(@PathVariable Long knowledgeBaseId, @PathVariable Long documentId) {
        service.delete(knowledgeBaseId, documentId);
        return ApiResponse.success();
    }
}
