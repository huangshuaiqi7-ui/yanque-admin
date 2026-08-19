package cn.yanque.commons.apires;

import lombok.Getter;

/**
 * @ClassName CommonCode
 * @Author mrzhang
 * @Date 2026/7/17
 * @Description 统一返回状态码的实现类.
 */
@Getter
public enum CommonErrorCode implements IErrorCode{
    SUCCESS(200,"操作成功")
    ,
    FAILED(500,"服务器开小差了,请稍后再试")
    ,
    UNAUTHORIZED(401,"未授权")
    ,
    FORBIDDEN(403,"无权访问当前资源")
    ,
    NOT_FOUND(404,"访问的资源不存在"),
    PARAM_VALID_FAILED(40001, "请求参数校验失败"),

    USERNAME_OR_PASSWORD_NOT_NULL(10001, "用户名称或者是密码不能为空" ),
    USER_NOT_EXIST(10002,"用户名称和用户密码不正确" ),
    USER_NOT_ACTIVE(10003,  "用户是禁用状态" ),
    USER_DETAIL_NOT_FOUND(10004, "用户不存在"),
    USERNAME_ALREADY_EXISTS(10005, "用户名已存在"),
    USER_OPERATION_FAILED(10006, "用户操作失败"),
    USER_ROLE_INVALID(10007, "用户包含不存在的角色"),

    TOKEN_NOT_FOUND(11001, "Token不能为空"),
    TOKEN_INVALID(11002, "Token不合法或登录状态已失效"),
    TOKEN_EXPIRED(11003, "Token已过期"),

    SIGN_HEADER_MISSING(12001, "请求签名参数不能为空"),
    SIGN_TIMESTAMP_INVALID(12002, "请求时间戳不合法"),
    SIGN_REQUEST_EXPIRED(12003, "请求签名已过期"),
    SIGN_NONCE_REPEATED(12004, "请求已执行，请勿重复提交"),
    SIGN_SECRET_NOT_FOUND(12005, "请求签名密钥不存在"),
    SIGN_INVALID(12006, "请求签名校验失败"),

    ROLE_NOT_FOUND(13001, "角色不存在"),
    ROLE_CODE_EXISTS(13002, "角色编码已存在"),
    ROLE_OPERATION_FAILED(13003, "角色操作失败"),
    ROLE_PERMISSION_INVALID(13004, "角色包含不存在的权限"),

    PERMISSION_NOT_FOUND(14001, "权限不存在"),
    PERMISSION_CODE_EXISTS(14002, "权限编码已存在"),
    PERMISSION_PARENT_NOT_FOUND(14003, "父权限不存在"),
    PERMISSION_PARENT_INVALID(14004, "不能将当前权限或其子权限设置为父权限"),
    PERMISSION_HAS_CHILDREN(14005, "当前权限存在子权限，不能删除"),
    PERMISSION_API_PATH_REQUIRED(14006, "API权限的接口路径不能为空"),
    PERMISSION_OPERATION_FAILED(14007, "权限操作失败"),

    CAMPUS_NOT_FOUND(15001, "校区不存在"),
    CAMPUS_OPERATION_FAILED(15002, "校区操作失败"),

