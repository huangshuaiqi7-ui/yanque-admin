package cn.yanque.modules.payments.pojo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

public final class RefundOrderDtos {
    private RefundOrderDtos() {
    }

    @Data
    public static class PageReq {
        private String refundOrderNo;
        private String paymentOrderNo;

        private String status;

        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }

    @Data
    public static class CreateRes {
        private String refundOrderNo;
    }

    @Data
    public static class ApplyReq {
        @NotBlank(message = "支付订单号不能为空")
        private String paymentOrderNo;

        @NotNull(message = "退款金额不能为空")
        @DecimalMin(value = "0.01", message = "退款金额必须大于0")
        private BigDecimal refundAmount;

        @Size(max = 200, message = "退款原因不能超过200个字符")
        private String reason;
    }

    @Data
    public static class ApplyRes {
        private String refundOrderNo;
        private String paymentOrderNo;
        private BigDecimal refundAmount;
        private String status;
        private String uniqueRefundNo;
    }
}
