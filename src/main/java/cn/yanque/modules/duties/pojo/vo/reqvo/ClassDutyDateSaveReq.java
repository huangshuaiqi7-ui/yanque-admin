package cn.yanque.modules.duties.pojo.vo.reqvo;

import cn.yanque.commons.enums.ClassDutyTypeEnum;
import cn.yanque.commons.validation.EnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ClassDutyDateSaveReq {
    @NotNull(message = "值班日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    @NotNull(message = "班级值班列表不能为空")
    @Valid
    private List<ClassDutyItem> classDutyList;

    @NotNull(message = "校区值班列表不能为空")
    @Valid
    private List<CampusDutyItem> campusDutyList;

    @Data
    public static class ClassDutyItem {
        @NotNull(message = "班级不能为空")
        @Positive(message = "班级ID必须大于0")
        private Long classId;
        @NotNull(message = "班级值班老师不能为空")
        @Positive(message = "老师ID必须大于0")
        private Long teacherId;
        @NotNull(message = "班级值班类型不能为空")
        @EnumValue(enumClass = ClassDutyTypeEnum.class, message = "班级值班类型不合法")
        private String dutyType;
    }

    @Data
    public static class CampusDutyItem {
        @NotNull(message = "校区不能为空")
        @Positive(message = "校区ID必须大于0")
        private Long campusId;
        @NotNull(message = "校区值班老师不能为空")
        @Positive(message = "老师ID必须大于0")
        private Long teacherId;
        @NotNull(message = "校区值班类型不能为空")
        @EnumValue(enumClass = ClassDutyTypeEnum.class, message = "校区值班类型不合法")
        private String dutyType;
    }
}