    COURSE_NOT_FOUND(16001, "课程不存在"),
    COURSE_OPERATION_FAILED(16002, "课程操作失败"),
    COURSE_REFERENCED_BY_CLASS(16003, "课程已被班级关联，不能删除"),
    COURSE_MODE_CHANGE_HAS_DETAILS(16004, "课程存在详情，不能直接修改上课方式"),
    COURSE_DAYS_LESS_THAN_DETAIL(16005, "课程天数不能小于已有详情的最大天数"),
    COURSE_DETAIL_NOT_FOUND(16006, "课程详情不存在"),
    COURSE_DETAIL_OPERATION_FAILED(16007, "课程详情操作失败"),
    ONLINE_COURSE_DETAIL_INVALID(16008, "线上课程详情只能维护阶段名称"),
    OFFLINE_COURSE_DETAIL_REQUIRED(16009, "线下课程详情必须填写第几天和上课内容"),
    COURSE_DETAIL_DAY_OUT_OF_RANGE(16010, "课程详情天数不能超过课程总天数"),
    COURSE_STAGE_NOT_FOUND(16011, "所选阶段不属于当前课程"),
    COURSE_DETAIL_SHIFT_OUT_OF_RANGE(16012, "天数顺延后将超过课程总天数，请先增加课程天数"),
    COURSE_IMPORT_FILE_EMPTY(16013, "请选择需要导入的Excel文件"),
    COURSE_IMPORT_FILE_TYPE_INVALID(16014, "只能上传xlsx或xls格式的Excel文件"),
    COURSE_IMPORT_DATA_EMPTY(16015, "Excel中没有可导入的课程详情"),
    COURSE_IMPORT_DATA_INVALID(16016, "Excel课程详情数据不合法"),
    COURSE_DETAIL_IMPORT_FAILED(16017, "课程详情导入失败"),
    COURSE_IMPORT_ONLY_OFFLINE(16018, "课程详情Excel导入仅支持线下课程"),

    CLASS_NOT_FOUND(17001, "班级不存在"),
    CLASS_OPERATION_FAILED(17002, "班级操作失败"),
    CLASS_PERIOD_EXISTS(17003, "班级期数已存在"),
    CLASS_HEAD_TEACHER_INVALID(17004, "班主任不存在、已禁用或未分配班主任角色"),
    CLASS_CAMPUS_INVALID(17005, "所选校区不存在"),
    CLASS_COURSE_INVALID(17006, "所选课程不存在"),

    CLASS_SCHEDULE_RULE_NOT_FOUND(18001, "未配置排课规则"),
    CLASS_SCHEDULE_RULE_INVALID(18002, "排课规则格式或内容不合法"),
    CLASS_SCHEDULE_FIRST_DATE_INVALID(18003, "第一天上课日期必须是非节假日且属于上课日"),
    CLASS_SCHEDULE_COURSE_DETAIL_EMPTY(18004, "班级关联课程没有课程详情，无法生成课表"),
    CLASS_SCHEDULE_GENERATE_FAILED(18005, "课表生成失败"),
    CLASS_SCHEDULE_HOLIDAY_QUERY_FAILED(18006, "法定节假日数据查询失败，请稍后重试"),
    CLASS_SCHEDULE_DATE_RANGE_INVALID(18007, "课表查询日期范围不合法"),
    CLASS_SCHEDULE_NOT_FOUND(18008, "所选日期没有课表安排"),
    CLASS_SCHEDULE_STAGE_NOT_FOUND(18009, "课程阶段不存在或尚未生成课表"),
    CLASS_SCHEDULE_STAGE_REPEATED(18010, "课程阶段不能重复分配老师"),
    CLASS_SCHEDULE_TEACHER_INVALID(18011, "老师不存在、已禁用或未分配老师角色"),
    CLASS_SCHEDULE_TEACHER_CONFLICT(18012, "老师在当前日期范围内已有其他班级课程"),
    CLASS_SCHEDULE_DATE_OCCUPIED(18013, "当前班级在所选日期已经存在正常课程"),
    CLASS_SCHEDULE_ADD_COURSE_FAILED(18014, "新增课程失败"),
    TEACHER_SCHEDULE_DATE_RANGE_INVALID(18015, "老师课表查询范围必须在1至45天之间"),
    CLASS_SCHEDULE_SHIFT_TEACHER_CONFLICT(18016, "课程重新顺延后老师与其他班级课程冲突，请先调整老师或课表"),
    CLASS_SCHEDULE_INSERT_DATE_NOT_CLASS(18017, "只能在已有正常课程的日期插入课程"),

