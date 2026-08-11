package cn.yanque.modules.roles.mapper;

import cn.yanque.modules.roles.pojo.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    List<SysRoleEntity> selectPage(@Param("keyword") String keyword,
                                   @Param("roleCode") String roleCode,
                                   @Param("roleName") String roleName,
                                   @Param("status") String status);
    SysRoleEntity selectById(@Param("id") Long id);
    SysRoleEntity selectByCode(@Param("roleCode") String roleCode);
    int countByIds(@Param("ids") List<Long> ids);
    int insert(SysRoleEntity role);
    int updateById(SysRoleEntity role);
    int deleteById(@Param("id") Long id);
    List<Long> selectPermissionIds(@Param("roleId") Long roleId);
    int insertRolePermissions(@Param("roleId") Long roleId,
                              @Param("permissionIds") List<Long> permissionIds);
    int deleteRolePermissions(@Param("roleId") Long roleId);
    int deleteUserRoles(@Param("roleId") Long roleId);
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
