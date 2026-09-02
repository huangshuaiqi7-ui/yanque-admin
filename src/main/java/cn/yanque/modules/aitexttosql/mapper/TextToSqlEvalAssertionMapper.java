package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlEvalAssertionMapper {
    /**
     * 批量新增样本断言。
     */
    int insertBatch(@Param("list") List<TextToSqlEvalAssertionEntity> list);

    /**
     * 删除某个样本下的全部断言。
     */
    int deleteByEvalQuestionId(@Param("evalQuestionId") Long evalQuestionId);

    /**
     * 查询某个样本下的断言列表。
     */
    List<TextToSqlEvalAssertionEntity> selectByEvalQuestionId(@Param("evalQuestionId") Long evalQuestionId);
}