    CLASS_DUTY_TEACHER_INVALID(19001, "值班老师不存在、已禁用或未分配老师角色"),
    CLASS_DUTY_CLASS_NOT_REQUIRED(19002, "所选班级当天不需要安排值班"),
    CLASS_DUTY_CAMPUS_NOT_REQUIRED(19003, "所选校区当天不需要安排统一值班"),
    CLASS_DUTY_TYPE_INVALID(19004, "值班类型与当天课表不匹配"),
    CLASS_DUTY_DUPLICATED(19005, "同一班级当天同类型值班不能重复"),
    CLASS_DUTY_CAMPUS_DUPLICATED(19006, "同一校区当天统一值班不能重复"),
    CLASS_DUTY_TEACHER_CONFLICT(19007, "老师在当前值班时间段已有其他值班"),
    CLASS_DUTY_SAVE_FAILED(19008, "值班保存失败"),

    CONFIG_NOT_FOUND(20001, "参数配置不存在"),
    CONFIG_KEY_EXISTS(20002, "配置键已存在"),
    CONFIG_OPERATION_FAILED(20003, "参数配置操作失败"),

    COURSE_HOMEWORK_TEMPLATE_NOT_FOUND(21001, "课程作业标准不存在"),
    COURSE_HOMEWORK_TEMPLATE_ONLINE_DIMENSION_INVALID(21002, "线上课程必须选择当前课程已有阶段，且不能填写第几天"),
    COURSE_HOMEWORK_TEMPLATE_OFFLINE_DIMENSION_INVALID(21003, "线下课程必须选择当前课程已有天数，且不能填写阶段"),
    COURSE_HOMEWORK_TEMPLATE_DOCUMENT_INVALID(21004, "作业标准必须是course/homework-template/目录下的Markdown文件"),
    COURSE_HOMEWORK_TEMPLATE_DUPLICATED(21005, "当前课程的阶段或天数已经配置作业标准"),
    COURSE_HOMEWORK_TEMPLATE_OPERATION_FAILED(21006, "课程作业标准操作失败"),

    TOS_OBJECT_KEY_INVALID(22001, "对象存储Key不合法"),
    TOS_PRESIGN_FAILED(22002, "生成对象存储预签名地址失败"),
    HOMEWORK_NOT_FOUND(23001, "作业不存在"),
    HOMEWORK_DUPLICATED(23002, "当前班级当天已经发布作业"),
    HOMEWORK_SCHEDULE_NOT_FOUND(23003, "当前班级当天没有课表，不能发布作业"),
    HOMEWORK_TIME_INVALID(23004, "作业截止时间不能早于开始时间"),
    HOMEWORK_DOCUMENT_INVALID(23005, "作业文件必须是指定目录下的Markdown文件"),
    HOMEWORK_OPERATION_FAILED(23006, "作业操作失败"),
    HOMEWORK_SUBMISSION_NOT_FOUND(23007, "作业提交记录不存在"),
    STUDENT_LOGIN_FAILED(24001, "手机号或密码错误"),
    STUDENT_NOT_ACTIVE(24002, "学生账号已禁用"),
    STUDENT_NOT_FOUND(24003, "学生不存在"),
    STUDENT_CLASS_NOT_FOUND(24004, "学生尚未分配班级"),
    STUDENT_HOMEWORK_FORBIDDEN(24005, "无权访问该作业"),
    STUDENT_HOMEWORK_NOT_STARTED(24006, "作业尚未开始"),
    STUDENT_HOMEWORK_EXPIRED(24007, "作业已截止，不能提交"),
    STUDENT_SUBMISSION_DOCUMENT_INVALID(24008, "提交文件必须是本人目录下的Markdown文件"),
    HOMEWORK_ANSWER_NOT_VISIBLE(24009, "作业答案尚未发布"),

