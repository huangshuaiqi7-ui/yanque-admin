package cn.yanque.modules.aiknowledge.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseCreateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBasePageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseStatusReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeBaseUpdateReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeBaseRes;

public interface AiKnowledgeBaseService {
    PageResult<AiKnowledgeBaseRes> page(AiKnowledgeBasePageReq req);

    AiKnowledgeBaseRes detail(Long id);

    Long create(AiKnowledgeBaseCreateReq req);

    void update(Long id, AiKnowledgeBaseUpdateReq req);

    void updateStatus(Long id, AiKnowledgeBaseStatusReq req);

    void delete(Long id);
}
