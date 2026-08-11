package cn.yanque.modules.payments.controller;

import cn.yanque.commons.apires.ApiResponse;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.ApplyReq;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.ApplyRes;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.CreateRes;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.PageReq;
import cn.yanque.modules.payments.pojo.RefundOrderEntity;
import cn.yanque.modules.payments.service.RefundOrderService;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/refundOrders")
public class RefundOrderController {
    private final RefundOrderService service;

    public RefundOrderController(RefundOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<RefundOrderEntity>> page(@Valid PageReq req) {
        PageInfo<RefundOrderEntity> page = service.page(req);
        return ApiResponse.success(new PageResult<>(
                page.getTotal(), page.getPageNum(), page.getPageSize(), page.getList()));
    }

    @PostMapping("/create")
    public ApiResponse<CreateRes> create() {
        return ApiResponse.success(service.create());
    }

    @PostMapping("/{refundOrderNo}/apply")
    public ApiResponse<ApplyRes> apply(@PathVariable String refundOrderNo, @Valid @RequestBody ApplyReq req) {
        return ApiResponse.success(service.apply(refundOrderNo, req));
    }
}
