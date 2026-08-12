package cn.yanque.modules.aichat.service;

import cn.hutool.core.util.IdUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.utils.RedisUtils;
import cn.yanque.modules.aichat.mapper.AiChatMapper;
import cn.yanque.modules.aichat.pojo.entity.AiChatMessageEntity;
import cn.yanque.modules.aichat.pojo.entity.AiChatSessionEntity;
import cn.yanque.modules.aichat.pojo.vo.reqvo.AiChatSendReq;
import cn.yanque.modules.aichat.pojo.vo.resvo.AiChatCreateSessionRes;
import cn.yanque.modules.aichat.pojo.vo.resvo.AiChatMessageRes;
import cn.yanque.modules.aichat.pojo.vo.resvo.AiChatSessionRes;
import cn.yanque.modules.students.mapper.StudentMapper;
import cn.yanque.modules.students.pojo.entity.StudentEntity;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 问答业务服务。
 *
 * Java 这一层负责业务部分：识别当前学生、管理会话、保存消息、转发流式结果。
 * Python AI 服务只负责根据 question/history/summary 生成回答。
 */
@Service
public class AiChatService {
    /** 未压缩消息超过这个数量，就触发一次摘要压缩。 */
    private static final int COMPRESS_TRIGGER_MESSAGE_COUNT = 20;

    /** 压缩时保留最近几条原文，避免刚聊过的细节丢失。 */
    private static final int COMPRESS_KEEP_RECENT_MESSAGE_COUNT = 6;

    /** 只有压到 assistant 消息，才算压完一轮完整问答。 */
    private static final String ROLE_ASSISTANT = "assistant";

    /** Redis 压缩锁前缀，同一个会话同一时间只允许一个线程压缩。 */
    private static final String COMPRESS_LOCK_KEY_PREFIX = "ai_chat:compress:lock:";

