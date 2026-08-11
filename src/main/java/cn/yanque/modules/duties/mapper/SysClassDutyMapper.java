package cn.yanque.modules.duties.mapper;

import cn.yanque.modules.duties.pojo.entity.SysClassDutyEntity;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutyDateCampusRes;
import cn.yanque.modules.duties.pojo.vo.resvo.ClassDutyDateClassRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SysClassDutyMapper {
    List<ClassDutyDateClassRes> selectDateClasses(@Param("dutyDate") LocalDate dutyDate);
    List<ClassDutyDateCampusRes> selectDateCampuses(@Param("dutyDate") LocalDate dutyDate);
    int countValidTeacher(@Param("teacherId") Long teacherId);
    int countCampusCandidate(@Param("campusId") Long campusId, @Param("dutyDate") LocalDate dutyDate);
    int deleteByDutyDate(@Param("dutyDate") LocalDate dutyDate);
    int batchInsert(@Param("duties") List<SysClassDutyEntity> duties);
}
