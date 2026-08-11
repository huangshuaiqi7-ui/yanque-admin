package cn.yanque.modules.configs.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.yanque.commons.apires.CommonErrorCode;
import cn.yanque.commons.apires.PageResult;
import cn.yanque.commons.exception.BusinessException;
import cn.yanque.modules.configs.mapper.SysConfigMapper;
import cn.yanque.modules.configs.pojo.entity.SysConfigEntity;
import cn.yanque.modules.configs.pojo.vo.reqvo.SysConfigPageReq;
import cn.yanque.modules.configs.pojo.vo.reqvo.SysConfigSaveReq;
import cn.yanque.modules.configs.pojo.vo.resvo.SysConfigRes;
import cn.yanque.modules.configs.service.SysConfigService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SysConfigServiceImpl implements SysConfigService {
    private final SysConfigMapper configMapper;

    public SysConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Override
    public PageResult<SysConfigRes> page(SysConfigPageReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<SysConfigEntity> configs = configMapper.selectPage(StrUtil.trim(req.getKeyword()));
        PageInfo<SysConfigEntity> pageInfo = new PageInfo<>(configs);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize(),
                configs.stream().map(this::toRes).toList());
    }

    @Override
    public SysConfigRes detail(Long id) {
        return toRes(getConfig(id));
    }

    @Override
    @Transactional
    public Long create(SysConfigSaveReq req) {
        String key = StrUtil.trim(req.getK());
        validateUniqueKey(key, null);
        SysConfigEntity config = toEntity(req);
        try {
            if (configMapper.insert(config) != 1) {
                throw BusinessException.of(CommonErrorCode.CONFIG_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.CONFIG_KEY_EXISTS);
        }
        return config.getId();
    }

    @Override
    @Transactional
    public void update(Long id, SysConfigSaveReq req) {
        getConfig(id);
        validateUniqueKey(StrUtil.trim(req.getK()), id);
        SysConfigEntity config = toEntity(req);
        config.setId(id);
        try {
            if (configMapper.updateById(config) != 1) {
                throw BusinessException.of(CommonErrorCode.CONFIG_OPERATION_FAILED);
            }
        } catch (DuplicateKeyException exception) {
            throw BusinessException.of(CommonErrorCode.CONFIG_KEY_EXISTS);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getConfig(id);
        if (configMapper.deleteById(id) != 1) {
            throw BusinessException.of(CommonErrorCode.CONFIG_OPERATION_FAILED);
        }
    }

    private SysConfigEntity getConfig(Long id) {
        SysConfigEntity config = configMapper.selectById(id);
        if (config == null) {
            throw BusinessException.of(CommonErrorCode.CONFIG_NOT_FOUND);
        }
        return config;
    }

    private void validateUniqueKey(String key, Long currentId) {
        SysConfigEntity existing = configMapper.selectByKey(key);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw BusinessException.of(CommonErrorCode.CONFIG_KEY_EXISTS);
        }
    }

    private SysConfigEntity toEntity(SysConfigSaveReq req) {
        SysConfigEntity config = new SysConfigEntity();
        config.setK(StrUtil.trim(req.getK()));
        config.setV(StrUtil.trim(req.getV()));
        return config;
    }

    private SysConfigRes toRes(SysConfigEntity config) {
        SysConfigRes result = new SysConfigRes();
        result.setId(config.getId());
        result.setK(config.getK());
        result.setV(config.getV());
        return result;
    }
}
