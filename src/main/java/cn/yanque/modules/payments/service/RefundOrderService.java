package cn.yanque.modules.payments.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.payments.mapper.PaymentOrderMapper;
import cn.yanque.modules.payments.mapper.RefundOrderMapper;
import cn.yanque.modules.payments.pojo.PaymentOrderEntity;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.ApplyReq;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.ApplyRes;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.CreateRes;
import cn.yanque.modules.payments.pojo.RefundOrderDtos.PageReq;
import cn.yanque.modules.payments.pojo.RefundOrderEntity;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Service
public class RefundOrderService {
    private static final String STATUS_INIT = "INIT";
    private static final String STATUS_SUCCESS = "SUCCESS";

    private final RefundOrderMapper mapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final AlipayGateway alipayGateway;

    public RefundOrderService(RefundOrderMapper mapper,
                              PaymentOrderMapper paymentOrderMapper,
                              AlipayGateway alipayGateway) {
        this.mapper = mapper;
        this.paymentOrderMapper = paymentOrderMapper;
        this.alipayGateway = alipayGateway;
    }

    public PageInfo<RefundOrderEntity> page(PageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        return new PageInfo<>(mapper.selectPage(
                StrUtil.trim(req.getRefundOrderNo()),
                StrUtil.trim(req.getPaymentOrderNo()),
                StrUtil.trim(req.getStatus())
        ));
    }

    public CreateRes create() {
        CreateRes res = new CreateRes();
        res.setRefundOrderNo(generateRefundOrderNo());
        return res;
    }

    public ApplyRes apply(String refundOrderNo, ApplyReq req) {
        if (mapper.countRefundOrderNo(refundOrderNo) > 0) {
            fail("退款订单号已存在");
        }

        PaymentOrderEntity paymentOrder = paymentOrderMapper.selectByOrderNo(req.getPaymentOrderNo());
        if (paymentOrder == null) {
            fail("支付订单不存在");
        }
        if (!STATUS_SUCCESS.equals(paymentOrder.getStatus())) {
            fail("只有支付成功订单可以退款");
        }

        BigDecimal refundedAmount = paymentOrder.getRefundedAmount() == null ? BigDecimal.ZERO : paymentOrder.getRefundedAmount();
        BigDecimal refundableAmount = paymentOrder.getOrderAmount().subtract(refundedAmount);
        if (req.getRefundAmount().compareTo(refundableAmount) > 0) {
            fail("退款金额不能大于可退金额");
        }

        RefundOrderEntity refundOrder = new RefundOrderEntity();
        refundOrder.setRefundOrderNo(refundOrderNo);
        refundOrder.setPaymentOrderNo(paymentOrder.getOrderNo());
        refundOrder.setPaymentAmount(paymentOrder.getOrderAmount());
        refundOrder.setRefundAmount(req.getRefundAmount());
        refundOrder.setStatus(STATUS_INIT);
        refundOrder.setReason(StrUtil.trim(req.getReason()));
        Date now = new Date();
        refundOrder.setCreatedAt(now);
        refundOrder.setUpdatedAt(now);
        mapper.insert(refundOrder);

        try {
            mapper.updateProcessing(refundOrderNo);
            AlipayGateway.RefundResult result = alipayGateway.refund(
                    paymentOrder.getOrderNo(),
                    refundOrderNo,
                    req.getRefundAmount().toPlainString(),
                    req.getReason()
            );
            mapper.updateSuccess(refundOrderNo,
                    StrUtil.blankToDefault(result.getTradeNo(), paymentOrder.getUniqueOrderNo()),
                    new Date());
            mapper.increasePaymentRefundedAmount(paymentOrder.getOrderNo(), req.getRefundAmount());

            RefundOrderEntity saved = mapper.selectByRefundOrderNo(refundOrderNo);
            return BeanUtil.copyProperties(saved, ApplyRes.class);
        } catch (BusinessException e) {
            mapper.updateFail(refundOrderNo, e.getMessage());
            throw e;
        } catch (Exception e) {
            mapper.updateFail(refundOrderNo, "支付宝退款失败");
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "支付宝退款失败");
        }
    }

    private String generateRefundOrderNo() {
        for (int i = 0; i < 5; i++) {
            String refundOrderNo = "RF" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(8);
            if (mapper.countRefundOrderNo(refundOrderNo) == 0) {
                return refundOrderNo;
            }
        }
        throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "退款订单号生成失败");
    }

    private void fail(String message) {
        throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, message);
    }
}
