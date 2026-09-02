package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRunEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlRunMapper {
    /**
     * 新增运行记录。
     */
    int insert(TextToSqlRunEntity entity);

    /**
     * 保存运行成功或等待澄清时的结果。
     */
    int updateResult(TextToSqlRunEntity entity);

    /**
     * 保存运行失败结果。
     */
    int updateFailure(TextToSqlRunEntity entity);

    /**
     * 保存运行记录的最新反馈。
     */
    int updateFeedbackById(@Param("id") Long id,
                           @Param("feedbackResult") String feedbackResult,
                           @Param("feedbackComment") String feedbackComment);

    /**
     * 按 ID 查询运行记录。
     */
    TextToSqlRunEntity selectById(@Param("id") Long id);

    /**
     * 按会话 ID 查询运行记录。
     */
    TextToSqlRunEntity selectByConversationId(@Param("conversationId") String conversationId);

    /**
     * 分页筛选运行记录。
     */
    List<TextToSqlRunEntity> selectPage(@Param("keyword") String keyword,
                                        @Param("conversationId") String conversationId,
                                        @Param("sourceType") String sourceType,
                                        @Param("status") String status,
                                        @Param("feedbackResult") String feedbackResult);
}
