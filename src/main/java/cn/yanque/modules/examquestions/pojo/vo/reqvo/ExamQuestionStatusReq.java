package cn.yanque.modules.examquestions.pojo.vo.reqvo;
import jakarta.validation.constraints.NotBlank; import lombok.Data;
@Data public class ExamQuestionStatusReq { @NotBlank private String status; }
