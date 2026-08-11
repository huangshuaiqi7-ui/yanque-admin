package cn.yanque.modules.homeworks.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HomeworkRes {
    private Long id;
    private String title;
    private String contentObjectKey;
    private String contentFileName;
    private String answerObjectKey;
    private String answerFileName;
    private Boolean answerStudentVisible;
    private Long classId;
    private String classPeriod;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate homeworkDate;
    private String classContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
