/*
Navicat MySQL Data Transfer

Source Server         : 123
Source Server Version : 80024
Source Host           : localhost:3306
Source Database       : family_book

Target Server Type    : MYSQL
Target Server Version : 80024
File Encoding         : 65001

Date: 2026-03-30 12:08:48
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_budget
-- ----------------------------
DROP TABLE IF EXISTS `t_budget`;
CREATE TABLE `t_budget` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `family_id` bigint unsigned DEFAULT NULL COMMENT '家庭组ID',
  `category_id` bigint unsigned DEFAULT NULL COMMENT '分类ID(null表示总预算)',
  `budget_month` varchar(7) NOT NULL COMMENT '预算月份(yyyy-MM)',
  `budget_amount` decimal(15,2) NOT NULL COMMENT '预算金额',
  `alert_threshold` decimal(5,2) DEFAULT '0.80' COMMENT '预警阈值(如0.8表示80%)',
  `is_alerted` tinyint DEFAULT '0' COMMENT '是否已预警:0否1是',
  `status` tinyint DEFAULT '1' COMMENT '状态:0删除1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_category_month` (`user_id`,`category_id`,`budget_month`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2035956623339421701 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预算表';

-- ----------------------------
-- Records of t_budget
-- ----------------------------

-- ----------------------------
-- Table structure for t_category
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned DEFAULT NULL COMMENT '用户ID(系统分类为null)',
  `family_id` bigint unsigned DEFAULT NULL COMMENT '家庭组ID',
  `name` varchar(32) NOT NULL COMMENT '分类名称',
  `type` tinyint NOT NULL COMMENT '类型:1支出2收入',
  `icon` varchar(64) DEFAULT NULL COMMENT '图标',
  `color` varchar(16) DEFAULT NULL COMMENT '颜色',
  `sort` int DEFAULT '0' COMMENT '排序',
  `is_system` tinyint DEFAULT '0' COMMENT '是否系统预设:0否1是',
  `status` tinyint DEFAULT '1' COMMENT '状态:0禁用1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=2037558179415572482 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收支分类表';

-- ----------------------------
-- Records of t_category
-- ----------------------------
INSERT INTO `t_category` VALUES ('1', null, null, '工资', '2', 'salary', null, '1', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('2', null, null, '奖金', '2', 'bonus', null, '2', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('3', null, null, '投资收益', '2', 'investment', null, '3', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('4', null, null, '兼职', '2', 'part-time', null, '4', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('5', null, null, '红包', '2', 'redpacket', null, '5', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('6', null, null, '其他收入', '2', 'other', null, '6', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('101', null, null, '餐饮', '1', 'food', null, '1', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('102', null, null, '交通', '1', 'transport', null, '2', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('103', null, null, '购物', '1', 'shopping', null, '3', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('104', null, null, '娱乐', '1', 'entertainment', null, '4', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('105', null, null, '居住', '1', 'housing', null, '5', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('106', null, null, '医疗', '1', 'medical', null, '6', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('107', null, null, '教育', '1', 'education', null, '7', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('108', null, null, '人情', '1', 'gift', null, '8', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('109', null, null, '通讯', '1', 'phone', null, '9', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');
INSERT INTO `t_category` VALUES ('110', null, null, '其他支出', '1', 'other', null, '10', '1', '1', '2026-03-22 22:21:38', '2026-03-23 17:48:21');

-- ----------------------------
-- Table structure for t_dream_goal
-- ----------------------------
DROP TABLE IF EXISTS `t_dream_goal`;
CREATE TABLE `t_dream_goal` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `family_id` bigint unsigned DEFAULT NULL COMMENT '家庭组ID',
  `name` varchar(64) NOT NULL COMMENT '目标名称',
  `target_amount` decimal(15,2) NOT NULL COMMENT '目标金额',
  `saved_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已存金额',
  `savings_type` tinyint NOT NULL COMMENT '储蓄类型:1固定金额2工资百分比',
  `savings_amount` decimal(15,2) DEFAULT NULL COMMENT '固定储蓄金额',
  `savings_percent` decimal(5,2) DEFAULT NULL COMMENT '储蓄百分比(如0.3)',
  `monthly_income` decimal(15,2) DEFAULT NULL COMMENT '月收入(百分比模式用)',
  `target_date` date DEFAULT NULL COMMENT '目标日期',
  `icon` varchar(64) DEFAULT NULL COMMENT '图标',
  `remark` varchar(255) DEFAULT NULL COMMENT '目标备注',
  `priority` int DEFAULT '0' COMMENT '优先级',
  `goal_status` tinyint NOT NULL DEFAULT '1' COMMENT '业务状态:1进行中2已完成归档3已停止归档',
  `status` tinyint DEFAULT '1' COMMENT '状态:0停用1进行中2已完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_status` (`status`),
  KEY `idx_goal_status` (`goal_status`)
) ENGINE=InnoDB AUTO_INCREMENT=2037829735186284547 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='梦想目标表';

-- ----------------------------
-- Records of t_dream_goal
-- ----------------------------

-- ----------------------------
-- Table structure for t_family
-- ----------------------------
DROP TABLE IF EXISTS `t_family`;
CREATE TABLE `t_family` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '家庭组名称',
  `invite_code` varchar(16) NOT NULL COMMENT '邀请码',
  `owner_id` bigint unsigned NOT NULL COMMENT '创建者用户ID',
  `member_count` int DEFAULT '1' COMMENT '成员数量',
  `status` tinyint DEFAULT '1' COMMENT '状态:0禁用1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家庭组表';

-- ----------------------------
-- Records of t_family
-- ----------------------------

-- ----------------------------
-- Table structure for t_family_member
-- ----------------------------
DROP TABLE IF EXISTS `t_family_member`;
CREATE TABLE `t_family_member` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `family_id` bigint unsigned NOT NULL COMMENT '家庭组ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `role` tinyint DEFAULT '0' COMMENT '角色:0成员1管理员',
  `nickname_in_family` varchar(64) DEFAULT NULL COMMENT '家庭内昵称',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user` (`family_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家庭成员表';

-- ----------------------------
-- Records of t_family_member
-- ----------------------------

-- ----------------------------
-- Table structure for t_reminder
-- ----------------------------
DROP TABLE IF EXISTS `t_reminder`;
CREATE TABLE `t_reminder` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `remind_time` time NOT NULL COMMENT '提醒时间',
  `remind_type` tinyint DEFAULT '1' COMMENT '提醒类型:1每天2工作日3周末',
  `is_enabled` tinyint DEFAULT '1' COMMENT '是否启用:0否1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='记账提醒表';

-- ----------------------------
-- Records of t_reminder
-- ----------------------------

-- ----------------------------
-- Table structure for t_savings_record
-- ----------------------------
DROP TABLE IF EXISTS `t_savings_record`;
CREATE TABLE `t_savings_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goal_id` bigint unsigned NOT NULL COMMENT '目标ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `record_month` varchar(7) NOT NULL COMMENT '记录月份(yyyy-MM)',
  `planned_amount` decimal(15,2) NOT NULL COMMENT '计划储蓄金额',
  `actual_amount` decimal(15,2) DEFAULT '0.00' COMMENT '实际储蓄金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '存钱备注',
  `is_completed` tinyint DEFAULT '0' COMMENT '是否达标:0否1是',
  `status` tinyint DEFAULT '1' COMMENT '状态:0删除1正常',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_goal_id_create_time` (`goal_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2037833682772934659 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='储蓄记录表';

-- ----------------------------
-- Records of t_savings_record
-- ----------------------------

-- ----------------------------
-- Table structure for t_transaction
-- ----------------------------
DROP TABLE IF EXISTS `t_transaction`;
CREATE TABLE `t_transaction` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `family_id` bigint unsigned DEFAULT NULL COMMENT '家庭组ID',
  `category_id` bigint unsigned NOT NULL COMMENT '分类ID',
  `type` tinyint NOT NULL COMMENT '类型:1支出2收入',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `transaction_date` date NOT NULL COMMENT '交易日期',
  `transaction_time` time DEFAULT NULL COMMENT '交易时间',
  `location` varchar(128) DEFAULT NULL COMMENT '交易地点',
  `images` json DEFAULT NULL COMMENT '图片URL数组',
  `is_sync` tinyint DEFAULT '1' COMMENT '是否同步:0否1是',
  `status` tinyint DEFAULT '1' COMMENT '状态:0删除1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_type` (`type`),
  KEY `idx_transaction_date` (`transaction_date`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2037834210965831683 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易记录表';

-- ----------------------------
-- Records of t_transaction
-- ----------------------------

-- ----------------------------
-- Table structure for t_transfer
-- ----------------------------
DROP TABLE IF EXISTS `t_transfer`;
CREATE TABLE `t_transfer` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `from_account_id` bigint unsigned NOT NULL COMMENT '转出账户ID',
  `to_account_id` bigint unsigned NOT NULL COMMENT '转入账户ID',
  `amount` decimal(15,2) NOT NULL COMMENT '转账金额',
  `fee` decimal(15,2) DEFAULT '0.00' COMMENT '手续费',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `transfer_date` date NOT NULL COMMENT '转账日期',
  `status` tinyint DEFAULT '1' COMMENT '状态:0删除1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_from_account` (`from_account_id`),
  KEY `idx_to_account` (`to_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转账记录表';

-- ----------------------------
-- Records of t_transfer
-- ----------------------------

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(64) NOT NULL COMMENT '微信openid',
  `unionid` varchar(64) DEFAULT NULL COMMENT '微信unionid',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `default_family_id` bigint unsigned DEFAULT NULL COMMENT '默认家庭组ID',
  `status` tinyint DEFAULT '1' COMMENT '状态:0禁用1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `initial_balance` decimal(15,2) DEFAULT NULL COMMENT '起始金额',
  `initial_balance_set` tinyint(1) NOT NULL DEFAULT '0' COMMENT '鏄惁宸茶缃捣濮嬮噾棰�',
  `current_balance` decimal(15,2) DEFAULT '0.00' COMMENT '当前余额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_family_id` (`default_family_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2038282034517929987 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Records of t_user
-- ----------------------------
