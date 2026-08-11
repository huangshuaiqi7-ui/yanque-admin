package cn.yanque.modules.users.pojo.vo.reqvo;

import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.validation.EnumValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserPageReq {

    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 1000, message = "每页条数不能超过1000")
    private Integer pageSize = 10;

    private String keyword;
    private String roleCode;

    @EnumValue(enumClass = CommonStatusEnum.class, message = "状态只能是ACTIVE或INACTIVE")
    private String status;
}
