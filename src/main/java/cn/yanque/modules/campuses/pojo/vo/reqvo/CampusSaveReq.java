package cn.yanque.modules.campuses.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CampusSaveReq {
    @NotBlank(message = "校区地点不能为空")
    @Size(max = 255, message = "校区地点长度不能超过255个字符")
    private String campusLocation;

    @NotBlank(message = "负责人不能为空")
    @Size(max = 64, message = "负责人长度不能超过64个字符")
    private String managerName;

    @NotBlank(message = "负责人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "负责人电话必须是正确的手机号格式")
    private String managerPhone;
}
