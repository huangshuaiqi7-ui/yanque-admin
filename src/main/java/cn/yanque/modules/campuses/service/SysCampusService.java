package cn.yanque.modules.campuses.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.campuses.pojo.vo.reqvo.CampusPageReq;
import cn.yanque.modules.campuses.pojo.vo.reqvo.CampusSaveReq;
import cn.yanque.modules.campuses.pojo.vo.resvo.CampusRes;

public interface SysCampusService {
    PageResult<CampusRes> page(CampusPageReq req);
    CampusRes detail(Long id);
    Long create(CampusSaveReq req);
    void update(Long id, CampusSaveReq req);
    void delete(Long id);
}
