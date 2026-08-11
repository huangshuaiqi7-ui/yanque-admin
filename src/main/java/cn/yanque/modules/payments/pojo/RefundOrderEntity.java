package cn.yanque.modules.payments.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RefundOrderEntity {
    private Long id;
    private String refundOrderNo;
    private String paymentOrderNo;
    private BigDecimal paymentAmount;
    private BigDecimal refundAmount;
    private String status;
    private String reason;
    private String uniqueRefundNo;
    private String failReason;
    private Date refundSuccessTime;
    private Date createdAt;
    private Date updatedAt;
}
