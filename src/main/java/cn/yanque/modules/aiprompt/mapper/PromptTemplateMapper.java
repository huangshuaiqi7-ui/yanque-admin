package cn.yanque.modules.aiprompt.mapper;

import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PromptTemplateMapper {
    List<PromptTemplateEntity> selectPage(@Param("keyword") String keyword,
                                          @Param("agentCode") String agentCode,
                                          @Param("status") String status);

    PromptTemplateEntity selectById(@Param("id") Long id);

    PromptTemplateEntity selectByCode(@Param("code") String code);

    int insert(PromptTemplateEntity template);

    int updateById(PromptTemplateEntity template);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateActiveVersionId(@Param("id") Long id, @Param("activeVersionId") Long activeVersionId);

    int deleteById(@Param("id") Long id);
}
