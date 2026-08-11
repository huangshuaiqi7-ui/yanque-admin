package cn.yanque.modules.users.pojo.vo.reqvo;

import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.validation.EnumValue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateReq {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在6到64个字符之间")
    private String password;

    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    @Size(max = 64, message = "真实姓名长度不能超过64个字符")
    private String realName;

    @Size(max = 32, message = "手机号长度不能超过32个字符")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    @Size(max = 128, message = "unionId长度不能超过128个字符")
    private String unionId;

    @EnumValue(enumClass = CommonStatusEnum.class, message = "状态只能是ACTIVE或INACTIVE")
    private String status;
}
