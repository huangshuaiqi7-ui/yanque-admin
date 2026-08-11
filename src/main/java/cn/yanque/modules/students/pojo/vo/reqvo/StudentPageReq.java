package cn.yanque.modules.students.pojo.vo.reqvo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StudentPageReq {
    private String studentName;
    private String studentPhone;
    private String education;
    private String school;
    private String teachingMode;
    private String studentTag;
    private String status;
    @Min(1)
    private Integer pageNum = 1;
    @Min(1)
    @Max(1000)
    private Integer pageSize = 10;
}
