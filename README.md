# DeFi Aave Application

基于 Spring Boot 的 Maven 项目,提供 Web 服务能力和数据库连接支持。

## 项目信息

- **项目名称**: DeFi Aave Application
- **版本**: 1.0.0
- **Spring Boot**: 2.7.18
- **Java 版本**: 1.8

## 技术栈

### 核心框架
- Spring Boot 2.7.18
- Maven (项目构建和依赖管理)

### Web 层
- Spring Web (spring-boot-starter-web)
- 内嵌 Tomcat 服务器

### 数据访问层
- Spring Data JPA
- Hibernate ORM
- H2 数据库 (开发环境)
- MySQL 驱动 (生产环境支持)

### 开发辅助工具
- Lombok (简化实体类开发)
- Spring Boot DevTools (热部署)
- Spring Boot Actuator (健康检查)

## 项目结构

```
defi_aave/
├── src/main/java/com/defi/aave/
│   ├── Application.java           # 应用启动类
│   ├── controller/                # Web 控制器层
│   │   ├── HealthController.java
│   │   └── UserController.java
│   ├── service/                   # 业务逻辑层
│   │   └── UserService.java
│   ├── repository/                # 数据访问层
│   │   └── UserRepository.java
│   ├── entity/                    # 数据实体层
│   │   └── User.java
│   ├── dto/                       # 数据传输对象
│   │   └── ApiResponse.java
│   └── exception/                 # 异常处理
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml            # 主配置文件
│   ├── application-dev.yml        # 开发环境配置
│   ├── application-test.yml       # 测试环境配置
│   └── application-prod.yml       # 生产环境配置
└── pom.xml                        # Maven 配置文件
```

## 快速开始

### 前置要求

- Java 1.8 或更高版本
- Maven 3.x

### 构建项目

```bash
mvn clean compile
```

### 打包项目

```bash
mvn clean package
```

### 运行应用

```bash
mvn spring-boot:run
```

或者直接运行打包后的 JAR 文件:

```bash
java -jar target/defi-aave-1.0.0.jar
```

应用启动后,默认访问地址为: `http://localhost:8080/api`

## 配置说明

### 端口配置

应用默认运行在 8080 端口,上下文路径为 `/api`

### 数据库配置

#### 开发环境 (dev)
- 使用 H2 内存数据库
- 自动创建表结构
- H2 控制台: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:devdb`

#### 生产环境 (prod)
- 使用 MySQL 数据库
- 需要配置环境变量 `DB_PASSWORD`
- 连接池配置已优化

### 切换环境

修改 `application.yml` 中的 `spring.profiles.active` 配置:

```yaml
spring:
  profiles:
    active: dev  # 可选: dev, test, prod
```

或通过命令行参数:

```bash
java -jar target/defi-aave-1.0.0.jar --spring.profiles.active=prod
```

## API 端点

### 健康检查

- **GET** `/api/health` - 应用健康检查
- **GET** `/api/health/welcome` - 欢迎信息

### 用户管理

- **POST** `/api/users` - 创建用户
- **GET** `/api/users` - 获取所有用户
- **GET** `/api/users/{id}` - 根据ID获取用户
- **GET** `/api/users/active` - 获取活跃用户
- **GET** `/api/users/page` - 分页获取用户
- **GET** `/api/users/count` - 获取用户数量
- **PUT** `/api/users/{id}` - 更新用户
- **DELETE** `/api/users/{id}` - 删除用户

### API 响应格式

所有接口统一返回格式:

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

## API 使用示例

### 1. 健康检查

```bash
curl http://localhost:8080/api/health
```

响应:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "status": "UP",
    "timestamp": "2026-01-12T16:27:32",
    "application": "DeFi Aave Application",
    "version": "1.0.0"
  }
}
```

### 2. 创建用户

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "balance": 100.50
  }'
```

响应:
```json
{
  "code": 200,
  "message": "User created successfully",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "balance": 100.50,
    "isActive": true,
    "createdAt": "2026-01-12T16:28:11.307",
    "updatedAt": "2026-01-12T16:28:11.307"
  }
}
```

### 3. 获取用户

```bash
curl http://localhost:8080/api/users/1
```

### 4. 获取所有用户

```bash
curl http://localhost:8080/api/users
```

### 5. 分页获取用户

```bash
curl "http://localhost:8080/api/users/page?page=0&size=10&sortBy=id&direction=ASC"
```

### 6. 更新用户

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Updated Name",
    "balance": 200.00
  }'
```

### 7. 删除用户

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## 监控端点

Spring Boot Actuator 提供以下监控端点:

- **GET** `/actuator/health` - 健康检查
- **GET** `/actuator/info` - 应用信息
- **GET** `/actuator/metrics` - 应用指标

访问地址: `http://localhost:8080/actuator`

## 数据库表结构

### users 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键,自增 |
| username | VARCHAR(50) | 用户名,唯一 |
| email | VARCHAR(100) | 邮箱,唯一 |
| full_name | VARCHAR(100) | 全名 |
| wallet_address | VARCHAR(42) | 钱包地址,唯一 |
| balance | DECIMAL(20,8) | 余额 |
| is_active | BOOLEAN | 是否活跃 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

## 开发指南

### 添加新的实体

1. 在 `entity` 包下创建实体类
2. 在 `repository` 包下创建 Repository 接口
3. 在 `service` 包下创建 Service 类
4. 在 `controller` 包下创建 Controller 类

### 全局异常处理

所有异常由 `GlobalExceptionHandler` 统一处理,返回标准化的错误响应。

### 日志配置

日志级别在 `application.yml` 中配置:

```yaml
logging:
  level:
    root: INFO
    com.defi.aave: DEBUG
```

## 生产部署

### 环境变量

生产环境需要配置以下环境变量:

- `DB_PASSWORD`: MySQL 数据库密码

### 启动命令

```bash
java -jar defi-aave-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.password=${DB_PASSWORD}
```

### Docker 部署 (可选)

可以创建 Dockerfile 实现容器化部署。

## 验收标准

✅ **项目结构**: 符合 Maven 标准和分层架构设计  
✅ **依赖管理**: 所有必要依赖已正确配置  
✅ **应用启动**: 应用成功启动,无报错  
✅ **数据库连接**: 成功连接到数据库  
✅ **Web 服务**: Web 服务正常响应 HTTP 请求  
✅ **基础功能**: 完成基本的 CRUD 操作  
✅ **配置生效**: 配置文件中的配置正确加载并生效  

## 许可证

Copyright © 2026 DeFi Team
