# 团购商城 - 项目文档

> 基于 DDD（领域驱动设计）的团购交易系统，支持拼团锁单、结算、退款全流程。

---

## 一、项目简介

本项目为一个完整的团购营销交易系统，采用 DDD 分层架构，涵盖营销试算、锁单、支付结算、退款等完整交易链路。

- **技术栈**：Spring Boot 2.7.12 + JDK 8 + MyBatis + MySQL 8 + Redis(Redisson) + RabbitMQ
- **架构**：DDD 六层架构（api / app / domain / infrastructure / trigger / types）
- **作者**：小傅哥（@bugstack.cn），基于 Apache License 2.0 开源

---

## 二、工程结构

```
group-buy-market
├── group-buy-market-api           ← 对外接口层（DTO定义、Service接口）
├── group-buy-market-app           ← 应用层（启动类、配置、单元测试）
├── group-buy-market-domain        ← 领域层（核心业务逻辑、聚合根、策略模式）
├── group-buy-market-infrastructure ← 基础设施层（DAO、Repository、Redis、MQ、网关）
├── group-buy-market-trigger       ← 触发层（HTTP Controller、定时任务、MQ监听）
└── group-buy-market-types         ← 通用模块（枚举、异常、事件、常量）
```

### 各层职责

| 模块 | 职责 |
|------|------|
| **api** | 定义对外暴露的 REST 接口契约、请求/响应 DTO |
| **app** | Spring Boot 启动入口、配置管理、集成测试 |
| **domain** | 核心业务逻辑：活动试算、锁单、结算、退款、策略工厂 |
| **infrastructure** | 数据库访问（MyBatis）、Redis 缓存、RabbitMQ 消息、外部网关调用 |
| **trigger** | 触发入口：HTTP Controller、定时 Job、MQ Listener |
| **types** | 通用常量、枚举、异常、事件基类 |

---

## 三、API 接口清单

### 服务地址

```
http://localhost:8091
```

### 1. 营销首页 - 查询拼团配置

| 项目 | 值 |
|------|-----|
| **路径** | `POST /api/v1/gbm/index/query_group_buy_market_config` |
| **功能** | 查询商品的拼团营销配置，试算优惠价格，返回正在拼团的队伍和统计数据 |

**请求参数：**

