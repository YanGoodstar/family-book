# 家庭记账小程序 - Java后端技术方案

## 1. 技术选型

### 1.1 后端技术栈

| 层级 | 技术 | 版本    | 说明 |
|------|------|-------|------|
| 基础框架 | Spring Boot | 3.2.x | 快速开发、自动配置 |
| Web框架 | Spring Web | 6.1.x | RESTful API开发 |
| 数据访问 | MyBatis-Plus | 3.5.x | 简化CRUD操作 |
| 数据库 | MySQL | 8.0+  | 主数据库 |
| 缓存 | Redis | 5.x   | 缓存、会话、分布式锁 |
| 安全 | Spring Security + JWT | -     | 认证授权 |
| 任务调度 | XXL-Job | 2.4.x | 定时任务（储蓄计算、提醒） |
| 文档 | SpringDoc OpenAPI | 2.3.x | API文档（Swagger） |
| 工具库 | Lombok、Hutool | -     | 代码简化 |
| 构建工具 | Maven | 3.8.x | 依赖管理 |

### 1.2 开发环境

- **JDK**: OpenJDK 17
- **IDE**: IntelliJ IDEA
- **数据库**: MySQL 8.0
- **缓存**: Redis 5.x
- **版本控制**: Git

---

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      微信小程序前端                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼ HTTPS
┌─────────────────────────────────────────────────────────────┐
│                        微信服务器                            │
│              （登录鉴权、消息推送、支付）                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Nginx (负载均衡/SSL)                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot 应用服务                       │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐        │
│  │  Controller │  │  Service  │  │  Mapper   │  │  Entity   │        │
│  │  (API层)   │  │ (业务层)  │  │ (数据层)  │  │ (实体层)  │        │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘        │
│                              │                              │
│                    ┌─────────┴─────────┐                    │
│                    │   MyBatis-Plus    │                    │
│                    └─────────┬─────────┘                    │
└──────────────────────────────┼──────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
   ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
   │    MySQL    │     │    Redis    │     │  XXL-Job    │
   │   主数据库   │     │   缓存中心   │     │  任务调度    │
   └─────────────┘     └─────────────┘     └─────────────┘
