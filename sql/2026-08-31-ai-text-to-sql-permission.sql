-- Text-to-SQL 独立数据权限配置。
--
-- 这里不保存 DENY/MASK 等字段安全策略，字段安全策略仍然以 DDL Resource 为准。
-- 这里也不维护单独的表字段目录：页面展示表字段时从 information_schema.tables / columns 读取当前数据库真实结构。
-- ai_text_to_sql_role_permission 只保存“角色被授权了哪些字段”。

CREATE TABLE IF NOT EXISTS `ai_text_to_sql_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT 'sys_role.id',
  `role_code` varchar(100) NOT NULL COMMENT '角色编码',
  `business_domain` varchar(50) NOT NULL COMMENT '业务域编码',
  `table_name` varchar(100) NOT NULL COMMENT '授权表名',
  `column_name` varchar(100) NOT NULL COMMENT '授权字段名',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_text_to_sql_role_column` (`role_id`, `business_domain`, `table_name`, `column_name`),
  KEY `idx_ai_text_to_sql_role_permission_code` (`role_code`, `status`),
  KEY `idx_ai_text_to_sql_role_permission_role` (`role_id`, `status`),
  KEY `idx_ai_text_to_sql_role_permission_table` (`business_domain`, `table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Text-to-SQL角色字段授权';
