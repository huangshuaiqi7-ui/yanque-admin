package cn.yanque.modules.homeworks.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class StudentLoginReq { @NotBlank @Pattern(regexp="^1[3-9]\\d{9}$") private String phone; @NotBlank private String password; }
