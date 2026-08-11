package cn.yanque.modules.courses.pojo.vo.reqvo;

import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.validation.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseHomeworkTemplateSaveReq {
    @Size(max = 64, message = "阶段名称长度不能超过64个字符")
    private String stageName;

    @Positive(message = "第几天必须大于0")
    private Integer dayNumber;

    @NotBlank(message = "作业标准文档对象Key不能为空")
    @Size(max = 500, message = "作业标准文档对象Key长度不能超过500个字符")
    private String contentObjectKey;

    @NotBlank(message = "作业标准文档文件名不能为空")
    @Size(max = 255, message = "作业标准文档文件名长度不能超过255个字符")
    private String contentFileName;

    @NotBlank(message = "状态不能为空")
    @EnumValue(enumClass = CommonStatusEnum.class, message = "状态只能是ACTIVE或INACTIVE")
    private String status;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
