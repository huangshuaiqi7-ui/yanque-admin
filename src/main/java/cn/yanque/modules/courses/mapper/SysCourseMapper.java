package cn.yanque.modules.courses.mapper;

import cn.yanque.modules.courses.pojo.entity.SysCourseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysCourseMapper {
    List<SysCourseEntity> selectPage(@Param("keyword") String keyword);
    SysCourseEntity selectById(@Param("id") Long id);
    int insert(SysCourseEntity course);
    int updateById(SysCourseEntity course);
    int deleteById(@Param("id") Long id);
    int countClassReferences(@Param("courseId") Long courseId);
}
