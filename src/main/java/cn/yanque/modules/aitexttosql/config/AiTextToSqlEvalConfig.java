package cn.yanque.modules.aitexttosql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AiTextToSqlEvalConfig {
    /**
     * Text-to-SQL 评测任务线程池。
     *
     * 评测会批量调用 Python 和数据库，先控制并发，避免把本地服务压满。
     */
    @Bean("aiTextToSqlEvalExecutor")
    public Executor aiTextToSqlEvalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("ai-t2sql-eval-");
        executor.initialize();
        return executor;
    }
}
