package cn.yanque.modules.campuses.service.impl;

import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.campuses.mapper.SysCampusMapper;
import cn.yanque.modules.campuses.pojo.entity.SysCampusEntity;
import cn.yanque.modules.campuses.pojo.vo.reqvo.CampusPageReq;
import cn.yanque.modules.campuses.pojo.vo.reqvo.CampusSaveReq;
import cn.yanque.modules.campuses.pojo.vo.resvo.CampusRes;
import cn.yanque.modules.campuses.service.SysCampusService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysCampusServiceImpl implements SysCampusService {
    private final SysCampusMapper campusMapper;

    public SysCampusServiceImpl(SysCampusMapper campusMapper) {
        this.campusMapper = campusMapper;
    }

    @Override
    public PageResult<CampusRes> page(CampusPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SysCampusEntity> campuses = campusMapper.selectPage(req.getKeyword());
        PageInfo<SysCampusEntity> pageInfo = new PageInfo<>(campuses);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(),
                campuses.stream().map(this::toRes).toList());
    }

    @Override
    public CampusRes detail(Long id) {
        return toRes(getCampus(id));
    }

    @Override
    @Transactional
    public Long create(CampusSaveReq req) {
        SysCampusEntity campus = toEntity(req);
        if (campusMapper.insert(campus) != 1) {
            throw BusinessException.of(CommonErrorCode.CAMPUS_OPERATION_FAILED);
        }
        return campus.getId();
    }

    @Override
    @Transactional
    public void update(Long id, CampusSaveReq req) {
        getCampus(id);
        SysCampusEntity campus = toEntity(req);
        campus.setId(id);
        if (campusMapper.updateById(campus) != 1) {
            throw BusinessException.of(CommonErrorCode.CAMPUS_OPERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getCampus(id);
        if (campusMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.CAMPUS_OPERATION_FAILED);
        }
    }

    private SysCampusEntity getCampus(Long id) {
        SysCampusEntity campus = campusMapper.selectById(id);
        if (campus == null) {
            throw BusinessException.of(CommonErrorCode.CAMPUS_NOT_FOUND);
        }
        return campus;
    }

    private SysCampusEntity toEntity(CampusSaveReq req) {
        SysCampusEntity campus = new SysCampusEntity();
        campus.setCampusLocation(req.getCampusLocation());
        campus.setManagerName(req.getManagerName());
        campus.setManagerPhone(req.getManagerPhone());
        return campus;
    }

    private CampusRes toRes(SysCampusEntity campus) {
        CampusRes result = new CampusRes();
        result.setId(campus.getId());
        result.setCampusLocation(campus.getCampusLocation());
        result.setManagerName(campus.getManagerName());
        result.setManagerPhone(campus.getManagerPhone());
        result.setCreatedAt(campus.getCreatedAt());
        result.setUpdatedAt(campus.getUpdatedAt());
        return result;
    }
}