    EXAM_QUESTION_NOT_FOUND(25001, "题目不存在"),
    EXAM_QUESTION_TYPE_INVALID(25002, "题目类型不合法"),
    EXAM_QUESTION_DIFFICULTY_INVALID(25003, "题目难度不合法"),
    EXAM_QUESTION_STATUS_INVALID(25004, "题目状态不合法"),
    EXAM_QUESTION_OPTIONS_INVALID(25005, "选择题至少需要两个不重复的有效选项"),
    EXAM_QUESTION_ANSWER_INVALID(25006, "正确答案必须是题目选项中的有效选项"),
    EXAM_QUESTION_NON_CHOICE_OPTIONS_FORBIDDEN(25007, "非选择题不能维护选项"),
    EXAM_QUESTION_COURSE_STAGE_INVALID(25008, "关联的课程或阶段不存在"),
    EXAM_QUESTION_COURSE_REPEATED(25009, "同一道题在同一课程下只能关联一个阶段"),
    EXAM_QUESTION_OPERATION_FAILED(25010, "题目操作失败"),

    EXAM_PAPER_NOT_FOUND(26001, "试卷不存在"),
    EXAM_PAPER_COURSE_INVALID(26002, "试卷关联的课程不存在"),
    EXAM_PAPER_STAGE_INVALID(26003, "试卷阶段不属于所选课程"),
    EXAM_PAPER_QUESTION_EMPTY(26004, "试卷至少需要一道题目"),
    EXAM_PAPER_QUESTION_REPEATED(26005, "试卷中存在重复题目"),
    EXAM_PAPER_QUESTION_INVALID(26006, "题目不存在或未启用"),
    EXAM_PAPER_QUESTION_SCOPE_INVALID(26007, "题目不属于试卷关联的课程或阶段"),
    EXAM_PAPER_SCORE_INVALID(26008, "题目分值必须大于0"),
    EXAM_PAPER_TOTAL_SCORE_MISMATCH(26009, "题目分值合计必须等于试卷总分"),
    EXAM_PAPER_OPERATION_FAILED(26010, "试卷操作失败"),

    EXAM_NOT_FOUND(27001, "考试安排不存在"),
    EXAM_TIME_INVALID(27002, "考试开始时间必须早于截止时间"),
    EXAM_PAPER_INVALID(27003, "试卷不存在"),
    EXAM_CLASS_INVALID(27004, "班级不存在"),
    EXAM_INVIGILATOR_INVALID(27005, "监考老师不存在或已禁用"),
    EXAM_CLASS_TIME_CONFLICT(27006, "当前班级在所选时间范围内已有考试"),
    EXAM_OPERATION_FAILED(27007, "考试安排操作失败"),

    STUDENT_EXAM_FORBIDDEN(28001, "无权参加该考试"),
    STUDENT_EXAM_NOT_AVAILABLE(28002, "当前不在考试可进入时间范围内"),
    STUDENT_EXAM_RECORD_NOT_FOUND(28003, "学生考试记录不存在"),
    STUDENT_EXAM_ALREADY_SUBMITTED(28004, "考试已经提交，不能重复操作"),
    STUDENT_EXAM_TIMEOUT(28005, "考试答题时间已结束"),
    STUDENT_EXAM_ANSWER_INVALID(28006, "提交的题目答案不合法"),
    STUDENT_EXAM_OPERATION_FAILED(28007, "学生考试操作失败"),
    EXAM_GRADE_OBJECTIVE_FORBIDDEN(28008, "客观题由系统自动判分，不能手动修改"),
    EXAM_GRADE_SCORE_INVALID(28009, "主观题得分不能小于0或超过题目分值"),
    EXAM_GRADE_ANSWER_INVALID(28010, "批改答案不属于当前答卷"),

    PRODUCT_NOT_FOUND(29001, "产品不存在"),
    PRODUCT_TEACHING_MODE_INVALID(29002, "产品上课方式只能是ONLINE或OFFLINE"),
    PRODUCT_PRICE_INVALID(29003, "产品价格不合法"),
    PRODUCT_OPERATION_FAILED(29004, "产品操作失败"),
    PRODUCT_REFERENCED_BY_ORDER(29005, "产品已被订单引用，不能删除"),

