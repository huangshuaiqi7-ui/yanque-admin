package cn.yanque.modules.exampapers.service;
import cn.yanque.commons.apires.*; import cn.yanque.modules.exampapers.pojo.vo.reqvo.*; import cn.yanque.modules.exampapers.pojo.vo.resvo.ExamPaperRes;
public interface ExamPaperService {
    PageResult<ExamPaperRes> page(ExamPaperPageReq req); ExamPaperRes detail(Long id); Long create(ExamPaperSaveReq req); void delete(Long id);
}
