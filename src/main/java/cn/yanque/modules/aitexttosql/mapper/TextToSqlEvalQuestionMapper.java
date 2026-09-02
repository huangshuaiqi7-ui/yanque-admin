package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlEvalQuestionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlEvalQuestionMapper {
    int insert(TextToSqlEvalQuestionEntity entity);

    int updateById(TextToSqlEvalQuestionEntity entity);

    TextToSqlEvalQuestionEntity selectById(@Param("id") Long id);

    TextToSqlEvalQuestionEntity selectBySourceRunId(@Param("sourceRunId") Long sourceRunId);

    List<TextToSqlEvalQuestionEntity> selectPage(@Param("keyword") String keyword,
                                                 @Param("businessDomain") String businessDomain,
                                                 @Param("evalTarget") String evalTarget,
                                                 @Param("sampleCategory") String sampleCategory,
                                                 @Param("sourceType") String sourceType,
                                                 @Param("status") String status);
}
