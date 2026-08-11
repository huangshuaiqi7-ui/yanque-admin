package cn.yanque.modules.courses.mapper;

import cn.yanque.modules.courses.pojo.entity.CourseHomeworkTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseHomeworkTemplateMapper {
    List<CourseHomeworkTemplateEntity> selectByCourseId(@Param("courseId") Long courseId);
    CourseHomeworkTemplateEntity selectById(@Param("id") Long id);
    int countDimension(@Param("courseId") Long courseId,
                       @Param("teachingMode") String teachingMode,
                       @Param("stageName") String stageName,
                       @Param("dayNumber") Integer dayNumber,
                       @Param("excludeId") Long excludeId);
    int insert(CourseHomeworkTemplateEntity template);
    int updateById(CourseHomeworkTemplateEntity template);
    int deleteById(@Param("id") Long id);
}
