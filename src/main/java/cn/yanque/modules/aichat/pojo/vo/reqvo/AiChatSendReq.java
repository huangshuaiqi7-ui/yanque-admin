package cn.yanque.modules.aichat.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学生发送 AI 问答消息的请求参数。
 */
@Data
public class AiChatSendReq {
    /**
     * 会话 ID。
     *
     * 为空表示这是第一条消息，后端会自动创建一个新会话。
     */
    private Long sessionId;

    /** 学生本次输入的问题。 */
    @NotBlank(message = "问题不能为空")
    @Size(max = 8000, message = "问题最多8000个字符")
    private String question;
}
