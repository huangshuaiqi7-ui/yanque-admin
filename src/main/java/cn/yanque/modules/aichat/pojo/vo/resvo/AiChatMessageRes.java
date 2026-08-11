package cn.yanque.modules.aichat.pojo.vo.resvo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 会话里的消息展示对象。
 */
@Data
public class AiChatMessageRes {
    /** 消息 ID；前端临时流式消息可能没有这个值。 */
    private Long id;

    /** 所属会话 ID。 */
    private Long sessionId;

    /** 消息角色：user=学生，assistant=AI。 */
    private String role;

    /** 消息正文。 */
    private String content;

    /** AI 回答使用的模型。 */
    private String model;

    /** AI 回答消耗的 token 总数。 */
    private Integer tokens;

    /** 消息创建时间。 */
    private LocalDateTime createdAt;
}
