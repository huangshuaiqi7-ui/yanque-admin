package cn.yanque.modules.payments.mapper;

import cn.yanque.modules.payments.pojo.RefundOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface RefundOrderMapper {
    List<RefundOrderEntity> selectPage(@Param("refundOrderNo") String refundOrderNo,
                                       @Param("paymentOrderNo") String paymentOrderNo,
                                       @Param("status") String status);

    RefundOrderEntity selectByRefundOrderNo(@Param("refundOrderNo") String refundOrderNo);

    int countRefundOrderNo(@Param("refundOrderNo") String refundOrderNo);

    int insert(RefundOrderEntity entity);

    int updateProcessing(@Param("refundOrderNo") String refundOrderNo);

    int updateSuccess(@Param("refundOrderNo") String refundOrderNo,
                      @Param("uniqueRefundNo") String uniqueRefundNo,
                      @Param("refundSuccessTime") Date refundSuccessTime);

    int updateFail(@Param("refundOrderNo") String refundOrderNo, @Param("failReason") String failReason);

    int increasePaymentRefundedAmount(@Param("paymentOrderNo") String paymentOrderNo,
                                      @Param("refundAmount") BigDecimal refundAmount);
}
