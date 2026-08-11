package cn.yanque.modules.examquestions.service;
import cn.yanque.commons.apires.*;
import cn.yanque.modules.examquestions.pojo.vo.reqvo.*;
import cn.yanque.modules.examquestions.pojo.vo.resvo.ExamQuestionRes;
public interface ExamQuestionService {
    PageResult<ExamQuestionRes> page(ExamQuestionPageReq req);
    ExamQuestionRes detail(Long id);
    Long create(ExamQuestionSaveReq req);
    void update(Long id,ExamQuestionSaveReq req);
    void delete(Long id);
    void updateStatus(Long id,ExamQuestionStatusReq req);
}
