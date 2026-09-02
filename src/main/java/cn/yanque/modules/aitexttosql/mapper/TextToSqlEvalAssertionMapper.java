package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalAssertionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlEvalAssertionMapper {
    int insertBatch(@Param("list") List<TextToSqlEvalAssertionEntity> list);

    int deleteByEvalQuestionId(@Param("evalQuestionId") Long evalQuestionId);

    List<TextToSqlEvalAssertionEntity> selectByEvalQuestionId(@Param("evalQuestionId") Long evalQuestionId);
}
