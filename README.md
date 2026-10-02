# 星际列车订票系统（Star Train Ticket Booking System）

<p align="center">
  <strong>基于 Spring Boot 3 + MyBatis-Plus + Redis 的星际列车订票系统后端服务</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.1.12-6DB33F?logo=springboot" alt="Spring Boot 3.1.12">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.10-red" alt="MyBatis-Plus 3.5.10">
  <img src="https://img.shields.io/badge/MySQL-8.3-4479A1?logo=mysql" alt="MySQL 8.3">
  <img src="https://img.shields.io/badge/Redis-7-DC382D?logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="MIT License">
</p>

---

## 📖 项目简介

本项目是一个面向**星际旅行**场景的列车订票系统后端服务。系统以「星系—星球站点—列车—车次—订单」为核心业务模型，为乘客提供站点查询、车次查询、在线购票、订单管理、退票等功能，同时为管理员提供站点、列车、车次、订单的全套后台管理能力。

项目在实现基础票务业务的同时，重点解决了**高并发抢票场景下的超卖问题**，采用 **Redis + Lua 脚本原子扣减库存**、**Redisson 分布式锁防重复下单**、**数据库乐观扣减兜底与补偿** 等多层防护机制，保证库存数据在并发环境下的一致性与准确性。

> 💡 命名说明：仓库名为 `star-train-ticket-booking-system`，父工程 artifactId 为 `take-out`（沿用自项目模板），业务代码包名为 `com.star`。

---

## ✨ 核心功能

### 🚄 用户端（乘客）

| 模块 | 功能说明 |
| --- | --- |
| 用户账号 | 注册、登录（JWT 鉴权）、查看个人信息、修改个人信息、修改密码 |
| 站点查询 | 分页查询所有**启用状态**的星球站点 |
| 车次查询 | 按出发站、到达站、出发时间范围分页查询**可售票**车次，实时展示余票比例 |
| 在线购票 | 选车次下单购票，自动计算总价、生成订单号、原子扣减库存 |
| 订单管理 | 分页查询个人订单（支持按订单状态筛选），按创建时间倒序展示 |
| 在线退票 | 对未发车、未退票的订单发起退票，自动恢复车次余票 |

### 🛠️ 管理端（管理员）

| 模块 | 功能说明 |
| --- | --- |
| 管理员账号 | 登录（JWT 鉴权）、查看信息、修改信息、修改密码 |
| 站点管理 | 新增、修改、批量删除、分页查询站点（星球名称、所属星系、启停状态） |
| 列车管理 | 新增、修改、批量删除、分页查询列车（车次编号、总座位数、运营状态） |
| 车次管理 | 新增、修改、批量删除、分页查询车次（含发车时间冲突校验、站点/列车状态校验） |
| 订单管理 | 按用户名/车次/状态/下单时间区间分页查询订单，修改订单、批量删除订单 |
| 库存运维 | 手动预热指定车次的 Redis 库存缓存 |

---

## 🏗️ 技术栈

| 分类 | 技术 | 版本 | 说明 |
| --- | --- | --- | --- |
| 语言 | Java | 17 | 使用 Record、Switch 表达式等新特性 |
| 框架 | Spring Boot | 3.1.12 | 基础应用框架（Jakarta EE 9+） |
| ORM | MyBatis-Plus | 3.5.10 | 简化 CRUD、内置分页插件 |
| 数据库 | MySQL | 8.3.0 | 业务数据持久化 |
| 缓存 | Redis + Lettuce | — | 库存缓存、高性能读写 |
| 分布式锁 | Redisson | 3.27.2 | 可重入分布式锁，防重复下单 |
| 连接池 | Apache Commons Pool2 | 2.12.0 | Redis 连接池支持 |
| 认证 | JJWT | 0.11.5 | JWT 令牌生成与校验 |
| 接口文档 | Knife4j / SpringDoc | 4.3.0 / 2.3.0 | OpenAPI 3 文档与调试界面 |
| 工具 | Lombok | 1.18.34 | 简化实体类样板代码 |
| 构建 | Maven | 3.12.1 (compiler plugin) | 多模块聚合构建 |

---

## 📂 项目结构

