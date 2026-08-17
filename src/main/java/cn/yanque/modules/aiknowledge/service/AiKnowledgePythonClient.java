package cn.yanque.modules.aiknowledge.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeBaseEntity;
import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeDocumentEntity;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeDocumentChunkPageReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeQaReq;
import cn.yanque.modules.aiknowledge.pojo.vo.reqvo.AiKnowledgeRecallReq;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkDetailRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeDocumentChunkRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeRecallItemRes;
import cn.yanque.modules.aiknowledge.pojo.vo.resvo.AiKnowledgeRecallRes;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Java 调 Python 知识库服务的内部客户端。
 *
 * Java 负责 MySQL 业务数据，Python 负责 Milvus Collection、稠密向量字段和 BM25 稀疏向量字段初始化。
 */
@Component
public class AiKnowledgePythonClient {
    private static final Logger log = LoggerFactory.getLogger(AiKnowledgePythonClient.class);

    private final String baseUrl;

    public AiKnowledgePythonClient(@Value("${yanque-ai.base-url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 通知 Python 为新知识库创建 Milvus Collection。
     *
     * @param knowledgeBase 已写入 MySQL 的知识库记录
     * @return Python 返回的 Collection 名称
     */
    public String createKnowledgeBaseCollection(AiKnowledgeBaseEntity knowledgeBase) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/create";
        String requestBody = JSON.toJSONString(buildCreateRequest(knowledgeBase));
        log.info("调用AI知识库初始化服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库初始化失败: status=" + response.getStatus() + ", body=" + body);
            }
            JSONObject result = JSON.parseObject(body);
            return result.getString("collection_name");
        } catch (Exception exception) {
            log.warn("AI知识库初始化调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 通知 Python 删除知识库对应的 Milvus Collection。
     *
     * @param knowledgeBase 待删除的知识库记录
     */
    public void deleteKnowledgeBaseCollection(AiKnowledgeBaseEntity knowledgeBase) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/delete";
        String requestBody = JSON.toJSONString(buildDeleteRequest(knowledgeBase));
        log.info("调用AI知识库删除服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库删除失败: status=" + response.getStatus() + ", body=" + body);
            }
        } catch (Exception exception) {
            log.warn("AI知识库删除调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 通知 Python 下载、切分并入库指定 Markdown 文档。
     *
     * @param knowledgeBase 文档所属知识库
     * @param document 待入库文档记录
     * @param downloadUrl TOS 临时下载链接
     * @return Python 返回的 chunk 数量
     */
    public Integer indexKnowledgeDocument(AiKnowledgeBaseEntity knowledgeBase,
                                          AiKnowledgeDocumentEntity document,
                                          String downloadUrl) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/documents/index";
        String requestBody = JSON.toJSONString(buildDocumentIndexRequest(knowledgeBase, document, downloadUrl));
        log.info("调用AI知识库文档入库服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库文档入库失败: status=" + response.getStatus() + ", body=" + body);
            }
            JSONObject result = JSON.parseObject(body);
            return result.getInteger("chunk_count");
        } catch (Exception exception) {
            log.warn("AI知识库文档入库调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 通知 Python 删除指定文档在 Milvus 中的全部向量。
     *
     * @param knowledgeBase 文档所属知识库
     * @param document 待删除文档记录
     */
    public void deleteKnowledgeDocumentVectors(AiKnowledgeBaseEntity knowledgeBase,
                                               AiKnowledgeDocumentEntity document) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/documents/delete";
        String requestBody = JSON.toJSONString(buildDocumentDeleteRequest(knowledgeBase, document));
        log.info("调用AI知识库文档向量删除服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库文档向量删除失败: status=" + response.getStatus() + ", body=" + body);
            }
        } catch (Exception exception) {
            log.warn("AI知识库文档向量删除调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 从 Python 查询指定文档在 Milvus 中的 chunk 摘要列表。
     *
     * @param document 当前知识库下的文档记录
     * @param req 分页条件
     * @return 当前页 chunk 摘要
     */
    public List<AiKnowledgeDocumentChunkRes> queryKnowledgeDocumentChunks(AiKnowledgeDocumentEntity document,
                                                                          AiKnowledgeDocumentChunkPageReq req) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/documents/chunks/query";
        String requestBody = JSON.toJSONString(buildDocumentChunkQueryRequest(document, req));
        log.info("调用AI知识库文档分段查询服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库文档分段查询失败: status=" + response.getStatus() + ", body=" + body);
            }
            JSONArray records = JSON.parseObject(body).getJSONArray("records");
            if (records == null) {
                return List.of();
            }
            return records.stream()
                    .map(item -> toChunkRes((JSONObject) item))
                    .toList();
        } catch (Exception exception) {
            log.warn("AI知识库文档分段查询调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 从 Python 查询指定文档 chunk 的完整内容。
     *
     * @param document 当前知识库下的文档记录
     * @param chunkIndex chunk序号
     * @return chunk 完整内容
     */
    public AiKnowledgeDocumentChunkDetailRes getKnowledgeDocumentChunkDetail(AiKnowledgeDocumentEntity document,
                                                                            Integer chunkIndex) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/documents/chunks/detail";
        String requestBody = JSON.toJSONString(buildDocumentChunkDetailRequest(document, chunkIndex));
        log.info("调用AI知识库文档分段详情服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库文档分段详情查询失败: status=" + response.getStatus() + ", body=" + body);
            }
            return toChunkDetailRes(JSON.parseObject(body));
        } catch (Exception exception) {
            log.warn("AI知识库文档分段详情查询调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 调用 Python 在指定知识库 Collection 中执行召回测试。
     *
     * @param knowledgeBase 知识库记录
     * @param req 召回测试参数
     * @return 召回结果
     */
    public AiKnowledgeRecallRes recallKnowledgeBase(AiKnowledgeBaseEntity knowledgeBase, AiKnowledgeRecallReq req) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/recall";
        String requestBody = JSON.toJSONString(buildRecallRequest(knowledgeBase, req));
        log.info("调用AI知识库召回服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .execute()) {
            String body = response.body();
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库召回失败: status=" + response.getStatus() + ", body=" + body);
            }
            return toRecallRes(JSON.parseObject(body));
        } catch (Exception exception) {
            log.warn("AI知识库召回调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /**
     * 调用 Python 在指定知识库 Collection 中执行流式问答测试。
     *
     * @param knowledgeBase 知识库记录
     * @param req 问答测试参数
     * @param consumer SSE事件消费者
     */
    public void streamKnowledgeBaseQa(AiKnowledgeBaseEntity knowledgeBase,
                                      AiKnowledgeQaReq req,
                                      Consumer<AiStreamEvent> consumer) {
        String url = baseUrl.replaceAll("/$", "") + "/ai/knowledge-bases/qa/stream";
        String requestBody = JSON.toJSONString(buildQaRequest(knowledgeBase, req));
        log.info("调用AI知识库问答服务: {}", requestBody);

        try (HttpResponse response = HttpRequest.post(url)
                .header("Accept", "text/event-stream")
                .contentType("application/json; charset=UTF-8")
                .body(requestBody)
                .executeAsync()) {
            if (!response.isOk()) {
                throw new IllegalStateException("AI知识库问答失败: status=" + response.getStatus() + ", body=" + response.body());
            }
            readStream(response, consumer);
        } catch (Exception exception) {
            log.warn("AI知识库问答调用异常", exception);
            throw new IllegalStateException(exception);
        }
    }

    /** 组装 Python 知识库初始化接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildCreateRequest(AiKnowledgeBaseEntity knowledgeBase) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("code", knowledgeBase.getCode());
        request.put("name", knowledgeBase.getName());
        request.put("description", knowledgeBase.getDescription());
        return request;
    }

    /** 组装 Python 知识库删除接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildDeleteRequest(AiKnowledgeBaseEntity knowledgeBase) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("code", knowledgeBase.getCode());
        return request;
    }

    /** 组装 Python 文档入库接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildDocumentIndexRequest(AiKnowledgeBaseEntity knowledgeBase,
                                                          AiKnowledgeDocumentEntity document,
                                                          String downloadUrl) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("knowledge_base_id", knowledgeBase.getId());
        request.put("knowledge_base_code", knowledgeBase.getCode());
        request.put("document_id", document.getId());
        request.put("document_code", document.getCode());
        request.put("document_name", document.getName());
        request.put("document_version", document.getVersion());
        request.put("object_key", document.getObjectKey());
        request.put("download_url", downloadUrl);
        request.put("file_size", document.getFileSize());
        return request;
    }

    /** 组装 Python 文档向量删除接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildDocumentDeleteRequest(AiKnowledgeBaseEntity knowledgeBase,
                                                           AiKnowledgeDocumentEntity document) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("knowledge_base_code", knowledgeBase.getCode());
        request.put("document_code", document.getCode());
        return request;
    }

    /** 组装 Python 文档分段查询接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildDocumentChunkQueryRequest(AiKnowledgeDocumentEntity document,
                                                               AiKnowledgeDocumentChunkPageReq req) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("knowledge_base_code", document.getKnowledgeBaseCode());
        request.put("document_code", document.getCode());
        request.put("document_version", document.getVersion());
        request.put("page_num", req.getPageNum());
        request.put("page_size", req.getPageSize());
        return request;
    }

    /** 组装 Python 文档分段详情接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildDocumentChunkDetailRequest(AiKnowledgeDocumentEntity document,
                                                                Integer chunkIndex) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("knowledge_base_code", document.getKnowledgeBaseCode());
        request.put("document_code", document.getCode());
        request.put("document_version", document.getVersion());
        request.put("chunk_index", chunkIndex);
        return request;
    }

    /** 组装 Python 知识库召回接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildRecallRequest(AiKnowledgeBaseEntity knowledgeBase, AiKnowledgeRecallReq req) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("knowledge_base_code", knowledgeBase.getCode());
        request.put("query", req.getQuery());
        request.put("mode", req.getMode());
        request.put("top_k", req.getTopK());
        return request;
    }

    /** 组装 Python 知识库问答接口需要的 snake_case 请求体。 */
    private Map<String, Object> buildQaRequest(AiKnowledgeBaseEntity knowledgeBase, AiKnowledgeQaReq req) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("knowledge_base_code", knowledgeBase.getCode());
        request.put("question", req.getQuestion());
        request.put("recall_mode", req.getRecallMode());
        request.put("top_k", req.getTopK());
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

    /** 解析并转发一整块 SSE 文本。 */
    private void handleBlock(String block, Consumer<AiStreamEvent> consumer) {
        AiStreamEvent event = parseEvent(block);
        if (event.event() == null || event.data() == null) {
            return;
        }
        log.info("Java收到AI知识库问答事件: event={}, data={}", event.event(), event.data());
        consumer.accept(event);
    }

    /** 将 SSE 文本块解析成事件名和 JSON 数据。 */
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

    /** 转换 Python 返回的 chunk 摘要为 Java 管理端响应对象。 */
    private AiKnowledgeDocumentChunkRes toChunkRes(JSONObject item) {
        AiKnowledgeDocumentChunkRes result = new AiKnowledgeDocumentChunkRes();
        result.setChunkIndex(item.getInteger("chunk_index"));
        result.setContentPreview(item.getString("content_preview"));
        result.setWordCount(item.getInteger("word_count"));
        result.setCharCount(item.getInteger("char_count"));
        return result;
    }

    /** 转换 Python 返回的 chunk 详情为 Java 管理端响应对象。 */
    private AiKnowledgeDocumentChunkDetailRes toChunkDetailRes(JSONObject item) {
        AiKnowledgeDocumentChunkDetailRes result = new AiKnowledgeDocumentChunkDetailRes();
        result.setChunkIndex(item.getInteger("chunk_index"));
        result.setContent(item.getString("content"));
        result.setWordCount(item.getInteger("word_count"));
        result.setCharCount(item.getInteger("char_count"));
        return result;
    }

    /** 转换 Python 返回的召回测试结果为 Java 管理端响应对象。 */
    private AiKnowledgeRecallRes toRecallRes(JSONObject item) {
        AiKnowledgeRecallRes result = new AiKnowledgeRecallRes();
        result.setQuery(item.getString("query"));
        result.setMode(item.getString("mode"));
        result.setTopK(item.getInteger("top_k"));
        JSONArray records = item.getJSONArray("records");
        result.setRecords(records == null ? List.of() : records.stream()
                .map(record -> toRecallItemRes((JSONObject) record))
                .toList());
        return result;
    }

    /** 转换 Python 返回的单条召回结果为 Java 管理端响应对象。 */
    private AiKnowledgeRecallItemRes toRecallItemRes(JSONObject item) {
        AiKnowledgeRecallItemRes result = new AiKnowledgeRecallItemRes();
        result.setRank(item.getInteger("rank"));
        result.setScore(item.getDouble("score"));
        result.setDocumentCode(item.getString("document_code"));
        result.setDocumentVersion(item.getInteger("document_version"));
        result.setChunkIndex(item.getInteger("chunk_index"));
        result.setContent(item.getString("content"));
        result.setContentPreview(item.getString("content_preview"));
        result.setWordCount(item.getInteger("word_count"));
        return result;
    }

    /** Python 返回的一条知识库问答 SSE 事件。 */
    public record AiStreamEvent(String event, JSONObject data) {

    }
}
