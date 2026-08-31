-- AI 知识库文档管理建表脚本。
--
-- ai_knowledge_document 保存每个知识库下的知识文档入库状态。
-- 删除文档采用物理删除，不保留 DELETED 状态。

CREATE TABLE IF NOT EXISTS `ai_knowledge_document` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` bigint NOT NULL COMMENT '知识库ID',
  `knowledge_base_code` varchar(64) NOT NULL COMMENT '知识库编码，对应 Milvus Collection 名称',
  `name` varchar(200) NOT NULL COMMENT '文档名称',
  `code` varchar(64) NOT NULL COMMENT '文档编码，同一知识库内唯一',
  `object_key` varchar(500) NOT NULL COMMENT 'TOS对象Key',
  `file_type` varchar(20) NOT NULL DEFAULT 'md' COMMENT '文件类型：md/json',
  `chunk_strategy` varchar(20) NOT NULL DEFAULT 'MARKDOWN' COMMENT '切分策略：MARKDOWN按Markdown切分，NONE整体入库，BY_ITEM按JSON数组元素入库',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小，单位字节',
  `status` varchar(20) NOT NULL DEFAULT 'INDEXING' COMMENT 'INDEXING 入库中，READY 已完成，FAILED 失败',
  `chunk_count` int NOT NULL DEFAULT '0' COMMENT '切分后chunk数量',
  `version` int NOT NULL DEFAULT '1' COMMENT '文档入库版本，重建时递增',
  `last_error_message` varchar(1000) DEFAULT NULL COMMENT '最近一次入库失败原因',
  `created_by` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_doc_code` (`knowledge_base_id`, `code`),
  UNIQUE KEY `uk_kb_doc_name` (`knowledge_base_id`, `name`),
  KEY `idx_kb_status_updated` (`knowledge_base_id`, `status`, `updated_at`),
  KEY `idx_kb_updated` (`knowledge_base_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识库文档表';
