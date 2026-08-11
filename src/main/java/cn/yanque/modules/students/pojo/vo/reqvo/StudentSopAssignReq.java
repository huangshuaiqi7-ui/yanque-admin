package cn.yanque.modules.students.pojo.vo.reqvo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentSopAssignReq {
    @NotNull
    private Long mentorId;
}
