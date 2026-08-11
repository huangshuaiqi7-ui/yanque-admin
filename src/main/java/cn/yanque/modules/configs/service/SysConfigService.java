package cn.yanque.modules.configs.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.configs.pojo.vo.reqvo.SysConfigPageReq;
import cn.yanque.modules.configs.pojo.vo.reqvo.SysConfigSaveReq;
import cn.yanque.modules.configs.pojo.vo.resvo.SysConfigRes;

public interface SysConfigService {
    PageResult<SysConfigRes> page(SysConfigPageReq req);
    SysConfigRes detail(Long id);
    Long create(SysConfigSaveReq req);
    void update(Long id, SysConfigSaveReq req);
    void delete(Long id);
}
