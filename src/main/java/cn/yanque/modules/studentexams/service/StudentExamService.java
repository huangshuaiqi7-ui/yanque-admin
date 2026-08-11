package cn.yanque.modules.studentexams.service;
import cn.yanque.commons.apires.*; import cn.yanque.modules.studentexams.pojo.vo.reqvo.*; import cn.yanque.modules.studentexams.pojo.vo.resvo.*;
public interface StudentExamService {
    PageResult<StudentExamRes> myExams(int pageNum,int pageSize); StudentExamStartRes start(Long examId);
    StudentExamPaperRes paper(Long recordId); StudentExamSubmitRes submit(Long recordId,StudentExamSubmitReq req);
    ExamSubmissionDetailRes submission(Long recordId);
}
