package cn.yanque.modules.payments.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.utils.RedisUtils;
import cn.yanque.modules.students.pojo.entity.StudentEntity;
import cn.yanque.modules.payments.mapper.PaymentOrderMapper;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CompleteProfileReq;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CompleteProfileRes;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CreateOrderNoRes;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CreatePaymentOrderReq;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.CreatePaymentOrderRes;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.PageReq;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.PaymentReturnInfo;
import cn.yanque.modules.payments.pojo.PaymentOrderDtos.StudentInfo;
import cn.yanque.modules.payments.pojo.PaymentOrderEntity;
import cn.yanque.modules.prepayorders.mapper.PrepayOrderMapper;
import cn.yanque.modules.prepayorders.pojo.vo.resvo.PendingPayOrderRes;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class PaymentOrderService {
    private static final String STATUS_INIT = "INIT";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String PREPAY_PENDING = "PENDING_PAYMENT";

    private final PaymentOrderMapper mapper;
    private final PrepayOrderMapper prepayOrderMapper;
    private final AlipayGateway alipayGateway;
    private final RedisUtils redis;

    public PaymentOrderService(PaymentOrderMapper mapper,
                               PrepayOrderMapper prepayOrderMapper,
                               AlipayGateway alipayGateway,
                               RedisUtils redis) {
        this.mapper = mapper;
        this.prepayOrderMapper = prepayOrderMapper;
        this.alipayGateway = alipayGateway;
        this.redis = redis;
    }

    public PageInfo<PaymentOrderEntity> page(PageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        return new PageInfo<>(mapper.selectPage(
                StrUtil.trim(req.getOrderNo()),
                StrUtil.trim(req.getStudentName()),
                StrUtil.trim(req.getStudentPhone()),
                StrUtil.trim(req.getPrepayOrderNo()),
                StrUtil.trim(req.getStatus())
        ));
    }

    public CreateOrderNoRes createOrderNo() {
        CreateOrderNoRes res = new CreateOrderNoRes();
        res.setOrderNo(generateOrderNo());
        return res;
    }

    public CreatePaymentOrderRes createPaymentOrder(CreatePaymentOrderReq req) {
        PendingPayOrderRes prepayOrder = currentPendingOrder();
        if (prepayOrder == null || !Objects.equals(prepayOrder.getOrderNo(), req.getPrepayOrderNo())) {
            fail("待支付订单不存在");
        }
        if (!PREPAY_PENDING.equals(prepayOrder.getOrderStatus())) {
            fail("预支付订单不是待支付状态");
        }
        if (!Objects.equals(prepayOrder.getStudentName(), StrUtil.trim(req.getStudentName()))) {
            fail("学生姓名与预支付订单不一致");
        }
        if (!Objects.equals(String.valueOf(prepayOrder.getProductId()), req.getProductId())) {
            fail("产品信息与预支付订单不一致");
        }

        BigDecimal payableAmount = prepayOrder.getProductAmount().subtract(prepayOrder.getDiscountAmount());
        if (payableAmount.compareTo(req.getOrderAmount()) != 0) {
            fail("支付金额与预支付订单应付金额不一致");
        }
        if (mapper.countOrderNo(req.getOrderNo()) > 0) {
            fail("支付订单号已存在");
        }

        PaymentOrderEntity order = new PaymentOrderEntity();
        order.setOrderNo(StrUtil.trim(req.getOrderNo()));
        order.setStudentPhone(StrUtil.trim(req.getStudentPhone()));
        order.setStudentName(StrUtil.trim(req.getStudentName()));
        order.setProductId(req.getProductId());
        order.setProductContent(prepayOrder.getProductContent());
        order.setOrderAmount(req.getOrderAmount());
        order.setRefundedAmount(BigDecimal.ZERO);
        order.setPrepayOrderNo(req.getPrepayOrderNo());
        order.setStatus(STATUS_INIT);
        Date now = new Date();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        mapper.insert(order);

        try {
            String cashierUrl = alipayGateway.pagePay(order);
            mapper.updateProcessing(order.getOrderNo());
            CreatePaymentOrderRes res = new CreatePaymentOrderRes();
            res.setCashierUrl(cashierUrl);
            return res;
        } catch (BusinessException e) {
            mapper.updateFail(order.getOrderNo());
            throw e;
        } catch (Exception e) {
            mapper.updateFail(order.getOrderNo());
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "创建支付宝支付订单失败");
        }
    }

    public PaymentReturnInfo paymentReturnInfo(String orderNo) {
        PaymentOrderEntity order = getOrder(orderNo);
        assertCurrentPendingOrder(order);
        return BeanUtil.copyProperties(order, PaymentReturnInfo.class);
    }

    @Transactional
    public CompleteProfileRes completeProfile(CompleteProfileReq req) {
        if (!Objects.equals(req.getPassword(), req.getConfirmPassword())) {
            fail("两次输入的密码不一致");
        }
        PaymentOrderEntity order = getOrder(req.getOrderNo());
        assertCurrentPendingOrder(order);
        if (!STATUS_SUCCESS.equals(order.getStatus())) {
            fail("支付订单尚未支付成功，请稍后再试");
        }

        StudentEntity student = mapper.selectStudentByPhone(order.getStudentPhone());
        if (student == null) {
            student = new StudentEntity();
            student.setStudentNo(generateStudentNo());
            student.setStudentName(order.getStudentName());
            student.setStudentPhone(order.getStudentPhone());
            student.setStatus("ACTIVE");
        }
        student.setPassword(req.getPassword());
        student.setEducation(StrUtil.trim(req.getEducation()));
        student.setGradeYear(req.getGradeYear());
        student.setSchool(StrUtil.trim(req.getSchool()));
        student.setMajor(StrUtil.trim(req.getMajor()));
        student.setTeachingMode(StrUtil.blankToDefault(order.getTeachingMode(), "ONLINE"));

        if (student.getId() == null) {
            mapper.insertStudent(student);
        } else {
            mapper.updateStudentProfile(student);
        }

        if (mapper.countStudentProduct(student.getId(), order.getProductId(), order.getOrderNo()) == 0) {
            mapper.insertStudentProduct(student.getId(), order.getProductId(), order.getOrderNo());
        }

        CompleteProfileRes res = new CompleteProfileRes();
        res.setStudentId(student.getId());
        res.setCompleted(true);
        res.setToken(createStudentToken(student.getId()));
        res.setSignSecret(createSignSecret());
        redis.set(JwtConstants.STUDENT_JWT_TOKEN_KEY_PREFIX + student.getId(),
                res.getToken(), JwtConstants.LOGIN_TOKEN_TTL);
        redis.set(JwtConstants.STUDENT_SIGN_SECRET_KEY_PREFIX + student.getId(),
                res.getSignSecret(), JwtConstants.LOGIN_TOKEN_TTL);

        StudentInfo info = new StudentInfo();
        info.setId(student.getId());
        info.setName(order.getStudentName());
        info.setPhone(order.getStudentPhone());
        res.setStudent(info);
        return res;
    }

    @Transactional
    public void handleAlipayNotify(Map<String, String> params) {
        if (!alipayGateway.verifyNotify(params)) {
            fail("支付宝异步通知验签失败");
        }
        if (!alipayGateway.matchesMerchant(params)) {
            fail("支付宝异步通知的应用或商户不匹配");
        }

        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return;
        }

        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        PaymentOrderEntity order = getOrder(orderNo);

        String totalAmount = params.get("total_amount");
        if (StrUtil.isBlank(totalAmount) || order.getOrderAmount().compareTo(new BigDecimal(totalAmount)) != 0) {
            fail("支付宝异步通知金额不一致");
        }

        Date payTime = parsePayTime(params.get("gmt_payment"));
        mapper.updateSuccess(orderNo, tradeNo, payTime);
        mapper.updatePrepayPaid(order.getPrepayOrderNo());
    }

    private PaymentOrderEntity getOrder(String orderNo) {
        if (StrUtil.isBlank(orderNo)) {
            fail("支付订单号不能为空");
        }
        PaymentOrderEntity order = mapper.selectByOrderNo(StrUtil.trim(orderNo));
        if (order == null) {
            fail("支付订单不存在");
        }
        return order;
    }

    private PendingPayOrderRes currentPendingOrder() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            fail("无法获取当前待支付会话");
        }
        String token = attributes.getRequest().getHeader(JwtConstants.PENDING_PAY_TOKEN_HEADER);
        String id = redis.get(JwtConstants.PENDING_PAY_TOKEN_KEY_PREFIX + token);
        if (StrUtil.isBlank(id)) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
        try {
            PendingPayOrderRes order = prepayOrderMapper.selectPendingById(Long.valueOf(id));
            if (order == null) {
                fail("待支付订单不存在");
            }
            return order;
        } catch (NumberFormatException e) {
            throw BusinessException.of(CommonErrorCode.TOKEN_INVALID);
        }
    }

    private void assertCurrentPendingOrder(PaymentOrderEntity paymentOrder) {
        PendingPayOrderRes pendingOrder = currentPendingOrder();
        if (!Objects.equals(pendingOrder.getOrderNo(), paymentOrder.getPrepayOrderNo())) {
            throw BusinessException.of(CommonErrorCode.FORBIDDEN, "无权访问该支付订单");
        }
    }

    private Date parsePayTime(String value) {
        if (StrUtil.isBlank(value)) {
            return new Date();
        }
        try {
            return DateUtil.parse(value, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            return new Date();
        }
    }

    private String generateOrderNo() {
        for (int i = 0; i < 5; i++) {
            String orderNo = "PAY" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(8);
            if (mapper.countOrderNo(orderNo) == 0) {
                return orderNo;
            }
        }
        throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "支付订单号生成失败");
    }

    private String generateStudentNo() {
        for (int i = 0; i < 5; i++) {
            String studentNo = "ST" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6);
            if (mapper.countStudentNo(studentNo) == 0) {
                return studentNo;
            }
        }
        throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "学员编号生成失败");
    }

    private String createStudentToken(Long studentId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(JwtConstants.JWT_CLAIM_USER_ID, studentId);
        payload.put(JwtConstants.JWT_CLAIM_EXPIRE_TIME,
                System.currentTimeMillis() + JwtConstants.LOGIN_TOKEN_TTL.toMillis());
        payload.put(JwtConstants.JWT_CLAIM_ID, UUID.fastUUID().toString(true));
        payload.put(JwtConstants.JWT_CLAIM_SUBJECT_TYPE, JwtConstants.JWT_SUBJECT_STUDENT);
        return JWTUtil.createToken(payload, JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String createSignSecret() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
    }

    private void fail(String message) {
        throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, message);
    }
}
