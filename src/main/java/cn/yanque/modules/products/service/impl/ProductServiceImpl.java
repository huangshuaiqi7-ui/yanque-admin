package cn.yanque.modules.products.service.impl;
import cn.hutool.core.util.StrUtil; import cn.yanque.commons.apires.*; import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.products.mapper.OrderProductMapper; import cn.yanque.modules.products.pojo.entity.OrderProductEntity;
import cn.yanque.modules.products.pojo.vo.reqvo.*; import cn.yanque.modules.products.pojo.vo.resvo.ProductRes; import cn.yanque.modules.products.service.ProductService;
import com.github.pagehelper.*; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.util.*;
@Service public class ProductServiceImpl implements ProductService {
    private static final Set<String> MODES=Set.of("ONLINE","OFFLINE");
    private final OrderProductMapper mapper;public ProductServiceImpl(OrderProductMapper mapper){this.mapper=mapper;}
    public PageResult<ProductRes> page(ProductPageReq req){PageHelper.startPage(req.getPageNum(),req.getPageSize());List<OrderProductEntity> rows=mapper.selectPage(StrUtil.trim(req.getKeyword()));PageInfo<OrderProductEntity> info=new PageInfo<>(rows);return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),rows.stream().map(this::toRes).toList());}
    public ProductRes detail(Long id){return toRes(require(id));}
    @Transactional public Long create(ProductSaveReq req){OrderProductEntity product=toEntity(req);if(mapper.insert(product)!=1)throw BusinessException.of(CommonErrorCode.PRODUCT_OPERATION_FAILED);return product.getId();}
    @Transactional public void update(Long id,ProductSaveReq req){require(id);OrderProductEntity product=toEntity(req);product.setId(id);if(mapper.updateById(product)!=1)throw BusinessException.of(CommonErrorCode.PRODUCT_OPERATION_FAILED);}
    @Transactional public void delete(Long id){require(id);try{if(mapper.deleteById(id)!=1)throw BusinessException.of(CommonErrorCode.PRODUCT_OPERATION_FAILED);}catch(DataIntegrityViolationException e){throw BusinessException.of(CommonErrorCode.PRODUCT_REFERENCED_BY_ORDER);}}
    private OrderProductEntity toEntity(ProductSaveReq req){String mode=StrUtil.trim(req.getTeachingMode()).toUpperCase(Locale.ROOT);if(!MODES.contains(mode))throw BusinessException.of(CommonErrorCode.PRODUCT_TEACHING_MODE_INVALID);if(req.getPrice()==null||req.getPrice().compareTo(BigDecimal.ZERO)<0)throw BusinessException.of(CommonErrorCode.PRODUCT_PRICE_INVALID);OrderProductEntity p=new OrderProductEntity();p.setCourseContent(StrUtil.trim(req.getCourseContent()));p.setTeachingMode(mode);p.setPrice(req.getPrice().setScale(2));return p;}
    private OrderProductEntity require(Long id){OrderProductEntity p=mapper.selectById(id);if(p==null)throw BusinessException.of(CommonErrorCode.PRODUCT_NOT_FOUND);return p;}
    private ProductRes toRes(OrderProductEntity p){ProductRes r=new ProductRes();r.setId(p.getId());r.setCourseContent(p.getCourseContent());r.setTeachingMode(p.getTeachingMode());r.setPrice(p.getPrice());r.setCreatedAt(p.getCreatedAt());r.setUpdatedAt(p.getUpdatedAt());return r;}
}
