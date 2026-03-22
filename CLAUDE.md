# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供本代码仓库的工作指南。

## 项目概述

家庭记账小程序是一款基于 Java Spring Boot 后端开发的微信小程序，帮助用户记录日常收支、管理家庭财务、分析消费习惯，并通过梦想目标管理实现强制储蓄。

**关键文档：**
- `plan.md` - 功能需求文档（9个模块，40+功能点）
- `tech-plan.md` - 完整技术设计方案（数据库设计、API设计、架构图）

## 技术栈

**后端：**
- Spring Boot 3.2.x (JDK 17)
- MyBatis-Plus 3.5.x (ORM框架)
- MySQL 8.0 + Redis 7.x
- Spring Security + JWT (认证授权)
- XXL-Job 2.4.x (定时任务)
- SpringDoc OpenAPI 2.3.x (API文档)

**前端：**
- 微信小程序

## 项目结构（后端）

```
family-book-server/
├── src/main/java/com/familybook/
│   ├── controller/          # 控制器层 - REST API接口
│   ├── service/             # 业务层 - 业务逻辑
│   ├── mapper/              # 数据访问层 - MyBatis-Plus
│   ├── entity/              # 实体层 - 数据模型
│   ├── dto/                 # 数据传输对象 - 请求/响应
│   ├── vo/                  # 视图对象 - 返回前端的数据
│   ├── config/              # 配置类
│   ├── common/              # 公共组件
│   ├── security/            # JWT与安全相关
│   ├── utils/               # 工具类
│   └── job/                 # XXL-Job定时任务
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/        # 数据库迁移脚本
└── pom.xml
```

## 构建命令

```bash
# 构建项目
mvn clean package

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 运行单个测试方法
mvn test -Dtest=UserServiceTest#testLogin

# 使用开发环境配置运行
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# API文档（启动后访问 /swagger-ui.html）
# 自动生成，无需额外命令
```

## 数据库设计

**11张核心表：**
- `t_user` - 用户表（基于openid的微信登录）
- `t_family` / `t_family_member` - 家庭组与家庭成员
- `t_category` - 收支分类表
- `t_account` - 账户表（现金、银行卡、支付宝等）
- `t_transaction` - 交易记录表（核心记账表）
- `t_transfer` - 转账记录表
- `t_budget` - 预算表
- `t_dream_goal` / `t_savings_record` - 梦想目标与储蓄记录
- `t_reminder` - 记账提醒表

完整DDL SQL 参见 `tech-plan.md` 第3节。

## API设计

**基础路径：** `/api/v1`

**9个模块：**
1. `/user` - 微信登录、用户信息
2. `/family` - 家庭组管理
3. `/category` - 分类管理
4. `/account` - 账户管理
5. `/transaction` - 核心记账接口
6. `/transfer` - 转账记录
7. `/budget` - 预算设置与分析
8. `/dream-goal` - 储蓄目标与看板
9. `/reminder` - 提醒设置

**统一响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 核心业务逻辑

### 1. 微信登录流程
```
wx.login() → 获取code → POST /user/login → 换取openid → 生成JWT → 返回token
```

### 2. 记账流程
- 保存到 `t_transaction`
- 原子操作更新账户余额
- 检查预算阈值 → 超支则触发预警
- 检查梦想目标进度

### 3. 月度储蓄计算（XXL-Job定时任务）
- 每月1日凌晨执行
- 计算计划储蓄金额（固定金额或收入百分比）
- 创建 `t_savings_record` 记录
- 更新 `t_dream_goal.saved_amount`
- 发送完成提醒或预警通知

### 4. 数据权限模型
- 用户只能访问自己的数据
- 家庭成员可访问共享的家庭数据
- 基于角色：管理员(1) vs 普通成员(0)

## 关键实现注意事项

### 软删除
所有实体使用 `status` 字段实现软删除：
- `status = 1` - 正常
- `status = 0` - 已删除（由MyBatis-Plus全局配置自动过滤）

### ID生成
使用 MyBatis-Plus `assign_id`（雪花算法）生成分布式ID。

### 金融数据精度
- 所有金额字段使用 `DECIMAL(15,2)`
- 绝不要使用 float/double 进行金融计算

### 家庭数据隔离
大部分查询应按以下条件过滤：
- `user_id = currentUserId`（个人数据）
- `family_id IN (用户所属家庭ID列表)`（共享数据）

## 环境配置

生产环境所需环境变量：
```bash
DB_USERNAME=
DB_PASSWORD=
REDIS_PASSWORD=
JWT_SECRET=
WX_APPID=       # 微信小程序AppId
WX_SECRET=      # 微信小程序Secret
```

## 开发阶段

1. **第一阶段（1-2周）：** 用户登录、记账基础CRUD
2. **第二阶段（3-4周）：** 分类、账户、统计API
3. **第三阶段（5-6周）：** 家庭组、预算、梦想目标
4. **第四阶段（第7周）：** 优化、测试、部署
