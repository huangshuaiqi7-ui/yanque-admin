package cn.yanque.modules.products.pojo.vo.reqvo;
import jakarta.validation.constraints.*; import lombok.Data; import java.math.BigDecimal;
@Data public class ProductSaveReq {
    @NotBlank @Size(max=1000) private String courseContent;
    @NotBlank private String teachingMode;
    @NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) private BigDecimal price;
}
