package cn.yanque.modules.payments.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CompleteProfileReq;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CompleteProfileRes;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CreateOrderNoRes;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CreatePaymentOrderReq;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CreatePaymentOrderRes;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.PaymentReturnInfo;
import cn.yanque.modules.payments.service.PaymentOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/pending")
public class StudentPaymentController {
    private final PaymentOrderService service;

    public StudentPaymentController(PaymentOrderService service) {
        this.service = service;
    }

    @PostMapping("/order/createOrderNo")
    public ApiResponse<CreateOrderNoRes> createOrderNo() {
        return ApiResponse.success(service.createOrderNo());
    }

    @PostMapping("/order/createPaymentOrder")
    public ApiResponse<CreatePaymentOrderRes> createPaymentOrder(@Valid @RequestBody CreatePaymentOrderReq req) {
        return ApiResponse.success(service.createPaymentOrder(req));
    }

    @GetMapping("/order/paymentReturnInfo")
    public ApiResponse<PaymentReturnInfo> paymentReturnInfo(@RequestParam String orderNo) {
        return ApiResponse.success(service.paymentReturnInfo(orderNo));
    }

    @PostMapping("/profile/completeProfile")
    public ApiResponse<CompleteProfileRes> completeProfile(@Valid @RequestBody CompleteProfileReq req) {
        return ApiResponse.success(service.completeProfile(req));
    }
}
