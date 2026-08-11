package cn.yanque.modules.aichat.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.aichat.pojo.vo.reqvo.AiChatSendReq;
import cn.yanque.modules.aichat.pojo.vo.resvo.AiChatCreateSessionRes;
import cn.yanque.modules.aichat.pojo.vo.resvo.AiChatMessageRes;
import cn.yanque.modules.aichat.pojo.vo.resvo.AiChatSessionRes;
import cn.yanque.modules.aichat.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 学生端 AI 问答接口。
 *
 * 前端只调用 Java，不直接访问 Python AI 服务。
 * 这一层负责身份校验后的会话管理、消息查询和流式转发。
 */
@RestController
@RequestMapping("/student/ai")
public class AiChatController {
    private final AiChatService service;

    public AiChatController(AiChatService service) {
        this.service = service;
    }

    /**
     * 查询当前学生的有效会话列表。
     *
     * 用在聊天页左侧会话列表，按最近更新时间倒序返回。
     */
    @GetMapping("/sessions")
    public ApiResponse<List<AiChatSessionRes>> sessions() {
        return ApiResponse.success(service.sessions());
    }

    /**
     * 手动新建一个空会话。
     *
     * 发送第一条消息时也可以由后端自动创建会话。
     */
    @PostMapping("/sessions")
    public ApiResponse<AiChatCreateSessionRes> createSession() {
        return ApiResponse.success(service.createSession());
    }

    /**
     * 查询某个会话里的历史消息。
     *
     * 打开或切换会话时调用，返回 user / assistant 消息列表。
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<AiChatMessageRes>> messages(@PathVariable Long sessionId) {
        return ApiResponse.success(service.messages(sessionId));
    }

    /**
     * 删除会话。
     *
     * MVP 里做逻辑删除，只把会话状态改成 DELETED，消息不物理删除。
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable Long sessionId) {
        service.deleteSession(sessionId);
        return ApiResponse.success();
    }

    /**
     * 发送问题并流式返回 AI 回答。
     *
     * 这里不能再包 ApiResponse，因为 SSE 需要持续输出多条事件：
     * delta 表示回答片段，done 表示结束，error 表示中途失败。
     */
    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> send(@Valid @RequestBody AiChatSendReq req) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(service.send(req));
    }
}
