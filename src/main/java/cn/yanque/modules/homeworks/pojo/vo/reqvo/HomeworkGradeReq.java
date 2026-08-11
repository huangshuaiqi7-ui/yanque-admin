package cn.yanque.modules.homeworks.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class HomeworkGradeReq { @NotNull @Min(0) @Max(100) private Integer score; @Size(max=500) private String teacherRemark; }
