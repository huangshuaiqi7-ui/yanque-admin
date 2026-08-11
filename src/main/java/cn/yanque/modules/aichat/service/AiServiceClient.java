package cn.yanque.modules.aichat.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.yanque.modules.aichat.pojo.entity.AiChatMessageEntity;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Java 调 Python AI 服务的客户端。
 *
 * 使用 Hutool 发请求，代码更短，也和项目里其他 HTTP 调用保持一致。
 * 注意流式接口要用 executeAsync()，否则可能会等响应体读完才返回。
 */
@Component
public class AiServiceClient {
    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);
    private final String baseUrl;

    public AiServiceClient(@Value("${yanque-ai.base-url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 调用 Python 的 /ai/chat/stream，并把每个 SSE 事件交给 consumer。
     *
     * consumer 会收到 delta/done/error 三类事件，具体业务处理在 AiChatService。
     */
    public void streamChat(Map<String, Object> request, Consumer<AiStreamEvent> consumer) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/chat/stream";
        String requestBody = JSON.toJSONString(request);
        log.info("调用AI服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .header("Accept", "text/event-stream")
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .executeAsync()) {

            if (!response.isOk()) {
                throw new IllegalStateException("AI服务调用失败: status="
                        + response.getStatus() + ", body=" + response.body());
            }

            readStream(response, consumer);
        } catch (Exception exception) {
            log.warn("AI服务调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 调用 Python 生成会话摘要。
     *
     * 这是内部压缩流程使用的接口，不给前端直接调用。
     */
    public String summarize(String oldSummary, List<AiChatMessageEntity> messages) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/chat/summary";
        String requestBody = JSON.toJSONString(buildSummaryRequest(oldSummary, messages));
        log.info("调用AI摘要服务: messageCount={}", messages.size());

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            if (!response.isOk()) {
                throw new IllegalStateException("AI摘要调用失败: status="
                        + response.getStatus() + ", body=" + response.body());
            }
            return JSON.parseObject(response.body()).getString("summary");
        } catch (Exception exception) {
            log.warn("AI摘要调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /** 组装 Python 摘要接口需要的请求体。 */
    private Map<String, Object> buildSummaryRequest(String oldSummary, List<AiChatMessageEntity> messages) {
        List<Map<String, String>> history = new ArrayList<>();
        for (AiChatMessageEntity message : messages) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            history.add(item);
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("summary", oldSummary);
        request.put("messages", history);
        return request;
    }

    /** 按行读取 Python 返回的 SSE 流。 */
    private void readStream(HttpResponse response, Consumer<AiStreamEvent> consumer) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.bodyStream(), StandardCharsets.UTF_8))) {
            StringBuilder block = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    handleBlock(block.toString(), consumer);
                    block.setLength(0);
                } else {
                    block.append(line).append('\n');
                }
            }
        }
    }

    /** 处理一整块 SSE 文本，解析成功后交给业务层。 */
    private void handleBlock(String block, Consumer<AiStreamEvent> consumer) {
        AiStreamEvent event = parseEvent(block);
        if (event.event() == null || event.data() == null) {
            return;
        }
        log.info("Java收到AI事件: event={}, data={}", event.event(), event.data());
        consumer.accept(event);
    }

    /**
     * 把一块 SSE 文本解析成事件。
     *
     * SSE 格式大致是：
     * event: delta
     * data: {"content":"你好"}
     */
    private AiStreamEvent parseEvent(String block) {
        String event = null;
        String data = null;
        for (String line : block.split("\\n")) {
            if (line.startsWith("event: ")) {
                event = line.substring("event: ".length());
            } else if (line.startsWith("data: ")) {
                data = line.substring("data: ".length());
            }
        }
        return new AiStreamEvent(event, data == null ? null : JSON.parseObject(data));
    }

    /** Python 返回的一条 SSE 事件。 */
    public record AiStreamEvent(String event, JSONObject data) {

    }
}
