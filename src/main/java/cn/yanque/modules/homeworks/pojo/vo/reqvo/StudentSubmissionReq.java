package cn.yanque.modules.homeworks.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data;
@Data public class StudentSubmissionReq { @NotBlank @Size(max=500) private String objectKey; @NotBlank @Size(max=255) private String fileName; }
