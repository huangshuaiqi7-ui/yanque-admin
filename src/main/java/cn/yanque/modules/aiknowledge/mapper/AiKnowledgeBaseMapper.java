package cn.yanque.modules.aiknowledge.mapper;

import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiKnowledgeBaseMapper {
    List<AiKnowledgeBaseEntity> selectPage(@Param("keyword") String keyword,
                                           @Param("status") String status);

    AiKnowledgeBaseEntity selectById(@Param("id") Long id);

    AiKnowledgeBaseEntity selectByCode(@Param("code") String code);

    int insert(AiKnowledgeBaseEntity knowledgeBase);

    int updateById(AiKnowledgeBaseEntity knowledgeBase);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int refreshStatistics(@Param("id") Long id);

    int deleteById(@Param("id") Long id);
}
