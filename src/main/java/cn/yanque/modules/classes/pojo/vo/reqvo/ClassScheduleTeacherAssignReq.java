package cn.yanque.modules.classes.pojo.vo.reqvo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

@Data
public class ClassScheduleTeacherAssignReq {
    @NotEmpty(message = "阶段老师分配信息不能为空")
    @Valid
    private List<StageTeacher> stages;

    @Data
    public static class StageTeacher {
        @NotBlank(message = "课程阶段不能为空")
        private String stageName;
        @NotNull(message = "老师不能为空")
        @Positive(message = "老师ID必须大于0")
        private Long teacherId;
    }
}
