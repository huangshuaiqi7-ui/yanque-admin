package cn.yanque.modules.prepayorders.pojo.vo.resvo;
import com.fasterxml.jackson.annotation.JsonFormat; import lombok.Data; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data public class PrepayOrderRes {
    private Long id; private String orderNo; private String studentName; private String studentPhone;
    private Long productId; private BigDecimal productAmount; private BigDecimal discountAmount; private String orderStatus;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") private LocalDateTime updatedAt;
}
