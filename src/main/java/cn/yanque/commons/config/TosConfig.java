package cn.yanque.commons.config;

import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TosProperties.class)
public class TosConfig {
    @Bean(destroyMethod = "close")
    public TOSV2 tosClient(TosProperties properties) {
        return new TOSV2ClientBuilder().build(properties.getRegion(), properties.getEndpoint(),
                properties.getAccessKey(), properties.getSecretKey());
    }
}
