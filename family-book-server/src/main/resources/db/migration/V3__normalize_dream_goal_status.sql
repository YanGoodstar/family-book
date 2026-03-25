-- 修复梦想目标误用 status 字段的问题
-- status 在当前项目中用于逻辑删除，正常记录应保持为 1
-- 旧版本把 2 当作“已完成”写入，导致 MyBatis-Plus 查询时被过滤
UPDATE `t_dream_goal`
SET `status` = 1
WHERE `status` IS NULL OR `status` <> 0;
