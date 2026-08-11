package cn.yanque.modules.exams.service;
import cn.yanque.commons.apires.*; import cn.yanque.modules.exams.pojo.vo.reqvo.*; import cn.yanque.modules.exams.pojo.vo.resvo.ExamRes;
public interface ExamService {
    PageResult<ExamRes> page(ExamPageReq req); ExamRes detail(Long id); Long create(ExamSaveReq req);
    void update(Long id,ExamSaveReq req); void delete(Long id); void updateAnswerVisible(Long id,ExamAnswerVisibleReq req);
}