    /** 压缩锁过期时间，防止服务异常退出后锁一直不释放。 */
    private static final Duration COMPRESS_LOCK_TTL = Duration.ofMinutes(2);

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);
    private final AiChatMapper mapper;
    private final StudentMapper studentMapper;
    private final AiServiceClient aiServiceClient;
    private final RedisUtils redisUtils;
    private final TransactionTemplate transactionTemplate;

    public AiChatService(AiChatMapper mapper, StudentMapper studentMapper,
                         AiServiceClient aiServiceClient, RedisUtils redisUtils,
                         TransactionTemplate transactionTemplate) {
        this.mapper = mapper;
        this.studentMapper = studentMapper;
        this.aiServiceClient = aiServiceClient;
        this.redisUtils = redisUtils;
        this.transactionTemplate = transactionTemplate;
    }

    /** 查询当前学生的会话列表，并把 Entity 转成前端需要的 VO。 */
    @Transactional(readOnly = true)
    public List<AiChatSessionRes> sessions() {
        return mapper.selectSessions(currentStudent().getId()).stream()
                .map(this::toSessionRes)
                .toList();
    }

    /** 手动创建一个空会话，标题先用默认值。 */
    @Transactional(rollbackFor = Exception.class)
    public AiChatCreateSessionRes createSession() {
        StudentEntity student = currentStudent();
        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setStudentId(student.getId());
        session.setTitle("新的对话");
        mapper.insertSession(session);
        return new AiChatCreateSessionRes(session.getId());
    }

    /** 查询会话消息前先校验会话归属，避免学生读取别人的记录。 */
    @Transactional(readOnly = true)
    public List<AiChatMessageRes> messages(Long sessionId) {
        StudentEntity student = currentStudent();
        requireSession(sessionId, student.getId());
        return mapper.selectMessages(sessionId).stream()
                .map(this::toMessageRes)
                .toList();
    }

    /** 逻辑删除当前学生自己的会话。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        StudentEntity student = currentStudent();
        if (mapper.deleteSession(sessionId, student.getId()) != 1) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
    }

    /**
     * 发送问题并返回 SseEmitter。
     *
     * 请求线程只负责创建 emitter；真正的 AI 调用放到后台线程里做，
     * 这样前端可以边收到 delta 边展示，不用等整段回答完成。
     */
    @Transactional(rollbackFor = Exception.class)
    public SseEmitter send(AiChatSendReq req) {
        StudentEntity student = currentStudent();
        AiChatSessionEntity session = prepareSession(req, student.getId());
        List<AiChatMessageEntity> history = mapper.selectContextMessages(session.getId());

        // 先保存学生问题。即使 AI 调用中途失败，学生输入也不会丢。
        saveUserMessage(session.getId(), req.getQuestion());

        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> streamAnswer(emitter, session, req.getQuestion(), history)).start();
        return emitter;
    }

    /** 有 sessionId 就校验并复用；没有 sessionId 就按本次问题自动创建新会话。 */
    private AiChatSessionEntity prepareSession(AiChatSendReq req, Long studentId) {
        if (req.getSessionId() != null) {
            return requireSession(req.getSessionId(), studentId);
        }

        AiChatSessionEntity session = new AiChatSessionEntity();
        session.setStudentId(studentId);
        session.setTitle(buildTitle(req.getQuestion()));
        mapper.insertSession(session);
        return session;
    }

    /** 查询会话并校验归属，查不到就按不存在处理。 */
    private AiChatSessionEntity requireSession(Long sessionId, Long studentId) {
        AiChatSessionEntity session = mapper.selectSession(sessionId, studentId);
        if (session == null) {
            throw BusinessException.of(CommonErrorCode.NOT_FOUND);
        }
        return session;
    }

    /** 保存学生提问，并刷新会话更新时间。 */
    private void saveUserMessage(Long sessionId, String question) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole("user");
        message.setContent(question);
        message.setCompressed(false);
        mapper.insertMessage(message);
        mapper.touchSession(sessionId);
    }

    /**
     * 调用 Python AI 服务，并把 Python 返回的 SSE 事件转发给前端。
     *
     * delta：马上发给前端展示，同时追加到 answer 缓冲区。
     * done：说明回答完成，把完整 answer 存库，再通知前端结束。
     * error：说明 Python 或模型出错，直接通知前端。
     */
    private void streamAnswer(SseEmitter emitter, AiChatSessionEntity session, String question,
                              List<AiChatMessageEntity> history) {
        StringBuilder answer = new StringBuilder();
        AtomicBoolean finished = new AtomicBoolean(false);
        try {
            aiServiceClient.streamChat(buildAiRequest(session, question, history), event -> {
                JSONObject data = event.data();
                if ("delta".equals(event.event())) {
                    String content = data.getString("content");
                    answer.append(content);
                    sendEvent(emitter, "delta", Map.of("content", content));
                } else if ("done".equals(event.event())) {
                    transactionTemplate.executeWithoutResult(status ->
                            saveAssistantMessage(session.getId(), answer.toString(), data));
                    Map<String, Object> done = new LinkedHashMap<>();
                    done.put("sessionId", session.getId());
                    done.put("model", data.getString("model"));
                    done.put("finishReason", data.getString("finish_reason"));
                    done.put("truncated", data.getBooleanValue("truncated"));
                    done.put("usage", data.get("usage"));
                    sendEvent(emitter, "done", done);
                    finished.set(true);
                    emitter.complete();
                    compressIfNeeded(session.getId());
                } else if ("error".equals(event.event())) {
                    sendEvent(emitter, "error", Map.of("message", data.getString("message")));
                    finished.set(true);
                    emitter.complete();
                }
            });
            if (!finished.get()) {
                sendEvent(emitter, "error", Map.of("message", "AI服务响应异常，请稍后重试"));
                emitter.complete();
            }
        } catch (Exception exception) {
            sendEvent(emitter, "error", Map.of("message", "AI服务暂时不可用，请稍后重试"));
            emitter.complete();
        }
    }

    /**
     * 按文档完成对话压缩流程。
     *
     * 1. 未压缩消息超过阈值才压缩。
     * 2. 保留最近几条消息原文，较早的消息生成 summary。
     * 3. 写 summary + 水位线，再把水位线之前的消息 compressed=1。
     */
    private void compressIfNeeded(Long sessionId) {
        String lockKey = COMPRESS_LOCK_KEY_PREFIX + sessionId;
        String lockValue = IdUtil.fastSimpleUUID();
        if (!Boolean.TRUE.equals(redisUtils.setIfAbsent(lockKey, lockValue, COMPRESS_LOCK_TTL))) {
            log.info("AI会话正在压缩，本次跳过: sessionId={}", sessionId);
            return;
        }

        try {
            AiChatSessionEntity session = mapper.selectSessionById(sessionId);
            if (session == null) {
                return;
            }

            List<AiChatMessageEntity> messages = mapper.selectUncompressedMessages(sessionId);
            if (messages.size() <= COMPRESS_TRIGGER_MESSAGE_COUNT) {
                return;
            }

            int endIndex = findCompressEndIndex(messages);
            if (endIndex <= 0) {
                log.info("AI会话暂不压缩，未找到完整问答边界: sessionId={}", sessionId);
                return;
            }

            List<AiChatMessageEntity> messagesToCompress = messages.subList(0, endIndex);
            Long lastCompressedMessageId = messagesToCompress.get(messagesToCompress.size() - 1).getId();
            String newSummary = aiServiceClient.summarize(session.getSummary(), messagesToCompress);
            if (newSummary == null || newSummary.isBlank()) {
                log.warn("AI摘要为空，本次不更新压缩状态: sessionId={}", sessionId);
                return;
            }

            transactionTemplate.executeWithoutResult(status -> {
                mapper.updateSummary(sessionId, newSummary, lastCompressedMessageId);
                mapper.markCompressed(sessionId, lastCompressedMessageId);
            });
            log.info("AI会话已压缩: sessionId={}, lastCompressedMessageId={}", sessionId, lastCompressedMessageId);
        } catch (Exception exception) {
            log.warn("AI会话压缩失败，不影响本次回答: sessionId={}", sessionId, exception);
        } finally {
            redisUtils.deleteIfValue(lockKey, lockValue);
        }
    }

    /**
     * 找压缩边界。
     *
     * 不能直接按条数切，否则可能把 user 压进 summary，却把对应 assistant 留在原文里。
     * 所以候选范围内只允许压到最后一条 assistant，保证压缩的是完整问答轮次。
     */
    private int findCompressEndIndex(List<AiChatMessageEntity> messages) {
        int candidateEndIndex = messages.size() - COMPRESS_KEEP_RECENT_MESSAGE_COUNT;
        for (int index = candidateEndIndex - 1; index >= 0; index--) {
            if (ROLE_ASSISTANT.equals(messages.get(index).getRole())) {
                return index + 1;
            }
        }
        return 0;
    }

    /** 组装发给 Python 的请求体：本次问题、历史消息、会话摘要。 */
    private Map<String, Object> buildAiRequest(AiChatSessionEntity session, String question,
                                               List<AiChatMessageEntity> history) {
        List<Map<String, String>> historyItems = new ArrayList<>();
        for (AiChatMessageEntity item : history) {
            historyItems.add(Map.of("role", item.getRole(), "content", item.getContent()));
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("question", question);
        request.put("history", historyItems);
        request.put("summary", session.getSummary());
        return request;
    }

    /** 保存 AI 完整回答；token 用量从 Python 的 done 事件里取。 */
    private void saveAssistantMessage(Long sessionId, String answer, JSONObject data) {
        AiChatMessageEntity message = new AiChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole("assistant");
        message.setContent(answer);
        message.setModel(data.getString("model"));
        JSONObject usage = data.getJSONObject("usage");
        if (usage != null) {
            message.setTokens(usage.getInteger("total_tokens"));
        }
        message.setCompressed(false);
        mapper.insertMessage(message);
        mapper.touchSession(sessionId);
    }

    /** 发送一条 SSE 事件给前端。 */
    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            log.info("Java发送前端SSE: event={}, data={}", name, data);
            emitter.send(SseEmitter.event().name(name).data(JSON.toJSONString(data)));
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }

    /** 获取当前登录学生，并检查学生账号是否可用。 */
    private StudentEntity currentStudent() {
        Long id = UserContext.getUserId();
        StudentEntity student = id == null ? null : studentMapper.selectById(id);
        if (student == null) {
            throw BusinessException.of(CommonErrorCode.STUDENT_NOT_FOUND);
        }
        if (!CommonStatusEnum.ACTIVE.name().equals(student.getStatus())) {
            throw BusinessException.of(CommonErrorCode.STUDENT_NOT_ACTIVE);
        }
        return student;
    }

    /** Entity 转会话列表 VO，Mapper 不直接返回 VO。 */
    private AiChatSessionRes toSessionRes(AiChatSessionEntity entity) {
        AiChatSessionRes res = new AiChatSessionRes();
        res.setId(entity.getId());
        res.setTitle(entity.getTitle());
        res.setUpdatedAt(entity.getUpdatedAt());
        return res;
    }

    /** Entity 转消息展示 VO，Mapper 不直接返回 VO。 */
    private AiChatMessageRes toMessageRes(AiChatMessageEntity entity) {
        AiChatMessageRes res = new AiChatMessageRes();
        res.setId(entity.getId());
        res.setSessionId(entity.getSessionId());
        res.setRole(entity.getRole());
        res.setContent(entity.getContent());
        res.setModel(entity.getModel());
        res.setTokens(entity.getTokens());
        res.setCreatedAt(entity.getCreatedAt());
        return res;
    }

    /** 用第一条问题生成默认标题，太长就截断，避免左侧列表撑开。 */
    private String buildTitle(String question) {
        String title = question.trim().replaceAll("\\s+", " ");
        return title.length() > 30 ? title.substring(0, 30) : title;
    }
}
