package cn.yanque.modules.aichat.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 问答会话表实体，对应 ai_chat_session。
 *
 * 会话属于某个学生，左侧会话列表和上下文读取都从这里开始。
 */
@Data
public class AiChatSessionEntity {
    /** 会话主键。 */
    private Long id;

    /** 学生 ID，用来保证学生只能看到自己的会话。 */
    private Long studentId;

    /** 会话标题，默认取第一条问题的前 30 个字符。 */
    private String title;

    /** 会话状态：ACTIVE=正常，DELETED=已删除。 */
    private String status;

    /** 历史对话摘要，压缩后的旧消息会合并到这里。 */
    private String summary;

    /** 已经压缩进 summary 的最后一条消息 ID。 */
    private Long lastCompressedMessageId;

    /** 会话创建时间。 */
    private LocalDateTime createdAt;

    /** 会话最近更新时间，用来做左侧列表排序。 */
    private LocalDateTime updatedAt;
}
