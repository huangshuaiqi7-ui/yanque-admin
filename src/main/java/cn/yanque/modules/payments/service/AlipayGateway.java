package cn.yanque.modules.payments.service;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.payments.pojo.PaymentOrderEntity;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AlipayGateway {
    private final AlipayClient alipayClient;
    private final Environment environment;

    public AlipayGateway(AlipayClient alipayClient, Environment environment) {
        this.alipayClient = alipayClient;
        this.environment = environment;
    }

    public String pagePay(PaymentOrderEntity order) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(required("alipay.notify-url", "支付宝异步通知地址不能为空"));
        request.setReturnUrl(required("alipay.return-url", "支付宝同步回跳地址不能为空"));

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOrderNo());
        bizContent.put("total_amount", order.getOrderAmount().toPlainString());
        bizContent.put("subject", buildSubject(order));
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        String sellerId = environment.getProperty("alipay.seller-id");
        if (StrUtil.isNotBlank(sellerId)) {
            bizContent.put("seller_id", sellerId);
        }
        request.setBizContent(bizContent.toJSONString());

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");
            if (!response.isSuccess() && StrUtil.isBlank(response.getBody())) {
                throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, alipayError("创建支付宝支付订单失败", response.getSubMsg()));
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, alipayError("创建支付宝支付订单失败", e.getMessage()));
        } catch (RuntimeException e) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, alipayError("创建支付宝支付订单失败", e.getMessage()));
        }
    }

    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    required("alipay.alipay-public-key", "支付宝公钥不能为空"),
                    AlipayConstants.CHARSET_UTF8,
                    AlipayConstants.SIGN_TYPE_RSA2
            );
        } catch (AlipayApiException e) {
            return false;
        }
    }

    /**
     * 验签只能证明报文来自支付宝，还必须确认通知确实属于当前沙箱应用和商户。
     */
    public boolean matchesMerchant(Map<String, String> params) {
        String appId = environment.getProperty("alipay.app-id");
        String sellerId = environment.getProperty("alipay.seller-id");
        return StrUtil.equals(appId, params.get("app_id"))
                && (StrUtil.isBlank(sellerId) || StrUtil.equals(sellerId, params.get("seller_id")));
    }

    public RefundResult refund(String paymentOrderNo, String refundOrderNo, String amount, String reason) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentOrderNo);
        bizContent.put("refund_amount", amount);
        bizContent.put("out_request_no", refundOrderNo);
        if (StrUtil.isNotBlank(reason)) {
            bizContent.put("refund_reason", reason);
        }
        request.setBizContent(bizContent.toJSONString());

        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED,
                        StrUtil.blankToDefault(response.getSubMsg(), "支付宝退款失败"));
            }
            RefundResult result = new RefundResult();
            result.setTradeNo(response.getTradeNo());
            return result;
        } catch (AlipayApiException e) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "支付宝退款失败");
        }
    }

    private String buildSubject(PaymentOrderEntity order) {
        if (StrUtil.isNotBlank(order.getProductContent())) {
            return StrUtil.subPre(order.getProductContent(), 128);
        }
        return "课程报名订单-" + order.getPrepayOrderNo();
    }

    private String required(String key, String message) {
        String value = environment.getProperty(key);
        if (StrUtil.isBlank(value)) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, message);
        }
        return value;
    }

    private String alipayError(String prefix, String detail) {
        if (StrUtil.isBlank(detail)) {
            return prefix;
        }
        if (detail.contains("私钥格式") || detail.contains("InvalidKey") || detail.contains("PKCS8")) {
            return prefix + "：支付宝商户私钥格式不正确，请检查 alipay.merchant-private-key 是否为应用私钥 PKCS8 格式";
        }
        return prefix + "：" + StrUtil.subPre(detail, 200);
    }

    public static class RefundResult {
        private String tradeNo;

        public String getTradeNo() {
            return tradeNo;
        }

        public void setTradeNo(String tradeNo) {
            this.tradeNo = tradeNo;
        }
    }
}
