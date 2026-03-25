-- 梦想目标 MVP 调整
-- 1. 目标增加备注字段
ALTER TABLE `t_dream_goal`
ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT '目标备注' AFTER `icon`;

-- 2. 存钱记录支持逐笔记录与备注
ALTER TABLE `t_savings_record`
DROP INDEX `uk_goal_month`,
ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT '存钱备注' AFTER `actual_amount`;

CREATE INDEX `idx_goal_id_create_time` ON `t_savings_record` (`goal_id`, `create_time`);
