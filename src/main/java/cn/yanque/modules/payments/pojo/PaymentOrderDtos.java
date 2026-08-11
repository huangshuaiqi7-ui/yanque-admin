package cn.yanque.modules.payments.pojo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

public final class PaymentOrderDtos {
    private PaymentOrderDtos() {
    }

    @Data
    public static class PageReq {
        private String orderNo;
        private String studentName;
        private String studentPhone;
        private String prepayOrderNo;

        private String status;

        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }

    @Data
    public static class CreateOrderNoRes {
        private String orderNo;
    }

    @Data
    public static class CreatePaymentOrderReq {
        @NotBlank(message = "支付订单号不能为空")
        @Size(max = 64, message = "支付订单号不能超过64个字符")
        private String orderNo;

        @NotBlank(message = "学生手机号不能为空")
        @Size(max = 30, message = "学生手机号不能超过30个字符")
        private String studentPhone;

        @NotBlank(message = "学生姓名不能为空")
        @Size(max = 50, message = "学生姓名不能超过50个字符")
        private String studentName;

        @NotBlank(message = "产品不能为空")
        @Size(max = 64, message = "产品不能超过64个字符")
        private String productId;

        @NotNull(message = "支付金额不能为空")
        @DecimalMin(value = "0.01", message = "支付金额必须大于0")
        private BigDecimal orderAmount;

        @NotBlank(message = "预支付订单号不能为空")
        @Size(max = 32, message = "预支付订单号不能超过32个字符")
        private String prepayOrderNo;
    }

    @Data
    public static class CreatePaymentOrderRes {
        private String cashierUrl;
    }

    @Data
    public static class PaymentReturnInfo {
        private String orderNo;
        private String studentName;
        private String studentPhone;
        private String productId;
        private String productContent;
        private BigDecimal orderAmount;
        private String status;
    }

    @Data
    public static class CompleteProfileReq {
        @NotBlank(message = "支付订单号不能为空")
        private String orderNo;

        @NotBlank(message = "登录密码不能为空")
        private String password;

        @NotBlank(message = "确认密码不能为空")
        private String confirmPassword;

        @NotBlank(message = "学历不能为空")
        private String education;

        @NotNull(message = "届数不能为空")
        private Integer gradeYear;

        @NotBlank(message = "学校不能为空")
        private String school;

        private String major;
    }

    @Data
    public static class CompleteProfileRes {
        private Long studentId;
        private Boolean completed;
        private String token;
        private String signSecret;
        private StudentInfo student;
    }

    @Data
    public static class StudentInfo {
        private Long id;
        private String name;
        private String phone;
    }
}
