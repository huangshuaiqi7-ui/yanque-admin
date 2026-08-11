package cn.yanque.modules.examquestions.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ExamQuestionPageReq {
    private String questionType; private Long courseId; private String stageName; private String difficulty; private String status; private String keyword;
    @Min(1) private Integer pageNum=1; @Min(1) @Max(1000) private Integer pageSize=10;
}
