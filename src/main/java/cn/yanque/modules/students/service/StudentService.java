package cn.yanque.modules.students.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentClassAssignReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentPageReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentSopAssignReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentTagUpdateReq;
import cn.yanque.modules.students.pojo.vo.resvo.StudentRes;
import java.util.List;

public interface StudentService {
    PageResult<StudentRes> page(StudentPageReq req);
    void assignClass(Long id, StudentClassAssignReq req);
    List<String> tagOptions();
    void updateTag(Long id, StudentTagUpdateReq req);
    Long assignSop(Long id, StudentSopAssignReq req);
}
