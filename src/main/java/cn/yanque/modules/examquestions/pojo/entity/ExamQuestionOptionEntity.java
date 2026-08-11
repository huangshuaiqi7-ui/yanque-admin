package cn.yanque.modules.examquestions.pojo.entity;
import lombok.Data;
@Data public class ExamQuestionOptionEntity {
    private Long id; private Long questionId; private String optionKey; private String optionContent; private Integer sortOrder;
}
