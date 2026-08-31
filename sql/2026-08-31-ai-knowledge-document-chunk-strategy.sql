-- 知识库文档支持 JSON 和可配置切分策略。

ALTER TABLE `ai_knowledge_document`
  ADD COLUMN `chunk_strategy` varchar(20) NOT NULL DEFAULT 'MARKDOWN' COMMENT '切分策略：MARKDOWN按Markdown切分，NONE整体入库，BY_ITEM按JSON数组元素入库'
  AFTER `file_type`;

ALTER TABLE `ai_knowledge_document`
  MODIFY COLUMN `file_type` varchar(20) NOT NULL DEFAULT 'md' COMMENT '文件类型：md/json';
