package cn.yanque.modules.prepayorders.mapper;
import cn.yanque.modules.prepayorders.pojo.entity.PrepayOrderEntity;
import cn.yanque.modules.prepayorders.pojo.vo.resvo.PendingPayOrderRes;
import org.apache.ibatis.annotations.*; import java.util.List;
@Mapper public interface PrepayOrderMapper {
    List<PrepayOrderEntity> selectPage(@Param("keyword") String keyword,@Param("orderStatus") String orderStatus);
    PrepayOrderEntity selectById(@Param("id") Long id);
    PendingPayOrderRes selectPendingByPhone(@Param("phone") String phone);
    PendingPayOrderRes selectPendingById(@Param("id") Long id);
    int insert(PrepayOrderEntity order); int updateById(PrepayOrderEntity order); int deleteById(@Param("id") Long id);
}
