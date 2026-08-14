package cn.yanque.modules.aiknowledge.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeBaseEntity;
import cn.yanque.modules.aiknowledge.pojo.entity.AiKnowledgeDocumentEntity;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

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
        request.put("document_id", document.getId());
        return request;
    }
}
