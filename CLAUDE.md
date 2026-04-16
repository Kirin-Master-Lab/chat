# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供在本代码库中工作的指导说明。

## 项目概述
这是一个基于 Java 17 开发的 Spring Boot 3.5.13 + LangChain4j AI 聊天应用。应用提供同步和流式两种聊天能力，由阿里云百炼（通义千问 LLM 模型）提供支持，使用 MySQL 做持久化存储，Redis 管理会话状态。

## 常用命令
```bash
# 编译并运行测试
mvn clean compile test

# 打包为可执行 JAR（跳过测试）
mvn clean package -DskipTests

# 本地运行应用（启动在 8082 端口）
mvn spring-boot:run

# 运行指定测试类
mvn test -Dtest=TestClassName

# 运行指定测试方法
mvn test -Dtest=TestClassName#testMethodName
```

## 系统架构
```
┌─────────────────────────────────────────────────────────────────────┐
│                     聊天应用（端口 8082）                            │
├─────────────────────────────────────────────────────────────────────┤
│  控制器层：REST API 接口                                             │
│  ├─ AssistantController (/defaultAssistant, /streamingAssistant)          │
│  └─ ChatModelController（底层 LLM 访问接口）                         │
│                                                                     │
│  AI 服务层：核心业务逻辑                                             │
│  ├─ Assistant（同步聊天编排）                                        │
│  ├─ StreamingAssistant（异步流式聊天）                               │
│  ├─ PromptService（动态系统提示词管理）                              │
│  ├─ IntentResolver（用户意图识别）                                   │
│  └─ StateStore（基于 Redis 的会话状态管理）                          │
│                                                                     │
│  工具层：AI 可调用的工具函数                                         │
│  ├─ AssistantTools（通用工具）                                       │
│  └─ DictionaryTools（词典管理操作）                                 │
│                                                                     │
│  持久化层：数据存储                                                 │
│  ├─ MySQL（通过 MyBatis-Plus ORM 框架访问）                          │
│  │  └─ ChatMemoryEntity（聊天历史持久化）                            │
│  └─ Redis（会话状态、LLM 缓存）                                      │
│                                                                     │
│  集成层：外部服务对接                                               │
│  ├─ LangChain4j（AI/LLM 开发框架）                                  │
│  └─ 阿里云百炼（通义千问 Qwen3-Max 大模型）                          │
└─────────────────────────────────────────────────────────────────────┘
```

## 关键代码区域
- **控制器层**：`src/main/java/com/chenliang/chat/controller/` - REST API 接口定义
- **AI 服务编排**：`src/main/java/com/chenliang/chat/aiservice/` - 核心聊天业务逻辑
- **配置类**：`src/main/java/com/chenliang/chat/config/` - Spring 配置 Bean
- **数据库 Mapper**：`src/main/java/com/chenliang/chat/mapper/` - MyBatis-Plus 数据访问接口
- **AI 工具**：`src/main/java/com/chenliang/chat/tools/` - LLM 可调用的工具函数
- **主配置文件**：`src/main/resources/application.properties` - 环境和服务配置

## 重要配置
- 服务运行端口：8082
- 数据库：MySQL 地址 `127.0.0.1:3307`（用户名：chenliang）
- Redis：地址 `localhost:6379`（使用 9 号数据库）
- LLM 模型：通义千问 Qwen3-Max（阿里云百炼）
- 聊天历史自动持久化到 MySQL
- 会话状态存储在 Redis 中

## API 接口
- `GET /defaultAssistant` - 同步非流式聊天接口
- `GET /streamingAssistant` - 服务端事件（SSE）流式聊天接口
