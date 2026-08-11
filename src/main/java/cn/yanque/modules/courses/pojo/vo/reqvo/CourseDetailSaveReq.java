package cn.yanque.modules.courses.pojo.vo.reqvo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseDetailSaveReq {
    @NotBlank(message = "阶段名称不能为空")
    @Size(max = 128, message = "阶段名称长度不能超过128个字符")
    private String stageName;
    @Min(value = 1, message = "课程天数必须大于0")
    private Integer dayNumber;
    @Size(max = 1000, message = "上课内容长度不能超过1000个字符")
    private String classContent;
}
