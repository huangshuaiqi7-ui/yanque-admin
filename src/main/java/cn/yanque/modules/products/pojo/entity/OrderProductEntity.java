package cn.yanque.modules.products.pojo.entity;
import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class OrderProductEntity {
    private Long id; private String courseContent; private String teachingMode; private BigDecimal price;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