```

### 2.2 分层架构

```
family-book/
├── controller/          # 控制器层 - REST API接口
├── service/             # 业务层 - 业务逻辑
│   └── impl/
├── mapper/              # 数据访问层 - MyBatis-Plus
├── entity/              # 实体层 - 数据模型
├── dto/                 # 数据传输对象 - 请求/响应
├── vo/                  # 视图对象 - 返回给前端的数据
├── config/              # 配置类
├── common/              # 公共组件
│   ├── result/          # 统一响应结果
│   ├── exception/       # 全局异常处理
│   └── constants/       # 常量定义
├── utils/               # 工具类
├── security/            # 安全相关 - JWT、权限
├── job/                 # 定时任务
└── interceptor/         # 拦截器
```

---

## 3. 数据库设计

### 3.1 数据表清单

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | `t_user` | 用户表 |
| 2 | `t_family` | 家庭组表 |
| 3 | `t_family_member` | 家庭成员表 |
| 4 | `t_category` | 收支分类表 |
| 5 | `t_account` | 账户表 |
| 6 | `t_transaction` | 交易记录表（账单） |
| 7 | `t_transfer` | 转账记录表 |
| 8 | `t_budget` | 预算表 |
| 9 | `t_dream_goal` | 梦想目标表 |
| 10 | `t_savings_record` | 储蓄记录表 |
| 11 | `t_reminder` | 记账提醒表 |

### 3.2 核心表结构

#### 3.2.1 用户表 (t_user)

```sql
CREATE TABLE `t_user` (
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
```

#### 3.2.2 家庭组表 (t_family)

```sql
CREATE TABLE `t_family` (
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
```

#### 3.2.3 家庭成员表 (t_family_member)

```sql
CREATE TABLE `t_family_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `family_id` BIGINT UNSIGNED NOT NULL COMMENT '家庭组ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role` TINYINT DEFAULT 0 COMMENT '角色:0成员1管理员',
  `nickname_in_family` VARCHAR(64) DEFAULT NULL COMMENT '家庭内昵称',
  `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user` (`family_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';
```

#### 3.2.4 收支分类表 (t_category)

```sql
CREATE TABLE `t_category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID(系统分类为null)',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID',
  `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
  `type` TINYINT NOT NULL COMMENT '类型:1支出2收入',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `color` VARCHAR(16) DEFAULT NULL COMMENT '颜色',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `is_system` TINYINT DEFAULT 0 COMMENT '是否系统预设:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0禁用1启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收支分类表';
```

#### 3.2.5 账户表 (t_account)

```sql
CREATE TABLE `t_account` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID(共享账户)',
  `name` VARCHAR(64) NOT NULL COMMENT '账户名称',
  `type` TINYINT NOT NULL COMMENT '类型:1现金2储蓄卡3信用卡4支付宝5微信6其他',
  `balance` DECIMAL(15,2) DEFAULT 0.00 COMMENT '当前余额',
  `credit_limit` DECIMAL(15,2) DEFAULT NULL COMMENT '信用额度(信用卡)',
  `bill_day` INT DEFAULT NULL COMMENT '账单日(信用卡)',
  `repay_day` INT DEFAULT NULL COMMENT '还款日(信用卡)',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认账户:0否1是',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0禁用1启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';
```

#### 3.2.6 交易记录表 (t_transaction)

```sql
CREATE TABLE `t_transaction` (
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
  `images` JSON DEFAULT NULL COMMENT '图片URL数组',
  `is_sync` TINYINT DEFAULT 1 COMMENT '是否同步:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_account_id` (`account_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_type` (`type`),
  KEY `idx_transaction_date` (`transaction_date`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';
```

#### 3.2.7 转账记录表 (t_transfer)

```sql
CREATE TABLE `t_transfer` (
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
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_from_account` (`from_account_id`),
  KEY `idx_to_account` (`to_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账记录表';
```

#### 3.2.8 预算表 (t_budget)

```sql
CREATE TABLE `t_budget` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID',
  `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID(null表示总预算)',
  `budget_month` VARCHAR(7) NOT NULL COMMENT '预算月份(yyyy-MM)',
  `budget_amount` DECIMAL(15,2) NOT NULL COMMENT '预算金额',
  `alert_threshold` DECIMAL(5,2) DEFAULT 0.80 COMMENT '预警阈值(如0.8表示80%)',
  `is_alerted` TINYINT DEFAULT 0 COMMENT '是否已预警:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_category_month` (`user_id`, `category_id`, `budget_month`),
  KEY `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';
```

#### 3.2.9 梦想目标表 (t_dream_goal)

```sql
CREATE TABLE `t_dream_goal` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `family_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '家庭组ID',
  `name` VARCHAR(64) NOT NULL COMMENT '目标名称',
  `target_amount` DECIMAL(15,2) NOT NULL COMMENT '目标金额',
  `saved_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '已存金额',
  `savings_type` TINYINT NOT NULL COMMENT '储蓄类型:1固定金额2工资百分比',
  `savings_amount` DECIMAL(15,2) DEFAULT NULL COMMENT '固定储蓄金额',
  `savings_percent` DECIMAL(5,2) DEFAULT NULL COMMENT '储蓄百分比(如0.3)',
  `monthly_income` DECIMAL(15,2) DEFAULT NULL COMMENT '月收入(百分比模式用)',
  `target_date` DATE DEFAULT NULL COMMENT '目标日期',
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
```

#### 3.2.10 储蓄记录表 (t_savings_record)

```sql
CREATE TABLE `t_savings_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goal_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `record_month` VARCHAR(7) NOT NULL COMMENT '记录月份(yyyy-MM)',
  `planned_amount` DECIMAL(15,2) NOT NULL COMMENT '计划储蓄金额',
  `actual_amount` DECIMAL(15,2) DEFAULT 0.00 COMMENT '实际储蓄金额',
  `is_completed` TINYINT DEFAULT 0 COMMENT '是否达标:0否1是',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0删除1正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goal_month` (`goal_id`, `record_month`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='储蓄记录表';
```

#### 3.2.11 记账提醒表 (t_reminder)

```sql
CREATE TABLE `t_reminder` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `remind_time` TIME NOT NULL COMMENT '提醒时间',
  `remind_type` TINYINT DEFAULT 1 COMMENT '提醒类型:1每天2工作日3周末',
  `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用:0否1是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账提醒表';
```

---

## 4. 核心API设计

### 4.1 API 前缀与版本

```
基础路径: /api/v1
示例: /api/v1/user/info
```

### 4.2 接口清单

#### 4.2.1 用户模块 (user)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/login` | 微信登录（code换token） |
| GET | `/user/info` | 获取用户信息 |
| PUT | `/user/info` | 更新用户信息 |
| GET | `/user/statistics` | 用户统计（记账天数、笔数等） |

#### 4.2.2 家庭模块 (family)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/family` | 创建家庭组 |
| GET | `/family/{id}` | 获取家庭组信息 |
| POST | `/family/join` | 通过邀请码加入家庭组 |
| GET | `/family/{id}/members` | 获取成员列表 |
| DELETE | `/family/{id}/members/{userId}` | 移除成员 |
| PUT | `/family/{id}/members/{userId}/role` | 修改成员角色 |
| DELETE | `/family/{id}/leave` | 退出家庭组 |

#### 4.2.3 分类模块 (category)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/category` | 获取分类列表 |
| POST | `/category` | 创建自定义分类 |
| PUT | `/category/{id}` | 更新分类 |
| DELETE | `/category/{id}` | 删除分类 |

#### 4.2.4 账户模块 (account)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/account` | 获取账户列表 |
| POST | `/account` | 创建账户 |
| GET | `/account/{id}` | 获取账户详情 |
| PUT | `/account/{id}` | 更新账户 |
| DELETE | `/account/{id}` | 删除账户 |
| GET | `/account/statistics` | 资产统计 |

#### 4.2.5 账单模块 (transaction)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/transaction` | 账单列表（分页） |
| POST | `/transaction` | 记账 |
| GET | `/transaction/{id}` | 账单详情 |
| PUT | `/transaction/{id}` | 更新账单 |
| DELETE | `/transaction/{id}` | 删除账单 |
| GET | `/transaction/statistics` | 收支统计 |
| GET | `/transaction/trend` | 收支趋势 |
| GET | `/transaction/category-stat` | 分类统计 |

#### 4.2.6 转账模块 (transfer)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/transfer` | 转账记录列表 |
| POST | `/transfer` | 创建转账 |
| GET | `/transfer/{id}` | 转账详情 |
| DELETE | `/transfer/{id}` | 删除转账记录 |

#### 4.2.7 预算模块 (budget)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/budget` | 获取预算列表 |
| POST | `/budget` | 设置预算 |
| PUT | `/budget/{id}` | 更新预算 |
| GET | `/budget/analysis` | 预算执行分析 |

#### 4.2.8 梦想目标模块 (dream-goal)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dream-goal` | 目标列表 |
| POST | `/dream-goal` | 创建目标 |
| GET | `/dream-goal/{id}` | 目标详情 |
| PUT | `/dream-goal/{id}` | 更新目标 |
| DELETE | `/dream-goal/{id}` | 删除目标 |
| GET | `/dream-goal/{id}/dashboard` | 目标看板 |
| GET | `/dream-goal/{id}/trend` | 储蓄趋势 |
| POST | `/dream-goal/{id}/deposit` | 手动存入金额 |

#### 4.2.9 提醒模块 (reminder)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/reminder` | 获取提醒设置 |
| POST | `/reminder` | 创建提醒 |
| PUT | `/reminder/{id}` | 更新提醒 |
| DELETE | `/reminder/{id}` | 删除提醒 |

### 4.3 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

---

## 5. 项目目录结构

```
family-book-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/familybook/
│   │   │       ├── FamilyBookApplication.java
│   │   │       ├── config/
│   │   │       │   ├── MybatisPlusConfig.java
│   │   │       │   ├── RedisConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── WebMvcConfig.java
│   │   │       │   └── SwaggerConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── UserController.java
│   │   │       │   ├── FamilyController.java
│   │   │       │   ├── CategoryController.java
│   │   │       │   ├── AccountController.java
│   │   │       │   ├── TransactionController.java
│   │   │       │   ├── TransferController.java
│   │   │       │   ├── BudgetController.java
│   │   │       │   ├── DreamGoalController.java
│   │   │       │   ├── ReminderController.java
│   │   │       │   └── StatisticsController.java
│   │   │       ├── service/
│   │   │       │   ├── UserService.java
│   │   │       │   ├── FamilyService.java
│   │   │       │   ├── CategoryService.java
│   │   │       │   ├── AccountService.java
│   │   │       │   ├── TransactionService.java
│   │   │       │   ├── TransferService.java
│   │   │       │   ├── BudgetService.java
│   │   │       │   ├── DreamGoalService.java
│   │   │       │   ├── ReminderService.java
│   │   │       │   ├── StatisticsService.java
│   │   │       │   └── impl/
│   │   │       ├── mapper/
│   │   │       │   ├── UserMapper.java
│   │   │       │   ├── FamilyMapper.java
│   │   │       │   ├── FamilyMemberMapper.java
│   │   │       │   ├── CategoryMapper.java
│   │   │       │   ├── AccountMapper.java
│   │   │       │   ├── TransactionMapper.java
│   │   │       │   ├── TransferMapper.java
│   │   │       │   ├── BudgetMapper.java
│   │   │       │   ├── DreamGoalMapper.java
│   │   │       │   ├── SavingsRecordMapper.java
│   │   │       │   └── ReminderMapper.java
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   ├── Family.java
│   │   │       │   ├── FamilyMember.java
│   │   │       │   ├── Category.java
│   │   │       │   ├── Account.java
│   │   │       │   ├── Transaction.java
│   │   │       │   ├── Transfer.java
│   │   │       │   ├── Budget.java
│   │   │       │   ├── DreamGoal.java
│   │   │       │   ├── SavingsRecord.java
│   │   │       │   └── Reminder.java
│   │   │       ├── dto/
│   │   │       │   ├── UserDTO.java
│   │   │       │   ├── FamilyDTO.java
│   │   │       │   ├── TransactionDTO.java
│   │   │       │   ├── BudgetDTO.java
│   │   │       │   ├── DreamGoalDTO.java
│   │   │       │   └── LoginDTO.java
│   │   │       ├── vo/
│   │   │       │   ├── UserVO.java
│   │   │       │   ├── TransactionVO.java
│   │   │       │   ├── StatisticsVO.java
│   │   │       │   ├── BudgetAnalysisVO.java
│   │   │       │   ├── DreamGoalDashboardVO.java
│   │   │       │   └── PageVO.java
│   │   │       ├── common/
│   │   │       │   ├── result/
│   │   │       │   │   ├── Result.java
│   │   │       │   │   └── ResultCode.java
│   │   │       │   ├── exception/
│   │   │       │   │   ├── GlobalExceptionHandler.java
│   │   │       │   │   ├── BusinessException.java
│   │   │       │   │   └── ErrorCode.java
│   │   │       │   └── constants/
│   │   │       │       ├── UserConstants.java
│   │   │       │       ├── TransactionConstants.java
│   │   │       │       └── CommonConstants.java
│   │   │       ├── security/
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── UserDetailsServiceImpl.java
│   │   │       ├── utils/
│   │   │       │   ├── JwtUtil.java
│   │   │       │   ├── RedisUtil.java
│   │   │       │   ├── DateUtil.java
│   │   │       │   ├── ExcelUtil.java
│   │   │       │   └── WechatUtil.java
│   │   │       └── job/
│   │   │           ├── SavingsCalculateJob.java
│   │   │           ├── BudgetAlertJob.java
│   │   │           └── ReminderPushJob.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── mapper/
│   │       │   └── xml/ (如需XML映射)
│   │       └── db/
│   │           └── migration/
│   │               └── V1__init.sql
│   └── test/
│       └── java/
│           └── com/familybook/
├── pom.xml
└── README.md
```

---

## 6. 核心业务流程

### 6.1 微信登录流程

```
1. 小程序调用 wx.login() 获取 code
2. 小程序发送 code 到后端 /user/login
3. 后端用 code + appid + secret 请求微信接口获取 openid
4. 后端查询/创建用户记录
5. 后端生成 JWT Token 返回给小程序
6. 小程序存储 Token，后续请求携带在 Header 中
```

### 6.2 记账流程

```
1. 用户填写记账信息（金额、分类、账户、备注等）
2. 后端校验数据合法性
3. 保存交易记录到 t_transaction
4. 更新对应账户余额（原子操作）
5. 检查预算是否超支，触发预警
6. 检查梦想目标储蓄是否达标
7. 返回记账结果
```

### 6.3 梦想目标储蓄计算流程（定时任务）

```
XXL-Job 每月1日凌晨执行:

1. 查询所有"进行中"的梦想目标
2. 对每个目标:
   a. 计算本月应存金额（固定金额或百分比）
   b. 查询上月实际支出
   c. 计算上月实际可存金额
   d. 创建储蓄记录到 t_savings_record
   e. 更新目标已存金额
   f. 检查是否达标，发送预警或进度通知
3. 检查目标是否完成（saved_amount >= target_amount）
4. 更新目标状态为"已完成"
```

### 6.4 预算预警流程

```
1. 用户记账时，触发预算检查
2. 查询该分类本月预算
3. 计算本月该分类已支出金额
4. 计算支出占预算比例
5. 如果超过预警阈值且未预警过:
   a. 更新预算 is_alerted = 1
   b. 发送预警消息给用户
6. 如果是家庭共享账本，通知相关成员
```

---

## 7. 关键配置

### 7.1 application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: family-book
  datasource:
    url: jdbc:mysql://localhost:3306/family_book?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: status
      logic-delete-value: 0
      logic-not-delete-value: 1

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 604800000  # 7天

wx:
  miniapp:
    appid: ${WX_APPID:}
    secret: ${WX_SECRET:}

logging:
  level:
    com.familybook: DEBUG
```

### 7.2 pom.xml 核心依赖

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- Security + JWT -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>

    <!-- API文档 -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- 工具库 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.8.23</version>
    </dependency>
</dependencies>
```

---

## 8. 部署方案

### 8.1 单机部署

```
用户请求 → Nginx (反向代理+SSL) → Spring Boot应用 → MySQL/Redis
```

### 8.2 服务器建议配置

- **应用服务器**: 2核4G以上
- **数据库**: MySQL 8.0 + Redis 7.x
- **带宽**: 5Mbps以上
- **存储**: 50GB以上（根据图片存储需求调整）

### 8.3 部署步骤

1. 安装 JDK 17
2. 安装并配置 MySQL 8.0
3. 安装并配置 Redis 7.x
4. 导入数据库初始化脚本
5. 打包部署 Spring Boot 应用
6. 配置 Nginx 反向代理

---

## 9. 安全设计

### 9.1 认证机制
- 使用 JWT Token 进行身份认证
- Token 有效期 7 天，支持刷新
- 敏感操作需二次验证

### 9.2 数据安全
- 敏感字段加密存储（手机号等）
- SQL 注入防护（MyBatis-Plus）
- XSS 攻击防护
- 接口限流防刷

### 9.3 权限控制
- Spring Security 角色权限控制
- 家庭组数据隔离
- 操作权限校验（只能修改自己的数据）

---

## 10. 性能优化

### 10.1 数据库优化
- 合理建立索引（已在表结构中定义）
- 分页查询，避免大数据量返回
- 复杂统计使用汇总表或缓存

### 10.2 缓存策略
- 用户会话存储 Redis
- 热点数据缓存（分类列表等）
- 统计数据缓存，定时刷新

### 10.3 其他优化
- 异步处理（消息通知、数据统计）
- 连接池配置优化
- 日志级别生产环境调整为 WARN
