package cn.yanque.modules.courses.mapper;

import cn.yanque.modules.courses.pojo.entity.SysCourseDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysCourseDetailMapper {
    List<SysCourseDetailEntity> selectByCourseId(@Param("courseId") Long courseId);
    SysCourseDetailEntity selectById(@Param("id") Long id);
    int countByCourseId(@Param("courseId") Long courseId);
    Integer selectMaxDayNumber(@Param("courseId") Long courseId);
    List<String> selectStageNames(@Param("courseId") Long courseId);
    int countByCourseIdAndStageName(@Param("courseId") Long courseId,
                                    @Param("stageName") String stageName);
    int countByCourseIdAndDayNumber(@Param("courseId") Long courseId,
                                    @Param("dayNumber") Integer dayNumber);
    int incrementDayNumbersFrom(@Param("courseId") Long courseId,
                                @Param("dayNumber") Integer dayNumber);
    int insert(SysCourseDetailEntity detail);
    int batchInsert(@Param("details") List<SysCourseDetailEntity> details);
    int updateById(SysCourseDetailEntity detail);
    int deleteById(@Param("id") Long id);
    int deleteByCourseId(@Param("courseId") Long courseId);
}
