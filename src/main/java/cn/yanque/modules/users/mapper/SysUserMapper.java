package cn.yanque.modules.users.mapper;


import cn.yanque.modules.users.pojo.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper //注解被扫描到,需压启动类添加 @MapperScan("cn.yanque.modules.users.mapper")
public interface SysUserMapper {

    SysUserEntity selectById(@Param("id") Long id);

    SysUserEntity selectByUsername(@Param("username") String username);

    List<SysUserEntity> selectPage(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("roleCode") String roleCode);

    List<Long> selectRoleIds(@Param("userId") Long userId);

    int deleteUserRoles(@Param("userId") Long userId);

    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    int insert(SysUserEntity user);

    int updateById(SysUserEntity user);

    int deleteById(@Param("id") Long id);
}
