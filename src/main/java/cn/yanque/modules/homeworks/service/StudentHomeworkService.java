package cn.yanque.modules.homeworks.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWTUtil;
import cn.yanque.commons.apires.*;
import cn.yanque.commons.constant.JwtConstants;
import cn.yanque.commons.context.UserContext;
import cn.yanque.commons.enums.CommonStatusEnum;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.commons.pojo.vo.resvo.*;
import cn.yanque.commons.service.TosPresignService;
import cn.yanque.commons.utils.RedisUtils;
import cn.yanque.modules.homeworks.mapper.HomeworkMapper;
import cn.yanque.modules.homeworks.pojo.entity.*;
import cn.yanque.modules.homeworks.pojo.vo.reqvo.*;
import cn.yanque.modules.homeworks.pojo.vo.resvo.*;
import cn.yanque.modules.students.mapper.StudentMapper;
import cn.yanque.modules.students.pojo.entity.StudentEntity;
import cn.yanque.modules.prepayorders.mapper.PrepayOrderMapper;
import cn.yanque.modules.prepayorders.pojo.vo.resvo.PendingPayOrderRes;
import com.github.pagehelper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StudentHomeworkService {
    private static final SecureRandom RANDOM=new SecureRandom();
    private final StudentMapper studentMapper; private final HomeworkMapper homeworkMapper;
    private final RedisUtils redis; private final TosPresignService tos; private final PrepayOrderMapper prepayOrderMapper;
    public StudentHomeworkService(StudentMapper studentMapper,HomeworkMapper homeworkMapper,RedisUtils redis,TosPresignService tos,PrepayOrderMapper prepayOrderMapper){this.studentMapper=studentMapper;this.homeworkMapper=homeworkMapper;this.redis=redis;this.tos=tos;this.prepayOrderMapper=prepayOrderMapper;}
    public StudentLoginRes login(StudentLoginReq req){
        StudentEntity s=studentMapper.selectByPhone(req.getPhone());
        if(s==null) {
            return pendingPayLogin(req);
        }
        if(!req.getPassword().equals(s.getPassword())) throw BusinessException.of(CommonErrorCode.STUDENT_LOGIN_FAILED);
        if(!CommonStatusEnum.ACTIVE.name().equals(s.getStatus())) throw BusinessException.of(CommonErrorCode.STUDENT_NOT_ACTIVE);
        Map<String,Object> claims=new HashMap<>(); claims.put(JwtConstants.JWT_CLAIM_USER_ID,s.getId());
        claims.put(JwtConstants.JWT_CLAIM_EXPIRE_TIME,System.currentTimeMillis()+JwtConstants.LOGIN_TOKEN_TTL.toMillis());
        claims.put(JwtConstants.JWT_CLAIM_ID,IdUtil.simpleUUID()); claims.put(JwtConstants.JWT_CLAIM_SUBJECT_TYPE,JwtConstants.JWT_SUBJECT_STUDENT);
        String token=JWTUtil.createToken(claims,JwtConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        byte[] bytes=new byte[32]; RANDOM.nextBytes(bytes); String secret=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String id=String.valueOf(s.getId()); redis.set(JwtConstants.STUDENT_JWT_TOKEN_KEY_PREFIX+id,token,JwtConstants.LOGIN_TOKEN_TTL);
        redis.set(JwtConstants.STUDENT_SIGN_SECRET_KEY_PREFIX+id,secret,JwtConstants.LOGIN_TOKEN_TTL);
        StudentLoginRes res=new StudentLoginRes(); res.setToken(token);res.setSignSecret(secret);
        res.setStudent(new StudentLoginRes.StudentInfo(s.getId(),s.getStudentName(),s.getStudentPhone())); return res;
    }
    private StudentLoginRes pendingPayLogin(StudentLoginReq req){
        if(!"123456".equals(req.getPassword())) throw BusinessException.of(CommonErrorCode.STUDENT_LOGIN_FAILED);
        PendingPayOrderRes order=prepayOrderMapper.selectPendingByPhone(req.getPhone());
        if(order==null) throw BusinessException.of(CommonErrorCode.STUDENT_LOGIN_FAILED);
        String pendingToken=randomSecret();
        String pendingSecret=randomSecret();
        redis.set(JwtConstants.PENDING_PAY_TOKEN_KEY_PREFIX+pendingToken,String.valueOf(order.getId()),JwtConstants.PENDING_PAY_TOKEN_TTL);
        redis.set(JwtConstants.PENDING_PAY_SECRET_KEY_PREFIX+pendingToken,pendingSecret,JwtConstants.PENDING_PAY_TOKEN_TTL);
        StudentLoginRes res=new StudentLoginRes();res.setNeedPay(true);res.setPendingPayToken(pendingToken);
        res.setPendingPaySignSecret(pendingSecret);res.setPendingOrder(order);
        res.setStudent(new StudentLoginRes.StudentInfo(null,order.getStudentName(),order.getStudentPhone()));
        return res;
    }
    private String randomSecret(){byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
    public PageResult<StudentHomeworkRes> page(int pageNum,int pageSize){
        StudentEntity s=currentStudent(); requireClass(s); PageHelper.startPage(pageNum,pageSize);
        List<StudentHomeworkRes> list=homeworkMapper.selectStudentHomeworks(s.getClassId(),s.getId(),LocalDateTime.now());
        PageInfo<StudentHomeworkRes> info=new PageInfo<>(list); return new PageResult<>(info.getTotal(),info.getPageNum(),info.getPageSize(),list);
    }
    public PresignDownloadRes homeworkDownload(Long homeworkId,String type){
        StudentEntity s=currentStudent(); HomeworkEntity h=accessibleHomework(s,homeworkId,true); String key; 
        if("ANSWER".equalsIgnoreCase(type)){
            if(!Boolean.TRUE.equals(h.getAnswerStudentVisible())||h.getAnswerObjectKey()==null) throw BusinessException.of(CommonErrorCode.HOMEWORK_ANSWER_NOT_VISIBLE);
            key=h.getAnswerObjectKey();
        } else if("CONTENT".equalsIgnoreCase(type)) key=h.getContentObjectKey(); else throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED);
        return tos.presignDownload(key);
    }
    public PresignDownloadRes submissionDownload(Long homeworkId){
        StudentEntity s=currentStudent(); accessibleHomework(s,homeworkId,true);
        HomeworkSubmissionEntity sub=homeworkMapper.selectSubmission(homeworkId,s.getId());
        if(sub==null) throw BusinessException.of(CommonErrorCode.HOMEWORK_SUBMISSION_NOT_FOUND); return tos.presignDownload(sub.getContentObjectKey());
    }
    public PresignUploadRes presignSubmission(String objectKey){
        StudentEntity s=currentStudent(); Long homeworkId=parseSubmissionKey(objectKey,s.getId()); accessibleHomework(s,homeworkId,false);
        return tos.presignUpload(objectKey);
    }
    @Transactional
    public StudentSubmissionRes submit(Long homeworkId,StudentSubmissionReq req){
        StudentEntity s=currentStudent(); HomeworkEntity h=accessibleHomework(s,homeworkId,false);
        String prefix="homework/submission/"+homeworkId+"/"+s.getId()+"/"; validateSubmissionDocument(req.getObjectKey(),req.getFileName(),prefix);
        LocalDateTime now=LocalDateTime.now(); HomeworkSubmissionEntity sub=homeworkMapper.selectSubmission(homeworkId,s.getId());
        if(sub==null){ sub=new HomeworkSubmissionEntity();sub.setHomeworkId(homeworkId);sub.setStudentId(s.getId());sub.setClassId(s.getClassId());sub.setContentObjectKey(req.getObjectKey());sub.setContentFileName(req.getFileName());sub.setSubmitTime(now);sub.setLateSubmitted(false);homeworkMapper.insertSubmission(sub); }
        else {sub.setContentObjectKey(req.getObjectKey());sub.setContentFileName(req.getFileName());sub.setSubmitTime(now);sub.setLateSubmitted(false);homeworkMapper.updateSubmissionFile(sub);}
        StudentSubmissionRes res=new StudentSubmissionRes();res.setId(sub.getId());res.setHomeworkId(homeworkId);res.setContentFileName(req.getFileName());res.setSubmitTime(now);res.setLateSubmitted(false);return res;
    }
    private StudentEntity currentStudent(){Long id=UserContext.getUserId();StudentEntity s=id==null?null:studentMapper.selectById(id);if(s==null)throw BusinessException.of(CommonErrorCode.STUDENT_NOT_FOUND);if(!CommonStatusEnum.ACTIVE.name().equals(s.getStatus()))throw BusinessException.of(CommonErrorCode.STUDENT_NOT_ACTIVE);return s;}
    private void requireClass(StudentEntity s){if(s.getClassId()==null)throw BusinessException.of(CommonErrorCode.STUDENT_CLASS_NOT_FOUND);}
    private HomeworkEntity accessibleHomework(StudentEntity s,Long id,boolean download){requireClass(s);HomeworkEntity h=homeworkMapper.selectById(id);if(h==null)throw BusinessException.of(CommonErrorCode.HOMEWORK_NOT_FOUND);if(!s.getClassId().equals(h.getClassId()))throw BusinessException.of(CommonErrorCode.STUDENT_HOMEWORK_FORBIDDEN);LocalDateTime now=LocalDateTime.now();if(now.isBefore(h.getStartTime()))throw BusinessException.of(CommonErrorCode.STUDENT_HOMEWORK_NOT_STARTED);if(!download&&now.isAfter(h.getDeadline()))throw BusinessException.of(CommonErrorCode.STUDENT_HOMEWORK_EXPIRED);return h;}
    private Long parseSubmissionKey(String key,Long studentId){String[] p=key==null?new String[0]:key.split("/");try{if(p.length<6||!"homework".equals(p[0])||!"submission".equals(p[1])||!String.valueOf(studentId).equals(p[3]))throw new Exception();return Long.valueOf(p[2]);}catch(Exception e){throw BusinessException.of(CommonErrorCode.STUDENT_SUBMISSION_DOCUMENT_INVALID);}}
    private void validateSubmissionDocument(String key,String name,String prefix){if(key==null||name==null||!key.startsWith(prefix)||!key.toLowerCase().endsWith(".md")||!name.toLowerCase().endsWith(".md")||key.contains("..")||key.contains("\\"))throw BusinessException.of(CommonErrorCode.STUDENT_SUBMISSION_DOCUMENT_INVALID);}
}
