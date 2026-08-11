package cn.yanque.modules.aichat.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 问答消息表实体，对应 ai_chat_message。
 *
 * 一问一答会存成两行：
 * role=user 表示学生提问，role=assistant 表示 AI 回答。
 */
@Data
public class AiChatMessageEntity {
    /** 消息主键。 */
    private Long id;

    /** 所属会话 ID，对应 ai_chat_session.id。 */
    private Long sessionId;

    /** 消息角色：user=学生，assistant=AI。 */
    private String role;

    /** 消息正文。 */
    private String content;

    /** AI 回答使用的模型；学生消息这个字段为空。 */
    private String model;

    /** 本次 AI 回答消耗的 token 总数；学生消息这个字段为空。 */
    private Integer tokens;

    /** 是否已经被压缩进 summary；取上下文时只查 false 的消息。 */
    private Boolean compressed;

    /** 消息创建时间。 */
    private LocalDateTime createdAt;
}
