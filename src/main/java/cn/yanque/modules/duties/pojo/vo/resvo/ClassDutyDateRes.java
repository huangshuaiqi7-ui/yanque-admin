package cn.yanque.modules.duties.pojo.vo.resvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ClassDutyDateRes {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;
    private List<ClassDutyDateClassRes> classDutyList;
    private List<ClassDutyDateCampusRes> campusDutyList;
}
