package cn.yanque.modules.aichat.pojo.vo.resvo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 会话列表项。
 */
@Data
public class AiChatSessionRes {
    /** 会话 ID。 */
    private Long id;

    /** 会话标题。 */
    private String title;

    /** 最近更新时间，前端按这个感知最新对话。 */
    private LocalDateTime updatedAt;
}