```
star-train-ticket-booking-system/
├── README.md
└── take-out/                              # Maven 父工程
    ├── pom.xml                            # 父 POM：依赖与版本统一管理
    │
    ├── star-common/                       # 公共模块
    │   └── src/main/java/com/star/
    │       ├── constant/                  # 常量（状态、消息、JWT、自动填充字段）
    │       ├── context/                   # BaseContext：ThreadLocal 保存当前登录用户 ID
    │       ├── enumeration/               # OperationType：数据库操作类型枚举
    │       ├── exception/                 # 自定义业务异常体系
    │       ├── json/                      # JacksonObjectMapper：时间格式定制
    │       ├── properties/                # JwtProperties：JWT 配置属性绑定
    │       ├── result/                    # Result / PageResult：统一响应封装
    │       └── utils/                     # JwtUtil、HttpClientUtil 工具类
    │
    ├── star-pojo/                         # 实体与数据传输对象模块
    │   └── src/main/java/com/star/
    │       ├── entity/                    # User、Station、Train、Schedule、Order
    │       ├── dto/                       # 入参对象（登录、注册、分页查询、购票等）
    │       └── vo/                        # 出参对象（含关联名称与计算字段）
    │
    └── star-server/                       # 业务服务模块（Web 层）
        └── src/main/
            ├── java/com/star/
            │   ├── annotation/            # @AutoFill 自动填充注解
            │   ├── aspect/                # AutoFillAspect：公共字段自动填充切面
            │   ├── config/                # WebMvcConfiguration、RedisConfig
            │   ├── controller/
            │   │   ├── admin/             # 管理端接口
            │   │   └── user/              # 用户端接口
            │   ├── handler/               # GlobalExceptionHandler 全局异常处理
            │   ├── interceptor/           # JWT 拦截器（admin / user 双通道）
            │   ├── mapper/                # MyBatis-Plus Mapper 接口
            │   ├── service/               # 业务接口与实现
            │   └── StarServerApplication  # 启动类
            └── resources/
                ├── application.yml        # 主配置（支持环境变量注入密钥）
                ├── application-local.yml  # 本地配置（含明文密码，已加入 .gitignore）
                └── lua/deduct_stock.lua   # 库存原子扣减 Lua 脚本
```

---

## 🗄️ 数据模型

系统包含 5 张核心业务表：

```
                        ┌─────────────┐
                        │   t_user    │  用户 / 管理员（role 区分）
                        └──────┬──────┘
                               │ 1
                               │
                               │ N
   ┌─────────────┐      ┌──────┴──────┐      ┌─────────────┐
   │ t_station   │      │   t_order   │      │   t_train   │
   │  站点(星球)  │      └──────┬──────┘      │    列车      │
   └──────┬──────┘             │ N           └──────┬──────┘
          │ 1                  │                    │ 1
          │                    │                    │
          │ N            ┌─────┴──────┐             │ N
          └──────────────┤ t_schedule ├─────────────┘
              (出发/到达)  │   车次      │
                         └────────────┘
```

| 表名 | 说明 | 关键字段 |
| --- | --- | --- |
| `t_user` | 用户表（含管理员） | `user_id`、`username`、`password`(MD5)、`real_name`、`phone`、`role` |
| `t_station` | 站点表（星球） | `station_id`、`station_code`、`planet_name`、`galaxy_name`、`status` |
| `t_train` | 列车表 | `train_id`、`train_code`、`total_seats`、`status` |
| `t_schedule` | 车次表 | `schedule_id`、`train_id`、`departure/arrival_station_id`、`departure/arrival_time`、`ticket_price`、`remaining_seats`、`status` |
| `t_order` | 订单表 | `order_id`、`order_no`、`user_id`、`schedule_id`、`ticket_count`、`total_amount`、`status` |

### 状态枚举（业务字典）

| 维度 | 状态值 |
| --- | --- |
| 站点 | `启用` / `禁用` |
| 列车 | `运营中` / `维护中` / `停运` |
| 车次 | `可售票` / `已停售` / `已发车` |
| 订单 | `已支付` / `已退票` / `已完成` |

---

## 🔥 高并发购票设计（项目核心亮点）

抢票场景下最大的风险是**超卖**与**重复下单**。本项目在 `PurchaseServiceImpl` 中构建了完整的防护链路：

