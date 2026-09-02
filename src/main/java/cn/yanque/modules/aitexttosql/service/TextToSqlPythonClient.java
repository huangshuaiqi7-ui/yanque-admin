package cn.yanque.modules.aitexttosql.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.yanque.modules.aitexttosql.pojo.vo.reqvo.TextToSqlAnalyzeReq;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Java 调 Python Text-to-SQL 总流程的客户端。
 */
@Component
public class TextToSqlPythonClient {
    private static final Logger log = LoggerFactory.getLogger(TextToSqlPythonClient.class);

    private final String baseUrl;

    public TextToSqlPythonClient(@Value("${yanque-ai.base-url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 调用 Python 生成 SQL、执行查询并总结结果。
     */
    public JSONObject analyze(TextToSqlAnalyzeReq req, Long userId, List<String> roleCodes) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/text-to-sql/query";
        String requestBody = JSON.toJSONString(buildRequest(req, userId, roleCodes));
        log.info("调用AI Text-to-SQL服务: userId={}, roleCodes={}, question={}", userId, roleCodes, req.getQuestion());

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI Text-to-SQL调用失败: status=" + response.getStatus() + ", body=" + body);
            }
            return JSON.parseObject(body);
        } catch (Exception exception) {
            log.warn("AI Text-to-SQL调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 调用 Python 大模型裁判，判断自然语言字段是否符合评测标准。
     */
    public JSONObject semanticJudge(String question,
                                    String actualValue,
                                    String referenceAnswer,
                                    String keyPoints,
                                    String forbiddenPoints,
                                    Integer minScore) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/text-to-sql/eval/semantic-judge";
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("question", question);
        request.put("actual_value", actualValue);
        request.put("reference_answer", referenceAnswer);
        request.put("key_points", keyPoints);
        request.put("forbidden_points", forbiddenPoints);
        request.put("min_score", minScore == null ? 80 : minScore);
        String requestBody = JSON.toJSONString(request);
        log.info("调用AI Text-to-SQL语义评测: question={}", question);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI Text-to-SQL语义评测失败: status=" + response.getStatus() + ", body=" + body);
            }
            return JSON.parseObject(body);
        } catch (Exception exception) {
            log.warn("AI Text-to-SQL语义评测异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 组装 Python Text-to-SQL 查询请求。
     *
     * Java 侧负责补 userId 和角色；Python 侧负责意图识别、选表、生成 SQL、总结。
     */
    private Map<String, Object> buildRequest(TextToSqlAnalyzeReq req, Long userId, List<String> roleCodes) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("question", req.getQuestion());
        request.put("user_id", userId == null ? null : String.valueOf(userId));
        request.put("roles", roleCodes == null ? List.of() : roleCodes);
        request.put("max_rows", req.getMaxRows() == null ? 100 : req.getMaxRows());
        request.put("conversation_id", req.getConversationId());
        request.put("clarification_answer", req.getClarificationAnswer());
        return request;
    }
}
