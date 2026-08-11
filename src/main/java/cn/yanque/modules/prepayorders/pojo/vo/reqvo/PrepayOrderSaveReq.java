package cn.yanque.modules.prepayorders.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data; import java.math.BigDecimal;
@Data public class PrepayOrderSaveReq {
    @NotBlank @Size(max=50) private String studentName;
    @NotBlank @Pattern(regexp="^1[3-9]\\d{9}$",message="手机号格式不正确") private String studentPhone;
    @NotNull @Positive private Long productId;
    private BigDecimal productAmount;
    @NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) private BigDecimal discountAmount;
    @NotBlank private String orderStatus;
}