### 1️⃣ 基础业务校验

```
车次是否存在 → 车次是否处于「可售票」 → 是否已过发车时间
```

### 2️⃣ Redis + Lua 原子扣减库存（防超卖第一道防线）

传统「查询库存 → 判断 → 扣减」在并发下存在竞态条件。本项目将**判断与扣减合并为一次 Redis 原子操作**：

```lua
-- lua/deduct_stock.lua
local stock = redis.call('GET', stockKey)
if not stock then return -2 end          -- 缓存不存在
stock = tonumber(stock)
if stock < quantity then return -1 end   -- 库存不足
return redis.call('DECRBY', stockKey, quantity)  -- 原子扣减
```

**优势**：Redis 单线程执行 Lua 脚本，脚本执行期间不会被其他命令打断，从根本上杜绝超卖。

**懒加载机制**：若缓存 Key 不存在（返回 `-2`），系统会通过 `SETNX` 从数据库安全加载库存并自动重试一次，避免因缓存未预热导致购票失败。

### 3️⃣ 数据库条件扣减 + 自动补偿

```sql
UPDATE t_schedule SET remaining_seats = remaining_seats - #{count}
WHERE schedule_id = #{scheduleId} AND remaining_seats >= #{count}
```

利用 SQL 的 `WHERE` 条件保证数据库层不会出现负库存。若数据库扣减影响行数为 0（极端的缓存与库不一致场景），则**自动回滚 Redis 库存**并抛出异常，保证两侧最终一致。

### 4️⃣ Redisson 分布式锁（防重复下单）

```
锁粒度：order:lock:{userId}:{scheduleId}
```

同一用户对同一车次加锁后，在锁内执行**双重检查**（查询是否已存在有效订单），彻底避免同一用户重复下单。有效订单状态为 `已支付`、`已完成`，已退票的订单允许重新购买。

### 5️⃣ 事务保证

整个购票流程由 `@Transactional(rollbackFor = Exception.class)` 包裹，任何环节异常都会回滚订单写入，同时对已扣减的 Redis 库存进行补偿。

> 📌 **完整链路**：业务校验 → Lua 原子扣减 → DB 条件扣减 → 分布式锁 → 双重检查 → 生成订单

---

## 🔐 认证与鉴权设计

系统采用**双通道 JWT 认证**，用户端与管理员端使用**完全独立的密钥体系**：

| 通道 | 拦截路径 | 令牌请求头 | 密钥配置项 | 有效期 |
| --- | --- | --- | --- | --- |
| 用户端 | `/user/**` | `authentication` | `com.jwt.user-secret-key` | 7200000 ms |
| 管理端 | `/admin/**` | `token` | `com.jwt.admin-secret-key` | 7200000 ms |

**工作流程**：

1. 登录成功后，服务端通过 `JwtUtil.createJWT()` 生成令牌，Payload 中携带用户/管理员 ID；
2. 客户端后续请求在指定请求头中携带令牌；
3. 拦截器 `JwtTokenAdminInterceptor` / `JwtTokenUserInterceptor` 校验令牌，解析出 ID 并存入 `BaseContext`（基于 `ThreadLocal`）；
4. 业务层通过 `BaseContext.getCurrentId()` 获取当前登录用户，无需层层传参；
5. 校验失败返回 **401** 状态码。

**放行路径**：登录、注册接口，以及 Knife4j 文档相关路径（`/doc.html`、`/webjars/**`、`/v3/**` 等）。

---

## 🚀 快速开始

### 环境要求

| 组件 | 版本要求 |
| --- | --- |
| JDK | 17 及以上 |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 6.0+（推荐 7.x） |

### 1. 克隆项目

```bash
git clone https://github.com/<your-username>/star-train-ticket-booking-system.git
cd star-train-ticket-booking-system
```

### 2. 初始化数据库

创建数据库并导入表结构与初始数据：

