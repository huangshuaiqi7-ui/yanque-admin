package cn.yanque.commons.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PresignUploadReq {
    @NotBlank(message = "对象Key不能为空")
    @Size(max = 500, message = "对象Key长度不能超过500个字符")
    private String objectKey;
}
