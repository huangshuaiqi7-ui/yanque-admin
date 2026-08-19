package cn.yanque.modules.aiprompt.mapper;

import cn.yanque.modules.aiprompt.pojo.entity.PromptTemplateVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PromptTemplateVersionMapper {
    List<PromptTemplateVersionEntity> selectByTemplateId(@Param("templateId") Long templateId);

    PromptTemplateVersionEntity selectById(@Param("id") Long id);

    Integer selectMaxVersionNo(@Param("templateId") Long templateId);

    int insert(PromptTemplateVersionEntity version);

    List<PromptTemplateVersionEntity> selectByIds(@Param("ids") List<Long> list);
}
