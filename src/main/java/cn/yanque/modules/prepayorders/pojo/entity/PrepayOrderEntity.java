package cn.yanque.modules.prepayorders.pojo.entity;
import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class PrepayOrderEntity {
    private Long id; private String orderNo; private String studentName; private String studentPhone;
    private Long productId; private BigDecimal productAmount; private BigDecimal discountAmount;
    private String orderStatus; private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
