package cn.yanque.modules.prepayorders.service.impl;
import cn.hutool.core.util.*; import cn.yanque.commons.apires.*; import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.prepayorders.mapper.PrepayOrderMapper; import cn.yanque.modules.prepayorders.pojo.entity.PrepayOrderEntity;
import cn.yanque.modules.prepayorders.pojo.vo.reqvo.*; import cn.yanque.modules.prepayorders.pojo.vo.resvo.PrepayOrderRes; import cn.yanque.modules.prepayorders.service.PrepayOrderService;
import cn.yanque.modules.products.mapper.OrderProductMapper; import cn.yanque.modules.products.pojo.entity.OrderProductEntity;
import com.github.pagehelper.*; import org.springframework.dao.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.time.format.DateTimeFormatter; import java.util.*;
@Service public class PrepayOrderServiceImpl implements PrepayOrderService {
    private static final Set<String> STATUSES=Set.of("PENDING_PAYMENT","PAID","CANCELED");
    private static final DateTimeFormatter ORDER_TIME=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final PrepayOrderMapper mapper;private final OrderProductMapper productMapper;
    public PrepayOrderServiceImpl(PrepayOrderMapper mapper,OrderProductMapper productMapper){this.mapper=mapper;this.productMapper=productMapper;}
    public PageResult<PrepayOrderRes> page(PrepayOrderPageReq req){String status=normalizeStatus(req.getOrderStatus(),true);PageHelper.startPage(req.getPageNum(),req.getPageSize());List<PrepayOrderEntity> rows=mapper.selectPage(StrUtil.trim(req.getKeyword()),status);PageInfo<PrepayOrderEntity> info=new PageInfo<>(rows);return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),rows.stream().map(this::toRes).toList());}
    public PrepayOrderRes detail(Long id){return toRes(require(id));}
    @Transactional public Long create(PrepayOrderSaveReq req){
        PrepayOrderEntity order=toEntity(req);order.setOrderNo(generateOrderNo());order.setOrderStatus("PENDING_PAYMENT");
        try{if(mapper.insert(order)!=1)throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_OPERATION_FAILED);}catch(DuplicateKeyException e){throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_NO_GENERATE_FAILED);}
        return order.getId();
    }
    @Transactional public void update(Long id,PrepayOrderSaveReq req){PrepayOrderEntity old=require(id);PrepayOrderEntity order=toEntity(req);order.setId(id);order.setOrderNo(old.getOrderNo());order.setOrderStatus(normalizeStatus(req.getOrderStatus(),false));if(mapper.updateById(order)!=1)throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_OPERATION_FAILED);}
    @Transactional public void delete(Long id){require(id);try{if(mapper.deleteById(id)!=1)throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_OPERATION_FAILED);}catch(DataIntegrityViolationException e){throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_OPERATION_FAILED,"预支付订单已经关联支付记录，不能删除");}}
    private PrepayOrderEntity toEntity(PrepayOrderSaveReq req){OrderProductEntity product=productMapper.selectById(req.getProductId());if(product==null)throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_PRODUCT_INVALID);BigDecimal discount=req.getDiscountAmount();if(discount==null||discount.compareTo(BigDecimal.ZERO)<0||discount.compareTo(product.getPrice())>0)throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_AMOUNT_INVALID);PrepayOrderEntity o=new PrepayOrderEntity();o.setStudentName(StrUtil.trim(req.getStudentName()));o.setStudentPhone(StrUtil.trim(req.getStudentPhone()));o.setProductId(product.getId());o.setProductAmount(product.getPrice().setScale(2));o.setDiscountAmount(discount.setScale(2));return o;}
    private String generateOrderNo(){return "PO"+LocalDateTime.now().format(ORDER_TIME)+IdUtil.fastSimpleUUID().substring(0,16).toUpperCase(Locale.ROOT);}
    private String normalizeStatus(String value,boolean allowBlank){String status=StrUtil.trim(value);if(StrUtil.isBlank(status)){if(allowBlank)return null;throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_STATUS_INVALID);}status=status.toUpperCase(Locale.ROOT);if(!STATUSES.contains(status))throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_STATUS_INVALID);return status;}
    private PrepayOrderEntity require(Long id){PrepayOrderEntity o=mapper.selectById(id);if(o==null)throw BusinessException.of(CommonErrorCode.PREPAY_ORDER_NOT_FOUND);return o;}
    private PrepayOrderRes toRes(PrepayOrderEntity o){PrepayOrderRes r=new PrepayOrderRes();r.setId(o.getId());r.setOrderNo(o.getOrderNo());r.setStudentName(o.getStudentName());r.setStudentPhone(o.getStudentPhone());r.setProductId(o.getProductId());r.setProductAmount(o.getProductAmount());r.setDiscountAmount(o.getDiscountAmount());r.setOrderStatus(o.getOrderStatus());r.setCreatedAt(o.getCreatedAt());r.setUpdatedAt(o.getUpdatedAt());return r;}
}
