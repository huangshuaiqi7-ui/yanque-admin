package cn.yanque.modules.payments.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.PageReq;
import cn.yanque.modules.payments.pojo.PaymentOrderEntity;
import cn.yanque.modules.payments.service.PaymentOrderService;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class PaymentOrderController {
    private final PaymentOrderService service;

    public PaymentOrderController(PaymentOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<PaymentOrderEntity>> page(@Valid PageReq req) {
        PageInfo<PaymentOrderEntity> page = service.page(req);
        return ApiResponse.success(new PageResult<>(
                page.getTotal(), page.getPageNum(), page.getPageSize(), page.getList()));
    }
}
