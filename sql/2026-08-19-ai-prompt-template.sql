-- AI 提示词模板管理一期建表脚本。
--
-- prompt_template 保存提示词模板元信息；提示词正文、版本发布、评估测试后续由版本表承载。
-- 删除模板采用物理删除，不保留 DELETED 状态。

CREATE TABLE IF NOT EXISTS `prompt_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(100) NOT NULL COMMENT '提示词编码，例如 student_chat_system，创建后不允许修改',
  `name` varchar(100) NOT NULL COMMENT '提示词名称',
  `agent_code` varchar(100) NOT NULL COMMENT '所属 Agent，例如 student_chat_agent',
  `prompt_type` varchar(20) NOT NULL COMMENT '提示词类型：SYSTEM/USER',
  `scene_code` varchar(50) DEFAULT NULL COMMENT '使用场景：CHAT/RAG/SUMMARY/JUDGE/STRUCTURED_EXTRACT',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 启用，INACTIVE 禁用',
  `active_version_id` bigint DEFAULT NULL COMMENT '当前启用的版本 ID，版本管理上线后维护',
  `description` varchar(500) DEFAULT NULL COMMENT '说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_prompt_template_code` (`code`),
  KEY `idx_prompt_template_filter` (`agent_code`, `status`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提示词模板表';
