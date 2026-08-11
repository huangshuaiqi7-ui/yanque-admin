package cn.yanque.modules.aichat.mapper;

import cn.yanque.modules.aichat.pojo.entity.AiChatMessageEntity;
import cn.yanque.modules.aichat.pojo.entity.AiChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * AI 问答 MyBatis Mapper。
 *
 * 约定：Mapper 只返回 Entity，不直接返回 VO。
 * Entity 到 VO 的转换统一放在 Service 层，避免 SQL 层掺杂前端展示字段。
 */
@Mapper
public interface AiChatMapper {
    /** 查询学生的有效会话列表，用于聊天页左侧列表。 */
    List<AiChatSessionEntity> selectSessions(@Param("studentId") Long studentId);

    /** 查询学生自己的某个有效会话，用于权限校验和获取 summary。 */
    AiChatSessionEntity selectSession(@Param("id") Long id, @Param("studentId") Long studentId);

    /** 后台压缩流程按会话 ID 查询会话，不走学生归属校验。 */
    AiChatSessionEntity selectSessionById(@Param("id") Long id);

    /** 查询会话完整消息，用于打开历史会话展示。 */
    List<AiChatMessageEntity> selectMessages(@Param("sessionId") Long sessionId);

    /** 查询传给 AI 的上下文消息，MVP 先排除已经压缩过的消息。 */
    List<AiChatMessageEntity> selectContextMessages(@Param("sessionId") Long sessionId);

    /** 查询当前会话所有未压缩消息，用来判断是否需要生成摘要。 */
    List<AiChatMessageEntity> selectUncompressedMessages(@Param("sessionId") Long sessionId);

    /** 新建会话，id 由数据库自增后回填到 session.id。 */
    int insertSession(AiChatSessionEntity session);

    /** 保存一条 user 或 assistant 消息。 */
    int insertMessage(AiChatMessageEntity message);

    /** 写入摘要和水位线，表示这次压缩已经压到哪条消息。 */
    int updateSummary(@Param("id") Long id,
                      @Param("summary") String summary,
                      @Param("lastCompressedMessageId") Long lastCompressedMessageId);

    /** 按水位线把消息打成已压缩，日常取上下文时就能直接 compressed=0。 */
    int markCompressed(@Param("sessionId") Long sessionId, @Param("lastCompressedMessageId") Long lastCompressedMessageId);

    /** 更新会话时间，让最近聊过的会话排在前面。 */
    int touchSession(@Param("id") Long id);

    /** 逻辑删除会话，只允许删除当前学生自己的有效会话。 */
    int deleteSession(@Param("id") Long id, @Param("studentId") Long studentId);
}
