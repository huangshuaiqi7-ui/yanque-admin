package cn.yanque.modules.exams.pojo.vo.reqvo;
import jakarta.validation.constraints.NotNull; import lombok.Data;
@Data public class ExamAnswerVisibleReq { @NotNull private Boolean answerVisible; }
