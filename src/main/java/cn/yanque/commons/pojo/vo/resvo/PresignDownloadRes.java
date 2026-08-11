package cn.yanque.commons.pojo.vo.resvo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignDownloadRes {
    private String downloadUrl;
    private String objectKey;
    private Long expires;
}
