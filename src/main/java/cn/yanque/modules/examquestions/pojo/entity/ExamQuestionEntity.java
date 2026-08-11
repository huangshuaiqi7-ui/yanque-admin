package cn.yanque.modules.examquestions.pojo.entity;
import lombok.Data;
import java.time.LocalDateTime;
@Data public class ExamQuestionEntity {
    private Long id; private String questionType; private String questionContent;
    private String answerContent; private String analysisContent; private String difficulty;
    private String status; private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
