package cn.yanque.modules.students.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.students.mapper.StudentMapper;
import cn.yanque.modules.students.pojo.entity.StudentEntity;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentClassAssignReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentPageReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentSopAssignReq;
import cn.yanque.modules.students.pojo.vo.reqvo.StudentTagUpdateReq;
import cn.yanque.modules.students.pojo.vo.resvo.StudentRes;
import cn.yanque.modules.students.service.StudentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public PageResult<StudentRes> page(StudentPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<StudentRes> rows = studentMapper.selectPage(req);
        PageInfo<StudentRes> pageInfo = new PageInfo<>(rows);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(), rows);
    }

    @Override
    @Transactional
    public void assignClass(Long id, StudentClassAssignReq req) {
        StudentEntity student = getStudent(id);
        if (!"OFFLINE".equals(student.getTeachingMode())) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "只有线下学生需要分配班级");
        }
        if (studentMapper.countClass(req.getClassId()) == 0) {
            throw BusinessException.of(CommonErrorCode.CLASS_NOT_FOUND);
        }
        if (studentMapper.updateClass(id, req.getClassId()) != 1) {
            throw BusinessException.of(CommonErrorCode.FAILED, "学生班级分配失败");
        }
    }

    @Override
    public List<String> tagOptions() {
        return studentMapper.selectTagOptions();
    }

    @Override
    @Transactional
    public void updateTag(Long id, StudentTagUpdateReq req) {
        getStudent(id);
        String studentTag = StrUtil.isBlank(req.getStudentTag()) ? null : req.getStudentTag().trim();
        if (studentMapper.updateTag(id, studentTag) != 1) {
            throw BusinessException.of(CommonErrorCode.FAILED, "学生标签更新失败");
        }
    }

    @Override
    @Transactional
    public Long assignSop(Long id, StudentSopAssignReq req) {
        StudentEntity student = getStudent(id);
        if (!"ONLINE".equals(student.getTeachingMode())) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "只有线上学生可以分配SOP");
        }
        if (studentMapper.countActiveUser(req.getMentorId()) == 0) {
            throw BusinessException.of(CommonErrorCode.USER_DETAIL_NOT_FOUND);
        }
        if (studentMapper.countActiveSop(id) > 0) {
            throw BusinessException.of(CommonErrorCode.PARAM_VALID_FAILED, "该学生已分配SOP");
        }
        if (studentMapper.insertSop(id, req.getMentorId()) != 1) {
            throw BusinessException.of(CommonErrorCode.FAILED, "SOP分配失败");
        }
        return studentMapper.selectLastInsertId();
    }

    private StudentEntity getStudent(Long id) {
        StudentEntity student = studentMapper.selectById(id);
        if (student == null) {
            throw BusinessException.of(CommonErrorCode.STUDENT_NOT_FOUND);
        }
        return student;
    }
}
