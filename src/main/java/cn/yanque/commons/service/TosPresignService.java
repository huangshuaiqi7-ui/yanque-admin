package cn.yanque.commons.service;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.config.TosProperties;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.pojo.vo.resvo.PresignDownloadRes;
import cn.yanque.commons.pojo.vo.resvo.PresignUploadRes;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.comm.HttpMethod;
import com.volcengine.tos.model.object.PreSignedURLInput;
import com.volcengine.tos.model.object.PreSignedURLOutput;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TosPresignService {
    private final TOSV2 tosClient;
    private final TosProperties properties;

    public TosPresignService(TOSV2 tosClient, TosProperties properties) {
        this.tosClient = tosClient;
        this.properties = properties;
    }

    public PresignUploadRes presignUpload(String objectKey) {
        String normalizedKey = validateObjectKey(objectKey);
        try {
            PreSignedURLOutput output = tosClient.preSignedURL(new PreSignedURLInput()
                    .setBucket(properties.getBucket())
                    .setKey(normalizedKey)
                    .setHttpMethod(HttpMethod.PUT)
                    .setExpires(properties.getUploadUrlExpireSeconds()));
            Map<String, String> headers = output.getSignedHeader() == null
                    ? Map.of() : output.getSignedHeader();
            return new PresignUploadRes(output.getSignedUrl(), normalizedKey,
                    properties.getUploadUrlExpireSeconds(), headers);
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.TOS_PRESIGN_FAILED);
        }
    }

    public PresignDownloadRes presignDownload(String objectKey) {
        String normalizedKey = validateObjectKey(objectKey);
        try {
            PreSignedURLOutput output = tosClient.preSignedURL(new PreSignedURLInput()
                    .setBucket(properties.getBucket())
                    .setKey(normalizedKey)
                    .setHttpMethod(HttpMethod.GET)
                    .setExpires(properties.getDownloadUrlExpireSeconds()));
            return new PresignDownloadRes(output.getSignedUrl(), normalizedKey,
                    properties.getDownloadUrlExpireSeconds());
        } catch (Exception exception) {
            throw BusinessException.of(CommonErrorCode.TOS_PRESIGN_FAILED);
        }
    }

    private String validateObjectKey(String objectKey) {
        String normalizedKey = StrUtil.trim(objectKey);
        if (StrUtil.isBlank(normalizedKey) || normalizedKey.startsWith("/")
                || normalizedKey.contains("..") || normalizedKey.contains("\\")) {
            throw BusinessException.of(CommonErrorCode.TOS_OBJECT_KEY_INVALID);
        }
        return normalizedKey;
    }
}
