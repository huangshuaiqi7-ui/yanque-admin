package cn.yanque.modules.users.pojo.vo.reqvo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @ClassName LoginReq
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description 登录的请求对象.
 */
@Data
public class LoginReq {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
