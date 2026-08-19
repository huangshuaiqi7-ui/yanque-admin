-- AI 提示词模板版本管理建表脚本。
--
-- prompt_template_version 保存提示词模板的历史版本内容。
-- 当前启用版本不在本表存状态，由 prompt_template.active_version_id 指向。

CREATE TABLE IF NOT EXISTS `prompt_template_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` bigint NOT NULL COMMENT '所属提示词模板 ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `content` text NOT NULL COMMENT '提示词内容',
  `variables` json DEFAULT NULL COMMENT '变量说明',
  `change_note` varchar(500) DEFAULT NULL COMMENT '本次修改说明',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prompt_template_version` (`template_id`, `version_no`),
  KEY `idx_prompt_template_version_template_time` (`template_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提示词模板版本表';
