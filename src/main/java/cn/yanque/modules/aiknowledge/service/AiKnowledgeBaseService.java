package cn.yanque.modules.aiknowledge.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseCreateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBasePageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseStatusReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseUpdateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeQaReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeRecallReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeBaseRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeRecallRes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiKnowledgeBaseService {
    PageResult<AiKnowledgeBaseRes> page(AiKnowledgeBasePageReq req);

    AiKnowledgeBaseRes detail(Long id);

    Long create(AiKnowledgeBaseCreateReq req);

    void update(Long id, AiKnowledgeBaseUpdateReq req);

    void updateStatus(Long id, AiKnowledgeBaseStatusReq req);

    AiKnowledgeRecallRes recall(Long id, AiKnowledgeRecallReq req);

    SseEmitter qa(Long id, AiKnowledgeQaReq req);

    void delete(Long id);
}
