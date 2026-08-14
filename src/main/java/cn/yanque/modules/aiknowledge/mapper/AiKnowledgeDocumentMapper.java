package cn.yanque.modules.aiknowledge.mapper;

import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeDocumentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiKnowledgeDocumentMapper {
    List<AiKnowledgeDocumentEntity> selectPage(@Param("knowledgeBaseId") Long knowledgeBaseId,
                                               @Param("keyword") String keyword,
                                               @Param("status") String status);

    AiKnowledgeDocumentEntity selectById(@Param("id") Long id);

    AiKnowledgeDocumentEntity selectByIdAndKnowledgeBaseId(@Param("id") Long id,
                                                           @Param("knowledgeBaseId") Long knowledgeBaseId);

    AiKnowledgeDocumentEntity selectByCode(@Param("knowledgeBaseId") Long knowledgeBaseId,
                                           @Param("code") String code);

    AiKnowledgeDocumentEntity selectByName(@Param("knowledgeBaseId") Long knowledgeBaseId,
                                           @Param("name") String name);

    int insert(AiKnowledgeDocumentEntity document);

    int markIndexing(@Param("id") Long id, @Param("version") Integer version);

    int markReady(@Param("id") Long id, @Param("version") Integer version, @Param("chunkCount") Integer chunkCount);

    int markFailed(@Param("id") Long id, @Param("version") Integer version, @Param("errorMessage") String errorMessage);

    int deleteById(@Param("id") Long id);

    int deleteByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);
}
