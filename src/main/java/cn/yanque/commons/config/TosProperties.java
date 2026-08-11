package cn.yanque.commons.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tos")
public class TosProperties {
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private Long uploadUrlExpireSeconds = 900L;
    private Long downloadUrlExpireSeconds = 900L;
}
