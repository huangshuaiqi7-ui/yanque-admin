package cn.yanque.modules.homeworks.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentHomeworkRes {
    private Long id;
    private String title;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate homeworkDate;
    private String classContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    private String contentFileName;
    private String contentObjectKey;
    private Boolean answerVisible;
    private String answerFileName;
    private String answerObjectKey;
    private Boolean submitted;
    private String submissionFileName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    private Boolean lateSubmitted;
    private Integer score;
    private String teacherRemark;
}
