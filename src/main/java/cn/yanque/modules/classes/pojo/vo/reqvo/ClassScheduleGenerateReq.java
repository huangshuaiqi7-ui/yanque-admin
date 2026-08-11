package cn.yanque.modules.classes.pojo.vo.reqvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClassScheduleGenerateReq {
    @NotNull(message = "班级不能为空")
    @Positive(message = "班级ID必须大于0")
    private Long classId;

    @NotNull(message = "第一天上课日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstClassDate;
}
