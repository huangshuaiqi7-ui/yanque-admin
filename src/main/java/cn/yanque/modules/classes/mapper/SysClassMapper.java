package cn.yanque.modules.classes.mapper;

import cn.yanque.modules.classes.pojo.entity.SysClassEntity;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysClassMapper {
    List<ClassRes> selectPage(@Param("keyword") String keyword,
                              @Param("headTeacherId") Long headTeacherId,
                              @Param("campusId") Long campusId,
                              @Param("courseId") Long courseId);
    ClassRes selectDetailById(@Param("id") Long id);
    SysClassEntity selectById(@Param("id") Long id);
    int countByClassPeriod(@Param("classPeriod") String classPeriod, @Param("excludeId") Long excludeId);
    int countValidHeadTeacher(@Param("userId") Long userId);
    int countCampus(@Param("campusId") Long campusId);
    int countCourse(@Param("courseId") Long courseId);
    int insert(SysClassEntity clazz);
    int updateById(SysClassEntity clazz);
    int deleteById(@Param("id") Long id);
}
