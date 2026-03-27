ALTER TABLE `t_dream_goal`
ADD COLUMN `goal_status` TINYINT NOT NULL DEFAULT 1 COMMENT '业务状态:1进行中2已完成归档3已停止归档' AFTER `priority`;

UPDATE `t_dream_goal`
SET `goal_status` = 1
WHERE `goal_status` IS NULL;

CREATE INDEX `idx_goal_status` ON `t_dream_goal` (`goal_status`);