```json
{
    "userId": "xfg01",
    "source": "s01",
    "channel": "c01",
    "goodsId": "9890001"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户ID |
| source | String | 渠道（如 s01） |
| channel | String | 来源（如 c01） |
| goodsId | String | 商品ID |

**响应示例：**

```json
{
    "code": "0000",
    "info": "成功",
    "data": {
        "activityId": 100123,
        "goods": {
            "goodsId": "9890001",
            "originalPrice": 100.00,
            "deductionPrice": 20.00,
            "payPrice": 80.00
        },
        "teamList": [
            {
                "userId": "xfg03",
                "teamId": "29487599",
                "activityId": 100123,
                "targetCount": 3,
                "completeCount": 0,
                "lockCount": 2,
                "validStartTime": "2025-04-05 14:54:47",
                "validEndTime": "2025-04-05 15:09:47",
                "validTimeCountdown": "00:05:23",
                "outTradeNo": "769515763172"
            }
        ],
        "teamStatistic": {
            "allTeamCount": 10,
            "allTeamCompleteCount": 3,
            "allTeamUserCount": 25
        }
    }
}
```

---

### 2. 交易锁单

| 项目 | 值 |
|------|-----|
| **路径** | `POST /api/v1/gbm/trade/lock_market_pay_order` |
| **功能** | 用户参团锁单，创建或加入拼团队伍，冻结库存 |

**请求参数：**

```json
{
    "userId": "xfg01",
    "teamId": null,
    "activityId": 100123,
    "goodsId": "9890001",
    "source": "s01",
    "channel": "c01",
    "outTradeNo": "123456789012",
    "notifyConfigVO": {
        "notifyType": "HTTP",
        "notifyUrl": "http://127.0.0.1:8091/api/v1/test/group_buy_notify",
        "notifyMQ": null
    }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户ID |
| teamId | String | 拼单组队ID，为空则创建新队伍 |
| activityId | Long | 活动ID |
| goodsId | String | 商品ID |
| source | String | 渠道 |
| channel | String | 来源 |
| outTradeNo | String | 外部交易单号（需唯一） |
| notifyConfigVO.notifyType | String | 回调方式：HTTP 或 MQ |
| notifyConfigVO.notifyUrl | String | HTTP 回调地址 |
| notifyConfigVO.notifyMQ | String | MQ 路由key |

**响应示例：**

```json
{
    "code": "0000",
    "info": "成功",
    "data": {
        "orderId": "146024339576",
        "originalPrice": 100.00,
        "deductionPrice": 20.00,
        "payPrice": 80.00,
        "tradeOrderStatus": 0,
        "teamId": "15721600"
    }
}
```

---

### 3. 交易结算

| 项目 | 值 |
|------|-----|
| **路径** | `POST /api/v1/gbm/trade/settlement_market_pay_order` |
| **功能** | 支付完成后组队结算，拼团人数+1，判断是否成团 |

**请求参数：**

```json
{
    "userId": "xfg01",
    "source": "s01",
    "channel": "c01",
    "outTradeNo": "123456789012",
    "outTradeTime": 1720000000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户ID |
| source | String | 渠道 |
| channel | String | 来源 |
| outTradeNo | String | 外部交易单号（锁单时传入的） |
| outTradeTime | Long | 外部交易时间（毫秒时间戳） |

**响应示例：**

```json
{
    "code": "0000",
    "info": "成功",
    "data": {
        "userId": "xfg01",
        "teamId": "15721600",
        "activityId": 100123,
        "outTradeNo": "123456789012"
    }
}
```

---

### 4. 交易退款

| 项目 | 值 |
|------|-----|
| **路径** | `POST /api/v1/gbm/trade/refund_market_pay_order` |
| **功能** | 退单退款，恢复锁单量库存 |

**请求参数：**

```json
{
    "userId": "xfg01",
    "outTradeNo": "123456789012",
    "source": "s01",
    "channel": "c01"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户ID |
| outTradeNo | String | 外部交易单号 |
| source | String | 渠道 |
| channel | String | 来源 |

**响应示例：**

```json
{
    "code": "0000",
    "info": "成功",
    "data": {
        "userId": "xfg01",
        "orderId": "146024339576",
        "teamId": "15721600",
        "code": "0000",
        "info": "成功"
    }
}
```

---

### 5. DCC 动态配置

| 项目 | 值 |
|------|-----|
| **路径** | `GET /api/v1/gbm/dcc/update_config?key=xxx&value=xxx` |
| **功能** | 运行时动态调整系统配置（降级开关、限流开关、切量范围） |

**配置项说明：**

| key | 说明 | 可选值 |
|-----|------|--------|
| downgradeSwitch | 降级开关 | 0=正常, 1=降级 |
| cutRange | 切量范围 | 0=全部, 其他值=部分 |
| rateLimiterSwitch | 限流开关 | open=开启, close=关闭 |

**示例：**

```
curl http://localhost:8091/api/v1/gbm/dcc/update_config?key=downgradeSwitch&value=1
curl http://localhost:8091/api/v1/gbm/dcc/update_config?key=rateLimiterSwitch&value=close
```

---

### 6. 模拟回调通知

| 项目 | 值 |
|------|-----|
| **路径** | `POST /api/v1/test/group_buy_notify` |
| **功能** | 模拟第三方服务接收拼团结果回调通知 |

**请求参数：**

```json
{
    "teamId": "15721600",
    "outTradeNoList": ["123456789012", "234567890123"]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| teamId | String | 组队ID |
| outTradeNoList | List\<String\> | 外部交易单号列表 |

**响应：** `success`

---

## 四、业务流程

```
用户浏览商品
    ↓
查询营销配置 → 看到试算价格 + 正在拼团的队伍
    ↓
选择参团（或开新团）
    ↓
锁单 → 创建订单（状态=锁定中），冻结库存
    ↓
支付完成 → 结算（拼团人数+1，判断是否成团）
    ↓
成团成功 → 触发回调通知（HTTP/MQ）
    ↓
（可选）退款 → 恢复库存
```

### 订单状态流转

```
锁单 → 状态=0（锁定中/待支付）
    ↓ 结算
状态=1（已结算/支付完成）
    ↓ 退款
状态=2（已退款）
```

---

## 五、数据库表结构

| 表名 | 说明 |
|------|------|
| `group_buy_activity` | 拼团活动表（活动ID、名称、目标人数、有效期） |
| `group_buy_discount` | 折扣表（直减/满减/N元购） |
| `group_buy_order` | 拼团组队表（队伍ID、进度、状态） |
| `group_buy_order_list` | 订单明细表（每个用户的锁单记录） |
| `sku` | 商品信息表（商品ID、名称、原价） |
| `sc_sku_activity` | 渠道商品活动关联表 |
| `crowd_tags` | 人群标签表 |
| `crowd_tags_detail` | 人群标签明细表（用户-标签关系） |
| `crowd_tags_job` | 人群标签任务表 |
| `notify_task` | 回调通知任务表 |

---

## 六、核心设计模式

| 模式 | 应用场景 |
|------|----------|
| **策略模式** | 折扣计算：直减(ZJ)、满减(MJ)、N元购(N) |
| **模板模式** | 拼团试算链路：查询活动→查询商品→计算折扣→人群校验 |
| **工厂模式** | `DefaultActivityStrategyFactory` - 根据活动类型创建对应策略 |
| **责任链** | 试算节点：RootNode → TagNode → MarketNode → EndNode |
| **事件驱动** | RabbitMQ 处理成团成功/退款成功事件 |
| **分布式锁** | Redisson 防止重复回调、重复退款 |

---

## 七、测试数据

| 类型 | 值 |
|------|-----|
| 活动ID | `100123` |
| 商品ID | `9890001` |
| 商品名称 | 《手写MyBatis：渐进式源码实践》 |
| 原价 | `100.00` |
| 优惠 | `20.00`（直减） |
| 实付 | `80.00` |
| 渠道/来源 | `s01` / `c01` |
| 测试用户 | `xfg01` ~ `xfg09` |
| 已有队伍 | `29487599`, `15721600`, `21762498` 等 |

---

## 八、部署运行

### 环境要求

- JDK 8
- MySQL 8.x
- Redis 6.x
- RabbitMQ 3.x（可选，不部署需禁用相关配置）

### 启动步骤

```bash
# 1. 初始化数据库
mysql -uroot -p < docs/dev-ops/mysql/sql/2-29-group_buy_market.sql

# 2. 修改配置（application-dev.yml）
#    - MySQL 连接信息
#    - Redis 连接信息

# 3. 编译
mvn clean install -DskipTests

# 4. 启动
mvn spring-boot:run -pl group-buy-market-app
```

### 验证

```
http://localhost:8091/actuator/health
```

---

## 九、前端项目

### 技术栈

- **框架**：Vue 3 (Composition API + `<script setup>`)
- **构建工具**：Vite
- **UI 库**：Element Plus
- **HTTP 客户端**：Axios
- **路由**：Vue Router 4

### 目录结构

```
frontend/
├── src/
│   ├── api/index.js         ← API 请求封装（对接全部后端接口）
│   ├── assets/main.css      ← 全局样式
│   ├── router/index.js      ← 路由配置
│   ├── utils/request.js     ← Axios 实例 + 拦截器
│   ├── views/
│   │   ├── HomeView.vue     ← 营销首页（查询配置、试算价格、队伍列表）
│   │   ├── TradeView.vue    ← 交易下单（锁单、结算、退款）
│   │   ├── OrderView.vue    ← 订单管理（本地订单列表）
│   │   ├── DccView.vue      ← DCC 动态配置（降级/限流开关）
│   │   └── TestView.vue     ← 模拟回调 + 日志面板
│   ├── App.vue              ← 主布局（侧边栏导航 + 用户信息）
│   └── main.js              ← 入口（注册 Element Plus + Router）
├── dist/                    ← 构建产物（npm run build 生成）
└── vite.config.js           ← Vite 配置（@ 别名 + API 代理）
```

### 启动方式

```bash
cd frontend
npm install
npm run dev     # 开发模式（http://localhost:5173）
npm run build   # 构建产物到 dist/
```

### 部署方式

1. **开发模式**：`npm run dev`，Vite 会自动代理 `/api` 请求到 `http://localhost:8091`
2. **生产部署**：`npm run build` 生成 `dist/`，将 `dist/` 文件夹放到 Nginx 即可

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8091;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 功能页面

| 页面 | 路由 | 功能 |
|------|------|------|
| 营销首页 | `/home` | 查询营销配置、试算价格、组队列表、参团 |
| 交易下单 | `/trade` | 锁单（创建订单）、结算、退款 |
| 订单管理 | `/order` | 本地订单列表、快速结算/退款 |
| 动态配置 | `/dcc` | 降级开关、限流开关、切量范围 |
| 模拟回调 | `/test` | 模拟第三方回调 + 实时日志 |

---

## 十、常见问题

| 问题 | 解决方案 |
|------|----------|
| Port 8091 already in use | 杀掉占用进程或修改 `application-dev.yml` 中的端口 |
| Unknown database | 执行 SQL 脚本初始化数据库 |
| Lombok 编译错误 | 确保使用 JDK 8，或升级 Lombok 到 1.18.30+ |
| Redis 连接失败 | 检查 Redis 是否启动、密码是否正确、防火墙是否放行 |
| MySQL 远程连接失败 | 确认 MySQL 用户有 `%` 主机权限，防火墙放行 3306 |
