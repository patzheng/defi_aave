# AAVE代币持有者分析功能使用指南

## 功能概述

本功能实现了AAVE代币持有者信息的获取与收益分析，包括：
- 获取持有超过3000个AAVE的钱包地址
- 追溯首次购买AAVE的时间和价格
- 计算当前持仓的收益情况

## 前置条件

### 1. 申请API密钥

#### Etherscan API Key
1. 访问 [Etherscan](https://etherscan.io/)
2. 注册账号并登录
3. 进入 [API Keys](https://etherscan.io/myapikey) 页面
4. 创建新的API Key

#### CoinGecko API
CoinGecko的免费API无需密钥，但有速率限制。如需更高限额，可以申请Pro版本。

### 2. 配置API密钥

在项目中配置Etherscan API Key，有两种方式：

**方式一：环境变量（推荐）**
```bash
export ETHERSCAN_API_KEY=your_etherscan_api_key_here
```

**方式二：配置文件**
编辑 `src/main/resources/application.yml`：
```yaml
aave:
  etherscan:
    api-key: your_etherscan_api_key_here
```

## API接口说明

### 1. 同步AAVE持有者数据

**端点**: `POST /api/aave/holders/sync`

**描述**: 手动触发从Etherscan获取并分析AAVE持有者数据

**请求示例**:
```bash
curl -X POST http://localhost:8080/api/aave/holders/sync
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Sync completed successfully",
  "data": {
    "totalProcessed": 150,
    "successCount": 145,
    "failedCount": 5,
    "startTime": "2026-01-12 17:00:00",
    "endTime": "2026-01-12 17:05:30",
    "durationSeconds": 330
  }
}
```

### 2. 查询持有者列表

**端点**: `GET /api/aave/holders`

**参数**:
- `page` (可选): 页码，从0开始，默认0
- `size` (可选): 每页数量，默认20
- `sortBy` (可选): 排序字段，可选值：`holdingAmount`、`profitLossPercentage`，默认`holdingAmount`
- `order` (可选): 排序方向，可选值：`asc`、`desc`，默认`desc`
- `minHolding` (可选): 最小持仓量过滤，默认3000

**请求示例**:
```bash
# 查询第一页，按持仓量降序
curl "http://localhost:8080/api/aave/holders?page=0&size=20&sortBy=holdingAmount&order=desc"

# 查询按收益率排序
curl "http://localhost:8080/api/aave/holders?sortBy=profitLossPercentage&order=desc"

# 查询持仓超过5000的地址
curl "http://localhost:8080/api/aave/holders?minHolding=5000"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Query successful",
  "data": {
    "content": [
      {
        "walletAddress": "0x1234567890123456789012345678901234567890",
        "holdingAmount": 50000.123456789012345678,
        "firstPurchaseTime": "2020-10-15 08:30:00",
        "firstPurchasePrice": 50.25,
        "firstPurchaseAmount": 10000.0,
        "currentPrice": 150.75,
        "costBasis": 502500.00,
        "currentValue": 7537500.00,
        "profitLoss": 7035000.00,
        "profitLossPercentage": 1400.00,
        "dataSource": "Etherscan",
        "lastUpdated": "2026-01-12 17:00:00"
      }
    ],
    "totalElements": 145,
    "totalPages": 8,
    "currentPage": 0,
    "pageSize": 20,
    "first": true,
    "last": false
  }
}
```

### 3. 查询单个持有者详情

**端点**: `GET /api/aave/holders/{address}`

**参数**:
- `address`: 钱包地址（42位十六进制，以0x开头）

**请求示例**:
```bash
curl "http://localhost:8080/api/aave/holders/0x1234567890123456789012345678901234567890"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "Query successful",
  "data": {
    "walletAddress": "0x1234567890123456789012345678901234567890",
    "holdingAmount": 50000.123456789012345678,
    "firstPurchaseTime": "2020-10-15 08:30:00",
    "firstPurchasePrice": 50.25,
    "firstPurchaseAmount": 10000.0,
    "currentPrice": 150.75,
    "costBasis": 502500.00,
    "currentValue": 7537500.00,
    "profitLoss": 7035000.00,
    "profitLossPercentage": 1400.00,
    "dataSource": "Etherscan",
    "lastUpdated": "2026-01-12 17:00:00"
  }
}
```

如果地址不存在：
```json
{
  "code": 404,
  "message": "Holder not found for address: 0x...",
  "data": null
}
```

## 使用流程

### 1. 启动应用

```bash
# 设置环境变量
export ETHERSCAN_API_KEY=your_api_key_here

# 启动应用
mvn spring-boot:run
```

或使用指定环境：
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```

### 2. 首次同步数据

应用启动后，调用同步接口获取AAVE持有者数据：

```bash
curl -X POST http://localhost:8080/api/aave/holders/sync
```

**注意**: 首次同步可能需要较长时间（取决于持有者数量和API速率限制）。

### 3. 查询分析结果

同步完成后，可以通过查询接口获取分析结果：

```bash
# 查看持仓最多的前20个地址
curl "http://localhost:8080/api/aave/holders?sortBy=holdingAmount&order=desc&size=20"

# 查看收益率最高的前20个地址
curl "http://localhost:8080/api/aave/holders?sortBy=profitLossPercentage&order=desc&size=20"
```

## 数据说明

### 字段解释

| 字段 | 说明 | 单位 |
|------|------|------|
| walletAddress | 钱包地址 | - |
| holdingAmount | 当前持有AAVE数量 | AAVE |
| firstPurchaseTime | 首次购买时间 | - |
| firstPurchasePrice | 首次购买时的AAVE价格 | USD |
| firstPurchaseAmount | 首次购买数量 | AAVE |
| currentPrice | 当前AAVE价格 | USD |
| costBasis | 购买成本 | USD |
| currentValue | 当前持仓价值 | USD |
| profitLoss | 收益金额 | USD |
| profitLossPercentage | 收益率 | % |

### 计算公式

- **购买成本**: `costBasis = firstPurchasePrice × firstPurchaseAmount`
- **当前价值**: `currentValue = currentPrice × holdingAmount`
- **收益金额**: `profitLoss = currentValue - costBasis`
- **收益率**: `profitLossPercentage = (profitLoss / costBasis) × 100`

## 重要提示

### 1. API限流

- **Etherscan免费版**: 5次/秒
- **CoinGecko免费版**: 50次/分钟

程序已内置重试机制和延迟处理，但大量数据同步仍需较长时间。

### 2. 数据准确性

- **首次购买识别**: 基于链上首笔AAVE转入交易，可能无法区分购买、转账、空投等场景
- **价格数据**: 历史价格来自CoinGecko，反映市场平均价格，实际购买价格可能不同
- **收益计算**: 基于首次购买成本，不考虑后续加仓减仓情况

### 3. 数据范围

- 仅分析以太坊主网上的AAVE代币
- 不包括其他链（Polygon、Arbitrum等）上的AAVE
- 不分析stAAVE等衍生代币

### 4. 持有者获取限制

由于Etherscan API限制，`getTopHolders()`方法只能获取最近交易的部分地址。
如需获取完整持有者列表，可以考虑：
- 使用付费的区块链数据服务（如The Graph、Alchemy）
- 定期运行同步任务，逐步积累数据
- 结合其他数据源（如链上快照、第三方分析平台）

## 配置参数

所有配置项在 `application.yml` 中：

```yaml
aave:
  etherscan:
    api-key: ${ETHERSCAN_API_KEY:YOUR_API_KEY_HERE}  # Etherscan API密钥
    base-url: https://api.etherscan.io/api           # API基础URL
  token:
    contract-address: 0x7Fc66500c84A76Ad7e9c93437bFc5Ac33E2DDaE9  # AAVE代币合约地址
    min-holding: 3000                                 # 最小持仓量阈值
  coingecko:
    api-url: https://api.coingecko.com/api/v3        # CoinGecko API地址
    token-id: aave                                    # AAVE在CoinGecko的ID
  sync:
    batch-size: 100                                   # 批处理大小
  cache:
    price-ttl: 300                                    # 价格缓存时间(秒)
```

## 故障排除

### 1. 同步失败

**症状**: 同步接口返回错误或大量失败记录

**可能原因**:
- API密钥无效或未配置
- API速率限制
- 网络连接问题

**解决方法**:
- 检查API密钥配置
- 查看日志确认具体错误
- 减小`batch-size`或增加延迟

### 2. 价格数据缺失

**症状**: `firstPurchasePrice`为null

**可能原因**:
- CoinGecko没有该日期的历史数据
- API调用失败

**解决方法**:
- 这是正常现象，部分早期数据可能缺失
- 可以考虑使用其他价格数据源

### 3. 查询无结果

**症状**: 查询接口返回空列表

**可能原因**:
- 尚未执行同步操作
- 数据库无数据

**解决方法**:
- 先调用同步接口
- 检查数据库连接和表创建

## 扩展功能（未来计划）

1. **定时任务**: 定期自动同步最新数据
2. **多代币支持**: 扩展到其他ERC-20代币
3. **详细交易历史**: 记录所有买卖记录
4. **收益趋势分析**: 历史快照和趋势图表
5. **数据导出**: CSV/Excel格式导出
6. **通知功能**: 价格或持仓变化通知

## 技术架构

```
Controller层 (AaveHolderController)
    ↓
Service层 (AaveHolderService)
    ↓
Repository层 (AaveHolderRepository)
    ↓
Database (MySQL/H2)

External APIs:
- EtherscanApiClient → Etherscan API
- CoinGeckoApiClient → CoinGecko API
```

## 数据库表结构

表名: `aave_holders`

```sql
CREATE TABLE aave_holders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wallet_address VARCHAR(42) NOT NULL UNIQUE,
    holding_amount DECIMAL(30,18) NOT NULL,
    first_purchase_time DATETIME,
    first_purchase_price DECIMAL(20,8),
    first_purchase_amount DECIMAL(30,18),
    current_price DECIMAL(20,8) NOT NULL,
    cost_basis DECIMAL(30,8),
    current_value DECIMAL(30,8) NOT NULL,
    profit_loss DECIMAL(30,8),
    profit_loss_percentage DECIMAL(10,2),
    data_source VARCHAR(50) NOT NULL DEFAULT 'Etherscan',
    last_updated DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_holding_amount (holding_amount),
    INDEX idx_profit_loss_percentage (profit_loss_percentage)
);
```

## 联系方式

如有问题或建议，请通过以下方式联系：
- GitHub Issues
- 项目维护者邮箱

---

**最后更新**: 2026-01-12
