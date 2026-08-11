package cn.yanque.modules.prepayorders.service;
import cn.yanque.commons.apires.*; import cn.yanque.modules.prepayorders.pojo.vo.reqvo.*; import cn.yanque.modules.prepayorders.pojo.vo.resvo.PrepayOrderRes;
public interface PrepayOrderService {
    PageResult<PrepayOrderRes> page(PrepayOrderPageReq req); PrepayOrderRes detail(Long id); Long create(PrepayOrderSaveReq req);
    void update(Long id,PrepayOrderSaveReq req); void delete(Long id);
}
