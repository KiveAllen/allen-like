# Allen-Like 高并发高可用点赞系统

## 项目介绍

Allen-Like 是一个基于 Spring Boot 3 和 Java 21
开发的高性能点赞系统，点赞系统是社交平台、内容社区等场景的核心互动功能，是主流的高并发业务场景。本项目完整覆盖了点赞系统的核心技术，从基础功能开发到高并发优化，再到企业级高可用架构，为开发者提供了一个完整的点赞系统解决方案。

## 技术栈

- **核心框架**：Spring Boot 3 + MyBatis-Plus
- **开发语言**：Java 21
- **数据库**：TiDB（分布式数据库）
- **缓存**：Redis + Caffeine（多级缓存）
- **消息队列**：Apache Pulsar
- **容器化**：Docker
- **监控**：Prometheus + Grafana

## 核心特性

- 🚀 **高性能设计**
  - 多级缓存策略（Caffeine + Redis）
  - 消息队列削峰
  - HeavyKeeper 热点数据识别
  - 分布式数据库支持

- 🔄 **高可用架构**
  - 分布式系统设计
  - 服务容错机制
  - 数据一致性保证

- 📊 **可观测性**
  - Prometheus 指标监控
  - Grafana 可视化面板
  - 系统性能分析

- 🛡️ **企业级特性**
  - 完整的监控告警
  - 系统容错机制
  - 数据一致性保证

## 项目结构

```
allen-like/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/allen/thumb/
│   │   │       ├── api/        # API 接口
│   │   │       ├── service/    # 业务逻辑
│   │   │       ├── model/      # 数据模型
│   │   │       └── config/     # 配置类
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
└── sql/                        # SQL数据结构
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose
- TiDB
- Redis
- Apache Pulsar

## 性能优化

- 使用 Caffeine 本地缓存减少 Redis 访问
- 通过 HeavyKeeper 算法识别并优化热点数据
- 采用 Pulsar 消息队列进行流量削峰、批量处理
- 分布式数据库 TiDB 提供水平扩展能力
- 定时数据持久化到 TiDB 和 定时对账

## 监控指标

- 主要使用 Prometheus 和 Grafana 进行监控和可视化
- 系统吞吐量
- 响应时间
- 缓存命中率
- 消息队列积压情况
- 数据库性能指标