    PREPAY_ORDER_NOT_FOUND(30001, "预支付订单不存在"),
    PREPAY_ORDER_PRODUCT_INVALID(30002, "订单关联的产品不存在"),
    PREPAY_ORDER_STATUS_INVALID(30003, "预支付订单状态不合法"),
    PREPAY_ORDER_AMOUNT_INVALID(30004, "优惠金额不能小于0或超过产品金额"),
    PREPAY_ORDER_OPERATION_FAILED(30005, "预支付订单操作失败"),
    PREPAY_ORDER_NO_GENERATE_FAILED(30006, "预支付订单号生成失败"),

    KNOWLEDGE_BASE_NOT_FOUND(31001, "知识库不存在"),
    KNOWLEDGE_BASE_CODE_EXISTS(31002, "知识库编码已存在"),
    KNOWLEDGE_BASE_STATUS_INVALID(31003, "知识库状态不合法"),
    KNOWLEDGE_BASE_OPERATION_FAILED(31005, "知识库操作失败"),
    KNOWLEDGE_BASE_VECTOR_INIT_FAILED(31006, "知识库向量表初始化失败"),
    KNOWLEDGE_BASE_VECTOR_DELETE_FAILED(31007, "知识库向量表删除失败"),
    KNOWLEDGE_DOCUMENT_NOT_FOUND(31101, "知识库文档不存在"),
    KNOWLEDGE_DOCUMENT_CODE_EXISTS(31102, "知识库文档编码已存在"),
    KNOWLEDGE_DOCUMENT_NAME_EXISTS(31103, "知识库文档名称已存在"),
    KNOWLEDGE_DOCUMENT_STATUS_INVALID(31104, "知识库文档状态不合法"),
    KNOWLEDGE_DOCUMENT_FILE_INVALID(31105, "知识库文档只支持指定目录下的Markdown文件"),
    KNOWLEDGE_DOCUMENT_OPERATION_FAILED(31106, "知识库文档操作失败"),
    KNOWLEDGE_DOCUMENT_VECTOR_INDEX_FAILED(31107, "知识库文档向量入库失败"),
    KNOWLEDGE_DOCUMENT_VECTOR_DELETE_FAILED(31108, "知识库文档向量删除失败"),
    KNOWLEDGE_RECALL_MODE_INVALID(31201, "知识库召回模式不合法"),
    KNOWLEDGE_RECALL_FAILED(31202, "知识库召回失败"),

    PROMPT_TEMPLATE_NOT_FOUND(32001, "提示词模板不存在"),
    PROMPT_TEMPLATE_CODE_EXISTS(32002, "提示词模板编码已存在"),
    PROMPT_TEMPLATE_STATUS_INVALID(32003, "提示词模板状态不合法"),
    PROMPT_TEMPLATE_TYPE_INVALID(32004, "提示词类型不合法"),
    PROMPT_TEMPLATE_SCENE_INVALID(32005, "提示词场景不合法"),
    PROMPT_TEMPLATE_OPERATION_FAILED(32006, "提示词模板操作失败"),
    PROMPT_TEMPLATE_VERSION_NOT_FOUND(32101, "提示词版本不存在"),
    PROMPT_TEMPLATE_VERSION_CONTENT_EMPTY(32102, "提示词版本内容不能为空"),
    PROMPT_TEMPLATE_VERSION_VARIABLES_INVALID(32103, "提示词版本变量说明不是合法JSON"),
    PROMPT_TEMPLATE_VERSION_OPERATION_FAILED(32104, "提示词版本操作失败"),
    PROMPT_ACTIVE_VERSION_NOT_FOUND(32105, "提示词当前启用版本不存在"),
    PROMPT_TEST_INVALID(32201, "提示词测试参数不合法"),
    PROMPT_TEST_FAILED(32202, "提示词测试失败");


    private Integer code;
    private String message;


    //构造器.
    CommonErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }



}
