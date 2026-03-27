ALTER TABLE `t_user`
MODIFY COLUMN `initial_balance` DECIMAL(15,2) DEFAULT NULL COMMENT '起始金额';

ALTER TABLE `t_user`
ADD COLUMN `initial_balance_set` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已设置起始金额' AFTER `initial_balance`;

UPDATE `t_user`
SET `initial_balance_set` = 1
WHERE `initial_balance` IS NOT NULL
  AND (`initial_balance` <> 0 OR `current_balance` <> 0);
