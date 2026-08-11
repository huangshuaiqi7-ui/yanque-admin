package cn.yanque.modules.courses.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseDetailSaveReq;
import cn.yanque.modules.courses.pojo.vo.reqvo.CoursePageReq;
import cn.yanque.modules.courses.pojo.vo.reqvo.CourseSaveReq;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseDetailRes;
import cn.yanque.modules.courses.pojo.vo.resvo.CourseRes;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface SysCourseService {
    PageResult<CourseRes> page(CoursePageReq req);
    CourseRes detail(Long id);
    Long create(CourseSaveReq req);
    void update(Long id, CourseSaveReq req);
    void delete(Long id);
    List<CourseDetailRes> listDetails(Long courseId);
    List<String> listStages(Long courseId);
    CourseDetailRes detailItem(Long id);
    Long createDetail(Long courseId, CourseDetailSaveReq req);
    void updateDetail(Long id, CourseDetailSaveReq req);
    void deleteDetail(Long id);
    void importDetails(Long courseId, MultipartFile file);
}
