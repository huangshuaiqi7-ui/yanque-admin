package cn.yanque.modules.exampapers.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class ExamPaperPageReq {
    private Long courseId; private String stageName; private String paperName;
    @Min(1) private Integer pageNum=1; @Min(1) @Max(1000) private Integer pageSize=10;
}
