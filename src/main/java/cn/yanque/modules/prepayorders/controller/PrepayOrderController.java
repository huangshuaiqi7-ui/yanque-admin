package cn.yanque.modules.prepayorders.controller;
import cn.yanque.commons.apires.*; import cn.yanque.modules.prepayorders.pojo.vo.reqvo.*; import cn.yanque.modules.prepayorders.pojo.vo.resvo.PrepayOrderRes; import cn.yanque.modules.prepayorders.service.PrepayOrderService;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/prepayOrders")
public class PrepayOrderController {
    private final PrepayOrderService service;public PrepayOrderController(PrepayOrderService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<PrepayOrderRes>> page(@Valid PrepayOrderPageReq req){return ApiResponse.success(service.page(req));}
    @GetMapping("/{id}") public ApiResponse<PrepayOrderRes> detail(@PathVariable Long id){return ApiResponse.success(service.detail(id));}
    @PostMapping public ApiResponse<Map<String,Long>> create(@Valid @RequestBody PrepayOrderSaveReq req){return ApiResponse.success(Map.of("id",service.create(req)));}
    @PutMapping("/{id}") public ApiResponse<Map<String,Long>> update(@PathVariable Long id,@Valid @RequestBody PrepayOrderSaveReq req){service.update(id,req);return ApiResponse.success(Map.of("id",id));}
    @DeleteMapping("/{id}") public ApiResponse<Map<String,Long>> delete(@PathVariable Long id){service.delete(id);return ApiResponse.success(Map.of("id",id));}
}
