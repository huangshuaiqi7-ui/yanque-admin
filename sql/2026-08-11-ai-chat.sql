-- AI 问答模块建表脚本。
--
-- ai_chat_session 保存一段对话的会话信息；
-- ai_chat_message 保存会话里的每一条 user / assistant 消息。
-- summary / last_compressed_message_id / compressed 三个字段一起完成对话压缩。

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `title` varchar(100) DEFAULT NULL COMMENT '会话标题，取第一句提问生成',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE 正常，DELETED 已删除',
  `summary` text COMMENT '历史对话摘要，旧消息压缩后保存到这里',
  `last_compressed_message_id` bigint NOT NULL DEFAULT '0' COMMENT '已压缩到哪条消息，配合summary使用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_status_updated` (`student_id`, `status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI问答会话表';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `role` varchar(20) NOT NULL COMMENT 'user 用户，assistant AI',
  `content` text NOT NULL COMMENT '消息内容',
  `model` varchar(100) DEFAULT NULL COMMENT '本条回答用的模型',
  `tokens` int DEFAULT NULL COMMENT '消耗Token数，用于成本统计',
  `compressed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已压缩进会话摘要',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_created` (`session_id`, `created_at`),
  KEY `idx_session_compressed_id` (`session_id`, `compressed`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI问答消息表';
