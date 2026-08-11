package cn.yanque.modules.products.mapper;
import cn.yanque.modules.products.pojo.entity.OrderProductEntity;
import org.apache.ibatis.annotations.*; import java.util.List;
@Mapper public interface OrderProductMapper {
    List<OrderProductEntity> selectPage(@Param("keyword") String keyword);
    OrderProductEntity selectById(@Param("id") Long id);
    int insert(OrderProductEntity product); int updateById(OrderProductEntity product); int deleteById(@Param("id") Long id);
}
