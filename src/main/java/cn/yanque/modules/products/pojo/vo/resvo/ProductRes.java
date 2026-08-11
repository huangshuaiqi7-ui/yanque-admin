package cn.yanque.modules.products.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class ProductRes {
    private Long id; private String courseContent; private String teachingMode; private BigDecimal price;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime updatedAt;
}
