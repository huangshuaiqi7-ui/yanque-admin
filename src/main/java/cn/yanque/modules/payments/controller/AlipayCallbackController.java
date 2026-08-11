package cn.yanque.modules.payments.controller;

import cn.yanque.modules.payments.service.PaymentOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/yop-callback")
public class AlipayCallbackController {
    private final PaymentOrderService service;

    public AlipayCallbackController(PaymentOrderService service) {
        this.service = service;
    }

    @PostMapping("/paySuccess")
    public String paySuccess(@RequestParam Map<String, String> queryParams,
                             HttpServletRequest request) {
        Map<String, String> params = new HashMap<>(queryParams);
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        service.handleAlipayNotify(params);
        return "success";
    }
}
