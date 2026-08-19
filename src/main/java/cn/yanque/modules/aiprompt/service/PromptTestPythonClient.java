package cn.yanque.modules.aiprompt.service;

import cn.yanque.modules.aiprompt.pojo.vo.reqvo.PromptTestReq;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Java 调 Python 提示词测试服务的内部客户端。
 */
@Component
public class PromptTestPythonClient {
    private static final Logger log = LoggerFactory.getLogger(PromptTestPythonClient.class);

    private final String baseUrl;

    /**
     * 创建提示词测试 Python 客户端。
     *
     * @param baseUrl Python AI 服务地址
     */
    public PromptTestPythonClient(@Value("${yanque-ai.base-url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 调用 Python 执行提示词流式测试。
     *
     * @param systemPrompt 系统提示词内容
     * @param userPrompt   用户提示词内容
     * @param req          测试请求参数
     * @param consumer     SSE 事件消费者
     */
    public void streamPromptTest(String systemPrompt, String userPrompt, PromptTestReq req,
                                 Consumer<AiStreamEvent> consumer) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/prompts/test/stream";
        String requestBody = JSON.toJSONString(buildRequest(systemPrompt, userPrompt, req));
        log.info("调用AI提示词测试服务: systemVersionId={}, userVersionId={}, model={}",
                req.getSystemVersionId(), req.getUserVersionId(), req.getModel());

        try (HttpResponse response = HttpRequest.post(url)
                .header("Accept", "text/event-stream")
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .executeAsync()) {
            if (!response.isOk()) {
                throw new IllegalStateException("AI提示词测试失败: status=" + response.getStatus()
                        + ", body=" + response.body());
            }
            readStream(response, consumer);
        } catch (Exception exception) {
            log.warn("AI提示词测试调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /** 组装 Python 提示词测试接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildRequest(String systemPrompt, String userPrompt, PromptTestReq req) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("system_prompt", systemPrompt);
        request.put("user_prompt", userPrompt);
        request.put("variables", req.getVariables() == null ? Map.of() : req.getVariables());
        request.put("model", req.getModel());
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

    /** 处理一整块 SSE 文本。 */
    private void handleBlock(String block, Consumer<AiStreamEvent> consumer) {
        AiStreamEvent event = parseEvent(block);
        if (event.event() == null || event.data() == null) {
            return;
        }
        consumer.accept(event);
    }

    /** 把一块 SSE 文本解析成事件。 */
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

    /** Python 返回的一条提示词测试 SSE 事件。 */
    public record AiStreamEvent(String event, JSONObject data) {
    }
}
