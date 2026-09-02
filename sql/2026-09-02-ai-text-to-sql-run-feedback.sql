ALTER TABLE `ai_text_to_sql_run`
  ADD COLUMN `feedback_result` varchar(16) NULL COMMENT '反馈结果：GOOD/BAD' AFTER `duration_ms`,
  ADD COLUMN `feedback_comment` text NULL COMMENT '反馈说明' AFTER `feedback_result`,
  ADD COLUMN `feedback_at` datetime NULL COMMENT '反馈时间' AFTER `feedback_comment`;

CREATE INDEX `idx_ai_text_to_sql_run_feedback`
  ON `ai_text_to_sql_run` (`feedback_result`, `feedback_at`);
