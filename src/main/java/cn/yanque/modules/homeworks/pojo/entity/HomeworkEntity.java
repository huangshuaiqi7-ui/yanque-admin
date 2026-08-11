package cn.yanque.modules.homeworks.pojo.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HomeworkEntity {
    private Long id;
    private String title;
    private String contentObjectKey;
    private String contentFileName;
    private String answerObjectKey;
    private String answerFileName;
    private Boolean answerStudentVisible;
    private Long classId;
    private LocalDate homeworkDate;
    private String classContent;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
