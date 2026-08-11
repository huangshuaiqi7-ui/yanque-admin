package cn.yanque.modules.students.pojo.vo.reqvo;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentTagUpdateReq {
    @Size(max = 50)
    private String studentTag;
}
