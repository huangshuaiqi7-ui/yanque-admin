package cn.yanque.modules.students.mapper;

import cn.yanque.modules.students.pojo.entity.StudentEntity;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentPageReq;
import cn.yanque.modules.students.pojo.vo.resvo.StudentRes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StudentMapper {
    StudentEntity selectById(@Param("id") Long id);
    StudentEntity selectByPhone(@Param("phone") String phone);
    List<StudentRes> selectPage(StudentPageReq req);
    List<String> selectTagOptions();
    int countClass(@Param("classId") Long classId);
    int countActiveUser(@Param("userId") Long userId);
    int countActiveSop(@Param("studentId") Long studentId);
    int updateClass(@Param("id") Long id, @Param("classId") Long classId);
    int updateTag(@Param("id") Long id, @Param("studentTag") String studentTag);
    int insertSop(@Param("studentId") Long studentId, @Param("mentorId") Long mentorId);
    Long selectLastInsertId();
}
