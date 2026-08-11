package cn.yanque.modules.courses.service;

import cn.yanque.modules.courses.pojo.vo.reqvo.CourseHomeworkTemplateSaveReq;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseHomeworkTemplateRes;
import java.util.List;

public interface CourseHomeworkTemplateService {
    List<CourseHomeworkTemplateRes> list(Long courseId);
    CourseHomeworkTemplateRes detail(Long id);
    Long create(Long courseId, CourseHomeworkTemplateSaveReq req);
    void update(Long id, CourseHomeworkTemplateSaveReq req);
    void delete(Long id);
}
