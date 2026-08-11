package cn.yanque.modules.classes.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassPageReq;
import cn.yanque.modules.classes.pojo.vo.reqvo.ClassSaveReq;
import cn.yanque.modules.classes.pojo.vo.resvo.ClassRes;

public interface SysClassService {
    PageResult<ClassRes> page(ClassPageReq req);
    ClassRes detail(Long id);
    Long create(ClassSaveReq req);
    void update(Long id, ClassSaveReq req);
    void delete(Long id);
}
