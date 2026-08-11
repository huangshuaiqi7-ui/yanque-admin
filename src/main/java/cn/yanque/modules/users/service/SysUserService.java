package cn.yanque.modules.users.service;

import cn.yanque.commons.apires.PageResult;
import cn.yanque.modules.users.pojo.vo.reqvo.LoginReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserCreateReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserPageReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserUpdateReq;
import cn.yanque.modules.users.pojo.vo.reqvo.UserRoleAssignReq;
import cn.yanque.modules.users.pojo.vo.resvo.LoginRes;
import cn.yanque.modules.users.pojo.vo.resvo.UserDetailRes;

public interface SysUserService {

    LoginRes login(LoginReq req);

    void logout();

    PageResult<UserDetailRes> page(UserPageReq req);

    UserDetailRes detail(Long id);

    Long create(UserCreateReq req);

    void update(Long id, UserUpdateReq req);

    void delete(Long id);

    void assignRoles(Long id, UserRoleAssignReq req);
}
