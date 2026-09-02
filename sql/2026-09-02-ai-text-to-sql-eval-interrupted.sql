ALTER TABLE `ai_text_to_sql_eval_task`
  ADD COLUMN `interrupted_count` int NOT NULL DEFAULT 0 COMMENT '中断数' AFTER `fail_count`;

ALTER TABLE `ai_text_to_sql_eval_result`
  ADD COLUMN `result_status` varchar(32) NOT NULL DEFAULT 'FAILED' COMMENT '结果状态：PASSED/FAILED/INTERRUPTED' AFTER `question`;
