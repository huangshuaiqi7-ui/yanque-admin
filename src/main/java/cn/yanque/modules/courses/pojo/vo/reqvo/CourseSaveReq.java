package cn.yanque.modules.courses.pojo.vo.reqvo;

import cn.yanque.commons.enums.TeachingModeEnum;
import cn.yanque.commons.validation.EnumValue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseSaveReq {
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 128, message = "课程名称长度不能超过128个字符")
    private String courseName;
    @NotNull(message = "课程天数不能为空")
    @Min(value = 1, message = "课程天数必须大于0")
    private Integer courseDays;
    @NotBlank(message = "上课方式不能为空")
    @EnumValue(enumClass = TeachingModeEnum.class, message = "上课方式只能是ONLINE或OFFLINE")
    private String teachingMode;
    @NotBlank(message = "资料路径不能为空")
    @Size(max = 500, message = "资料路径长度不能超过500个字符")
    private String materialPath;
}
