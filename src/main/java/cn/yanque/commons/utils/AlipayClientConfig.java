package cn.yanque.commons.utils;

import cn.hutool.core.util.StrUtil;
import com.alipay.api.*;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * @ClassName AlipayClientConfig
 * @Author mrzhang
 * @Date 2026/7/20
 * @Description 读取配置的工具类.
 */

@Configuration
@PropertySource("classpath:alipay-sandbox.properties")
public class AlipayClientConfig {

    @Resource
    private Environment environment;

    @Bean
    public AlipayClient alipayClient() throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl(environment.getProperty("alipay.gateway-url"));
        //设置应用ID
        alipayConfig.setAppId(environment.getProperty("alipay.app-id"));
        //设置应用私钥
        alipayConfig.setPrivateKey(normalizeKey(environment.getProperty("alipay.merchant-private-key")));
        //设置请求格式，固定值json
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        //设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey(normalizeKey(environment.getProperty("alipay.alipay-public-key")));
        //设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);//
        //构造client
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        return alipayClient;//该对象自动进行了签名和验签。
    }

    private String normalizeKey(String key) {
        if (StrUtil.isBlank(key)) {
            return key;
        }
        return key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }
}
