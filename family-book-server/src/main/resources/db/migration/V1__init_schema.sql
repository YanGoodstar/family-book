-- 家庭记账小程序数据库初始化脚本
-- 执行顺序：先创建表结构，再插入初始数据

-- ==========================================
-- 1. 创建数据库（如不存在）
-- ==========================================
-- CREATE DATABASE IF NOT EXISTS family_book DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE family_book;

-- ==========================================
-- 2. 创建表结构
-- ==========================================

-- 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `default_family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '默认家庭组ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0禁用1启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_family_id` (`default_family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 家庭组表
CREATE TABLE IF NOT EXISTS `t_family` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '家庭组名称',
  `invite_code` VARCHAR(16) NOT NULL COMMENT '邀请码',
  `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '创建者用户ID',
  `member_count` INT DEFAULT 1 COMMENT '成员数量',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0禁用1启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭组表';

-- 家庭成员表
CREATE TABLE IF NOT EXISTS `t_family_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `family_id` BIGINT UNSIGNED NOT NULL COMMENT '家庭组ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role` TINYINT DEFAULT 0 COMMENT '角色:0成员1管理员',
  `nickname_in_family` VARCHAR(64) DEFAULT NULL COMMENT '家庭内昵称',
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user` (`family_id`, `user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- 收支分类表
CREATE TABLE IF NOT EXISTS `t_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID，null为系统预设',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID，null为个人分类',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `type` TINYINT NOT NULL COMMENT '类型:1收入2支出',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `is_system` TINYINT DEFAULT 0 COMMENT '是否系统预设:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收支分类表';

-- 账户表
CREATE TABLE IF NOT EXISTS `t_account` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID',
  `name` VARCHAR(64) NOT NULL COMMENT '账户名称',
  `type` TINYINT NOT NULL COMMENT '类型:1现金2储蓄卡3信用卡4支付宝5微信6其他',
  `balance` DECIMAL(15,2) DEFAULT 0.00 COMMENT '余额',
  `credit_limit` DECIMAL(15,2) DEFAULT NULL COMMENT '信用额度(信用卡)',
  `bill_day` INT DEFAULT NULL COMMENT '账单日(信用卡)',
  `repay_day` INT DEFAULT NULL COMMENT '还款日(信用卡)',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认:0否1是',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- 交易记录表
CREATE TABLE IF NOT EXISTS `t_transaction` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID',
  `account_id` BIGINT UNSIGNED NOT NULL COMMENT '账户ID',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  `type` TINYINT NOT NULL COMMENT '类型:1支出2收入',
  `amount` DECIMAL(15,2) NOT NULL COMMENT '金额',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `transaction_date` DATE NOT NULL COMMENT '交易日期',
  `transaction_time` TIME DEFAULT NULL COMMENT '交易时间',
  `location` VARCHAR(128) DEFAULT NULL COMMENT '交易地点',
  `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL数组(JSON)',
  `is_sync` TINYINT DEFAULT 0 COMMENT '是否同步:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_transaction_date` (`transaction_date`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- 转账记录表
CREATE TABLE IF NOT EXISTS `t_transfer` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `from_account_id` BIGINT UNSIGNED NOT NULL COMMENT '转出账户ID',
  `to_account_id` BIGINT UNSIGNED NOT NULL COMMENT '转入账户ID',
  `amount` DECIMAL(15,2) NOT NULL COMMENT '转账金额',
  `fee` DECIMAL(15,2) DEFAULT 0.00 COMMENT '手续费',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `transfer_date` DATE NOT NULL COMMENT '转账日期',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_from_account` (`from_account_id`),
  KEY `idx_to_account` (`to_account_id`),
  KEY `idx_transfer_date` (`transfer_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账记录表';

-- 预算表
CREATE TABLE IF NOT EXISTS `t_budget` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID',
  `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID，null为总预算',
  `budget_month` VARCHAR(7) NOT NULL COMMENT '预算月份yyyy-MM',
  `budget_amount` DECIMAL(15,2) NOT NULL COMMENT '预算金额',
  `alert_threshold` DECIMAL(3,2) DEFAULT 0.80 COMMENT '预警阈值',
  `is_alerted` TINYINT DEFAULT 0 COMMENT '是否已预警:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_category_month` (`user_id`, `category_id`, `budget_month`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_budget_month` (`budget_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- 梦想目标表
CREATE TABLE IF NOT EXISTS `t_dream_goal` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID',
  `name` VARCHAR(64) NOT NULL COMMENT '目标名称',
  `target_amount` DECIMAL(15,2) NOT NULL COMMENT '目标金额',
  `saved_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '已存金额',
  `target_date` DATE DEFAULT NULL COMMENT '目标日期',
  `savings_type` TINYINT NOT NULL COMMENT '储蓄类型:1固定金额2工资百分比',
  `savings_amount` DECIMAL(15,2) DEFAULT NULL COMMENT '固定储蓄金额',
  `savings_percent` DECIMAL(5,2) DEFAULT NULL COMMENT '储蓄百分比',
  `monthly_income` DECIMAL(15,2) DEFAULT NULL COMMENT '月收入',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `priority` INT DEFAULT 0 COMMENT '优先级',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0停用1进行中2已完成',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='梦想目标表';

-- 储蓄记录表
CREATE TABLE IF NOT EXISTS `t_savings_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goal_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `record_month` VARCHAR(7) NOT NULL COMMENT '记录月份yyyy-MM',
  `planned_amount` DECIMAL(15,2) NOT NULL COMMENT '计划储蓄金额',
  `actual_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '实际储蓄金额',
  `is_completed` TINYINT DEFAULT 0 COMMENT '是否达标:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goal_month` (`goal_id`, `record_month`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='储蓄记录表';

-- 记账提醒表
CREATE TABLE IF NOT EXISTS `t_reminder` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `remind_time` TIME NOT NULL COMMENT '提醒时间',
  `remind_type` TINYINT DEFAULT 1 COMMENT '提醒类型:1每天2工作日3周末',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账提醒表';

-- ==========================================
-- 3. 插入初始数据
-- ==========================================

-- 收入分类 (type=2)
INSERT INTO `t_category` (`id`, `name`, `type`, `icon`, `sort`, `is_system`, `status`) VALUES
(1, '工资', 2, 'salary', 1, 1, 1),
(2, '奖金', 2, 'bonus', 2, 1, 1),
(3, '投资收益', 2, 'investment', 3, 1, 1),
(4, '兼职', 2, 'part-time', 4, 1, 1),
(5, '红包', 2, 'redpacket', 5, 1, 1),
(6, '其他收入', 2, 'other', 6, 1, 1)
ON DUPLICATE KEY UPDATE status = 1;

-- 支出分类 (type=1)
INSERT INTO `t_category` (`id`, `name`, `type`, `icon`, `sort`, `is_system`, `status`) VALUES
(101, '餐饮', 1, 'food', 1, 1, 1),
(102, '交通', 1, 'transport', 2, 1, 1),
(103, '购物', 1, 'shopping', 3, 1, 1),
(104, '娱乐', 1, 'entertainment', 4, 1, 1),
(105, '居住', 1, 'housing', 5, 1, 1),
(106, '医疗', 1, 'medical', 6, 1, 1),
(107, '教育', 1, 'education', 7, 1, 1),
(108, '人情', 1, 'gift', 8, 1, 1),
(109, '通讯', 1, 'phone', 9, 1, 1),
(110, '其他支出', 1, 'other', 10, 1, 1)
ON DUPLICATE KEY UPDATE status = 1;
