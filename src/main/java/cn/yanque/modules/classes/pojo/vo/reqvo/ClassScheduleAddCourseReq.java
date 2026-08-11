package cn.yanque.modules.classes.pojo.vo.reqvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassScheduleAddCourseReq {
    @NotBlank(message = "课程内容不能为空")
    @Size(max = 1000, message = "课程内容长度不能超过1000个字符")
    private String courseContent;

    @NotNull(message = "上课日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    @NotNull(message = "老师不能为空")
    @Positive(message = "老师ID必须大于0")
    private Long teacherId;
}
