package cn.yanque.modules.classes.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClassSaveReq {
    @NotBlank(message = "班级期数不能为空")
    @Size(max = 64, message = "班级期数长度不能超过64个字符")
    private String classPeriod;
    @NotNull(message = "班主任不能为空")
    @Positive(message = "班主任ID必须大于0")
    private Long headTeacherId;
    @NotNull(message = "校区不能为空")
    @Positive(message = "校区ID必须大于0")
    private Long campusId;
    @NotNull(message = "课程不能为空")
    @Positive(message = "课程ID必须大于0")
    private Long courseId;
}
