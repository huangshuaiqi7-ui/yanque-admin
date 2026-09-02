package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRunEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlRunMapper {
    int insert(TextToSqlRunEntity entity);

    int updateResult(TextToSqlRunEntity entity);

    int updateFailure(TextToSqlRunEntity entity);

    int updateFeedbackById(@Param("id") Long id,
                           @Param("feedbackResult") String feedbackResult,
                           @Param("feedbackComment") String feedbackComment);

    TextToSqlRunEntity selectById(@Param("id") Long id);

    TextToSqlRunEntity selectByConversationId(@Param("conversationId") String conversationId);

    List<TextToSqlRunEntity> selectPage(@Param("keyword") String keyword,
                                        @Param("conversationId") String conversationId,
                                        @Param("sourceType") String sourceType,
                                        @Param("status") String status,
                                        @Param("feedbackResult") String feedbackResult);
}
