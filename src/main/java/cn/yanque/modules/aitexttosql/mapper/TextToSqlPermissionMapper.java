package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlColumnEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRolePermissionEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlPermissionMapper {
    List<TextToSqlTableEntity> selectActiveTables();

    List<TextToSqlColumnEntity> selectActiveColumns();

    int countColumns(@Param("grants") List<TextToSqlRolePermissionEntity> grants);

    List<TextToSqlRolePermissionEntity> selectByRoleId(@Param("roleId") Long roleId);

    List<TextToSqlRolePermissionEntity> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    int deleteByRoleId(@Param("roleId") Long roleId);

    int insertRolePermissions(@Param("list") List<TextToSqlRolePermissionEntity> list);
}
