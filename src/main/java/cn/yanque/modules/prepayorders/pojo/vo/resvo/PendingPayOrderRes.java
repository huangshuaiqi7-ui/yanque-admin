package cn.yanque.modules.prepayorders.pojo.vo.resvo;
import lombok.Data; import java.math.BigDecimal;
@Data public class PendingPayOrderRes {
    private Long id; private String orderNo; private String studentName; private String studentPhone;
    private Long productId; private String productContent; private BigDecimal productAmount;
    private BigDecimal discountAmount; private BigDecimal payableAmount; private String orderStatus;
}