```sql
CREATE DATABASE star_train_ticket_booking_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

> 项目根目录未附带 SQL 脚本时，请根据上文「数据模型」章节自行建表，或联系作者获取初始化脚本。

### 3. 配置数据源与 Redis

主配置文件 `take-out/star-server/src/main/resources/application.yml` 已通过**环境变量**注入敏感信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/star_train_ticket_booking_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:}
  data:
    redis:
      host: localhost
      port: 6380
      password: ${RD_PASSWORD}
      database: 0

com:
  jwt:
    admin-secret-key: ${JWT_ADMIN_SECRET:}
    user-secret-key: ${JWT_USER_SECRET:}
```

**启动前需设置以下环境变量**：

| 环境变量 | 说明 | 示例 |
| --- | --- | --- |
| `DB_PASSWORD` | MySQL 数据库密码 | `your_db_password` |
| `RD_PASSWORD` | Redis 访问密码 | `your_redis_password` |
| `JWT_ADMIN_SECRET` | 管理员端 JWT 签名密钥 | 至少 32 位随机字符串 |
| `JWT_USER_SECRET` | 用户端 JWT 签名密钥 | 至少 32 位随机字符串（勿与管理端相同） |

**Windows PowerShell**：

```powershell
$env:DB_PASSWORD="your_db_password"
$env:RD_PASSWORD="your_redis_password"
$env:JWT_ADMIN_SECRET="your_admin_secret_key_at_least_32_chars"
$env:JWT_USER_SECRET="your_user_secret_key_at_least_32_chars"
```

**Linux / macOS**：

```bash
export DB_PASSWORD="your_db_password"
export RD_PASSWORD="your_redis_password"
export JWT_ADMIN_SECRET="your_admin_secret_key_at_least_32_chars"
export JWT_USER_SECRET="your_user_secret_key_at_least_32_chars"
```

> ⚠️ **注意**：Redis 默认端口在配置中为 **6380**，请根据实际部署情况调整。

### 4. 构建与启动

```bash
# 进入父工程目录
cd take-out

# 编译打包（跳过测试）
mvn clean package -DskipTests

# 方式一：Maven 插件启动
mvn spring-boot:run -pl star-server

# 方式二：运行打包产物
java -jar star-server/target/star-server-0.0.1-SNAPSHOT.jar

# 方式三：IDEA 中直接运行 StarServerApplication
```

启动成功后，服务监听 **`http://localhost:8080`**。

### 5. 访问接口文档

启动后浏览器打开 Knife4j 文档界面：

```
http://localhost:8080/doc.html
```

---

## 📡 接口一览

### 用户端接口

| 方法 | 路径 | 说明 | 是否需登录 |
| --- | --- | --- | --- |
| POST | `/user/self/register` | 注册用户 | ❌ |
| POST | `/user/self/login` | 登录用户 | ❌ |
| POST | `/user/self/view` | 查看用户信息 | ✅ |
| POST | `/user/self/update` | 修改个人信息 | ✅ |
| PUT | `/user/self/password` | 修改用户密码 | ✅ |
| GET | `/user/page/station` | 站点分页查询 | ✅ |
| GET | `/user/page/schedule` | 车次分页查询 | ✅ |
| POST | `/user/purchase` | 购票 | ✅ |
| GET | `/user/order/page/order` | 个人订单分页查询 | ✅ |
| POST | `/user/refund` | 退票 | ✅ |

### 管理端接口

| 方法 | 路径 | 说明 | 是否需登录 |
| --- | --- | --- | --- |
| POST | `/admin/self/login` | 登录管理员 | ❌ |
| POST | `/admin/self/view` | 查看管理员信息 | ✅ |
| POST | `/admin/self/update` | 修改管理员信息 | ✅ |
| PUT | `/admin/self/password` | 修改管理员密码 | ✅ |
| POST | `/admin/station` | 新增站点 | ✅ |
| PUT | `/admin/station` | 修改站点 | ✅ |
| DELETE | `/admin/station` | 批量删除站点 | ✅ |
| GET | `/admin/station/page` | 站点分页查询 | ✅ |
| POST | `/admin/train` | 新增列车 | ✅ |
| PUT | `/admin/train` | 修改列车 | ✅ |
| DELETE | `/admin/train` | 批量删除列车 | ✅ |
| GET | `/admin/train/page` | 列车分页查询 | ✅ |
| POST | `/admin/schedule` | 新增车次 | ✅ |
| PUT | `/admin/schedule` | 修改车次 | ✅ |
| DELETE | `/admin/schedule` | 批量删除车次 | ✅ |
| GET | `/admin/schedule/page` | 车次分页查询 | ✅ |
| GET | `/admin/order/page` | 订单分页查询 | ✅ |
| PUT | `/admin/order` | 修改订单 | ✅ |
| DELETE | `/admin/order` | 批量删除订单 | ✅ |
| POST | `/admin/stock/init/{scheduleId}` | 预热车次 Redis 库存 | ✅ |

