-- AI 知识库列表页建表脚本。
--
-- ai_knowledge_base 保存管理端知识库列表需要的业务状态。
-- 删除知识库采用物理删除，不保留 DELETED 状态。

CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '知识库名称',
  `code` varchar(64) NOT NULL COMMENT '知识库编码，创建后不允许修改，用于向量库过滤和系统标识',
  `description` varchar(500) DEFAULT NULL COMMENT '知识库说明',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 启用，INACTIVE 禁用',
  `document_count` int NOT NULL DEFAULT '0' COMMENT '文档数量',
  `chunk_count` int NOT NULL DEFAULT '0' COMMENT '已入库chunk数量',
  `created_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_status_updated` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识库表';
