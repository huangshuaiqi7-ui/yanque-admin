package cn.yanque.modules.products.service;
import cn.yanque.commons.apires.*; import cn.yanque.modules.products.pojo.vo.reqvo.*; import cn.yanque.modules.products.pojo.vo.resvo.ProductRes;
public interface ProductService {
    PageResult<ProductRes> page(ProductPageReq req); ProductRes detail(Long id); Long create(ProductSaveReq req);
    void update(Long id,ProductSaveReq req); void delete(Long id);
}
