package cn.yanque.modules.products.controller;
import cn.yanque.commons.apires.*; import cn.yanque.modules.products.pojo.vo.reqvo.*; import cn.yanque.modules.products.pojo.vo.resvo.ProductRes; import cn.yanque.modules.products.service.ProductService;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;public ProductController(ProductService service){this.service=service;}
    @GetMapping public ApiResponse<PageResult<ProductRes>> page(@Valid ProductPageReq req){return ApiResponse.success(service.page(req));}
    @GetMapping("/{id}") public ApiResponse<ProductRes> detail(@PathVariable Long id){return ApiResponse.success(service.detail(id));}
    @PostMapping public ApiResponse<Map<String,Long>> create(@Valid @RequestBody ProductSaveReq req){return ApiResponse.success(Map.of("id",service.create(req)));}
    @PutMapping("/{id}") public ApiResponse<Map<String,Long>> update(@PathVariable Long id,@Valid @RequestBody ProductSaveReq req){service.update(id,req);return ApiResponse.success(Map.of("id",id));}
    @DeleteMapping("/{id}") public ApiResponse<Map<String,Long>> delete(@PathVariable Long id){service.delete(id);return ApiResponse.success(Map.of("id",id));}
}
