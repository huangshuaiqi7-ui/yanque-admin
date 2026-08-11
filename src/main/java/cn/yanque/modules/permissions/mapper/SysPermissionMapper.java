package cn.yanque.modules.permissions.mapper;

import cn.yanque.modules.permissions.pojo.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper {
    List<SysPermissionEntity> selectPage(@Param("keyword") String keyword,
                                         @Param("parentId") Long parentId,
                                         @Param("permissionCode") String permissionCode,
                                         @Param("permissionName") String permissionName,
                                         @Param("permissionType") String permissionType,
                                         @Param("status") String status);
    List<SysPermissionEntity> selectAll();
    SysPermissionEntity selectById(@Param("id") Long id);
    SysPermissionEntity selectByCode(@Param("permissionCode") String permissionCode);
    int countByParentId(@Param("parentId") Long parentId);
    int countByIds(@Param("ids") List<Long> ids);
    int insert(SysPermissionEntity permission);
    int updateById(SysPermissionEntity permission);
    int deleteById(@Param("id") Long id);
    int deleteRolePermissionsByPermissionId(@Param("permissionId") Long permissionId);
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
    List<Long> selectUserIdsByPermissionId(@Param("permissionId") Long permissionId);
}