> 完整的请求参数与响应结构请以 `/doc.html` 在线文档为准。

---

## 📐 统一响应格式

所有接口均返回统一封装结构 `Result<T>`：

```json
{
  "code": 1,
  "msg": null,
  "data": { }
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | Integer | **1 表示成功**，0 或其他数字表示失败 |
| `msg` | String | 错误提示信息，成功时为 `null` |
| `data` | T | 业务数据（对象、列表或分页结果） |

分页接口的 `data` 为 `PageResult` 结构：

```json
{
  "total": 128,
  "records": [ ]
}
```

### 全局异常处理

`GlobalExceptionHandler` 统一捕获并转换异常：

| 异常类型 | 处理方式 |
| --- | --- |
| `BaseException` | 返回具体业务错误信息 |
| `SQLIntegrityConstraintViolationException` | 解析唯一键冲突，返回「xxx 已存在」 |
| `Exception` | 兜底处理，记录完整堆栈，返回「未知错误」 |

---

## 🎯 工程实践

| 实践 | 说明 |
| --- | --- |
| **多模块分层** | `common`（通用能力）/ `pojo`（数据模型）/ `server`（业务实现）职责清晰，便于复用与独立演进 |
| **DTO / VO 隔离** | 入参与出参和数据库实体分离，避免实体直接暴露，VO 中可携带关联名称与计算字段 |
| **AOP 自动填充** | `@AutoFill` 注解 + 切面自动填充 `createTime`、`updateTime` 等公共字段，业务代码零侵入 |
| **批量查询优化** | 车次列表查询通过 `selectBatchIds` + `Map` 转换，将 N+1 次查询优化为固定次数的批量查询 |
| **ThreadLocal 上下文** | `BaseContext` 保存当前登录用户，业务层随处可取，避免参数层层透传 |
| **统一消息常量** | `MessageConstant` / `StatusConstant` 集中管理提示语与状态字典，避免魔法值散落 |
| **环境变量注入密钥** | 主配置文件的数据库密码、Redis 密码、JWT 密钥均通过环境变量读取 |

---

## 🔒 安全说明

本项目为学习与演示用途，投入生产环境前建议进行以下加固：

- [ ] **密码加密升级**：当前使用 MD5 存储密码，建议升级为 BCrypt / Argon2 等加盐哈希算法
- [ ] **密钥管理**：JWT 密钥与数据库密码务必通过环境变量或密钥管理服务注入，切勿提交到版本库
- [ ] **CORS 策略**：当前未启用全局跨域配置，生产环境需按域名白名单精细化配置
- [ ] **限流与防刷**：建议在网关层对登录、购票接口增加限流与验证码机制
- [ ] **日志脱敏**：避免在日志中输出完整令牌与用户敏感信息
- [ ] **订单号生成**：当前使用「时间戳 + 随机数」，高并发下建议替换为雪花算法或 Redis 自增序列
- [ ] **管理员角色校验**：`AdminSelfServiceImpl` 中角色判断使用了 `==` 比较字符串，建议改为 `equals()`

---

## 🛣️ 后续规划

- [ ] 接入消息队列（RocketMQ / RabbitMQ）实现异步下单与削峰填谷
- [ ] 增加订单超时未支付自动取消机制
- [ ] 引入 Elasticsearch 支持车次的全文检索
- [ ] 补充单元测试与集成测试，接入 CI/CD 流水线
- [ ] 增加座位选择与多乘客出行支持

---

## 🤝 贡献指南

欢迎提交 Issue 与 Pull Request：

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交改动：`git commit -m "feat: add some feature"`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源，可自由用于学习、修改与分发。

---

<p align="center">
  ⭐ 如果这个项目对你有帮助，欢迎点一个 Star！
</p>
