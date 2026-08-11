package cn.yanque.modules.aichat.pojo.vo.resvo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 新建 AI 会话返回值。
 */
@Data
@AllArgsConstructor
public class AiChatCreateSessionRes {
    /** 新会话 ID，前端后续发送消息时带上它。 */
    private Long sessionId;
}
