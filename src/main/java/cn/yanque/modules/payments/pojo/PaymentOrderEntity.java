package cn.yanque.modules.payments.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentOrderEntity {
    private Long id;
    private String orderNo;
    private String studentPhone;
    private String studentName;
    private String productId;
    private String productContent;
    private String teachingMode;
    private BigDecimal orderAmount;
    private BigDecimal refundedAmount;
    private String prepayOrderNo;
    private String status;
    private String uniqueOrderNo;
    private Date paySuccessTime;
    private Date createdAt;
    private Date updatedAt;
}
