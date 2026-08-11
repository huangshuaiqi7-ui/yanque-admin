package cn.yanque.modules.studentexams.service;
import cn.yanque.commons.apires.*; import cn.yanque.modules.studentexams.pojo.vo.reqvo.ExamGradeReq; import cn.yanque.modules.studentexams.pojo.vo.resvo.*;
public interface AdminExamSubmissionService {
    PageResult<ExamSubmissionListRes> page(Long examId,int pageNum,int pageSize);
    ExamSubmissionDetailRes detail(Long recordId); ExamSubmissionDetailRes grade(Long recordId,ExamGradeReq req);
}
