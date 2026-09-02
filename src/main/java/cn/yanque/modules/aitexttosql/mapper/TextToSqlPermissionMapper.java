package cn.yanque.modules.aitexttosql.mapper;

import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlColumnEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlRolePermissionEntity;
import cn.yanque.modules.aitexttosql.pojo.entity.TextToSqlTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextToSqlPermissionMapper {
    /**
     * 查询当前数据库可用于 Text-to-SQL 的表。
     */
    List<TextToSqlTableEntity> selectActiveTables();

    /**
     * 查询当前数据库可用于 Text-to-SQL 的字段。
     */
    List<TextToSqlColumnEntity> selectActiveColumns();

    /**
     * 统计待授权字段在数据库目录中是否都存在。
     */
    int countColumns(@Param("grants") List<TextToSqlRolePermissionEntity> grants);

    /**
     * 查询某个角色的字段授权。
     */
    List<TextToSqlRolePermissionEntity> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询多个角色编码对应的字段授权。
     */
    List<TextToSqlRolePermissionEntity> selectByRoleCodes(@Param("roleCodes") List<String> roleCodes);

    /**
     * 删除某个角色的全部字段授权。
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量新增角色字段授权。
     */
    int insertRolePermissions(@Param("list") List<TextToSqlRolePermissionEntity> list);
}
