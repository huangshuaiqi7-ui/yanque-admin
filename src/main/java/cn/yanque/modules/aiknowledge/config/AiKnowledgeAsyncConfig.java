package cn.yanque.modules.aiknowledge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AiKnowledgeAsyncConfig {
    /**
     * 知识库文档入库专用线程池，避免 Python/Milvus 慢调用占用 Web 请求线程。
     */
    @Bean("aiKnowledgeDocumentExecutor")
    public Executor aiKnowledgeDocumentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-kb-doc-");
        executor.initialize();
        return executor;
    }
}
