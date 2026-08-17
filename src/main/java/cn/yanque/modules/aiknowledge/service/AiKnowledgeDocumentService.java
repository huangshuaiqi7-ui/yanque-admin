package cn.yanque.modules.aiknowledge.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.pojo.vo.resvo.PresignUploadRes;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentCreateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentChunkPageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentPageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentPresignReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkDetailRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentCreateRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentRes;

public interface AiKnowledgeDocumentService {
    PageResult<AiKnowledgeDocumentRes> page(Long knowledgeBaseId, AiKnowledgeDocumentPageReq req);

    PresignUploadRes presignUpload(Long knowledgeBaseId, AiKnowledgeDocumentPresignReq req);

    AiKnowledgeDocumentCreateRes create(Long knowledgeBaseId, AiKnowledgeDocumentCreateReq req);

    PageResult<AiKnowledgeDocumentChunkRes> pageChunks(Long knowledgeBaseId, Long documentId, AiKnowledgeDocumentChunkPageReq req);

    AiKnowledgeDocumentChunkDetailRes chunkDetail(Long knowledgeBaseId, Long documentId, Integer chunkIndex);

    void rebuild(Long knowledgeBaseId, Long documentId);

    void delete(Long knowledgeBaseId, Long documentId);
}
