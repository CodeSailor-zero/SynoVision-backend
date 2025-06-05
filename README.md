# SynoVision - 图库协作平台

SynoVision 是一个高效、现代化的图库协作平台，旨在为用户提供便捷的图片管理与共享体验。无论是个人还是团队，都可以通过本平台上传、组织、分析和分享图片资源，从而提升工作效率和协作能力。

## 项目亮点

1. **模块化设计**  
   SynoVision 采用模块化架构设计，功能划分清晰，便于扩展与维护。各模块之间低耦合，支持独立开发与部署。

2. **强大的权限控制**  
   系统内置了灵活的权限管理机制，基于 JSON 配置文件（如 [spacePermisssionConfig.json](src/main/resources/permisssionConfig/spacePermisssionConfig.json)），可轻松定制不同用户角色对空间、图片和标签的操作权限。

3. **高效的文件处理**  
   通过 [CosManager.java](src/main/java/com/sean/synovision/manager/CosManager.java) 和 [FileManager.java](src/main/java/com/sean/synovision/manager/FileManager.java)，实现了对图片文件的存储、检索和分发优化，确保系统的高性能与可靠性。

4. **分布式缓存与限流**  
   使用 Redis 进行分布式缓存（[RedisConfig.java](src/main/java/com/sean/synovision/config/RedisConfig.java)）和限流管理（[RedisLimiterManager.java](src/main/java/com/sean/synovision/manager/RedisLimiterManager.java)），显著提升了系统响应速度，并有效防止恶意请求。

5. **自动化任务调度**  
   借助 [ScheduledTasks.java](src/main/java/com/sean/synovision/job/ScheduledTasks.java)，实现了后台定时任务的支持，例如清理过期缓存或生成统计报告，保证系统长期稳定运行。

6. **RESTful API 设计**  
   平台提供了标准化的 RESTful API 接口，涵盖用户管理、图片操作、空间分析等核心功能。所有接口均经过严格的鉴权拦截（[AuthInterceptor.java](src/main/java/com/sean/synovision/aop/AuthInterceptor.java)），确保数据安全。

7. **多环境配置支持**  
   提供多套配置文件（如 [application.yaml](src/main/resources/application.yaml) 和 [application-local.yaml](src/main/resources/application-local.yaml)），方便在不同环境下快速切换，适应开发、测试和生产需求。

8. **异常处理机制**  
    完善的全局异常捕获与返回结果封装（[GlobalExceptionHander.java](src/main/java/com/sean/synovision/exception/GlobalExceptionHander.java) 和 [ResultUtils.java](src/main/java/com/sean/synovision/exception/ResultUtils.java)），让开发者能够更专注于业务逻辑实现。

---

## 技术栈

- **编程语言**: Java
- **框架**: Spring Boot, MyBatis-Plus，Sa-Token
- **缓存**: Redis，Caffeine
- **数据库**: MySQL（SQL脚本见 [create_sql.sql](src/sql/create_sql.sql)）
- **对象存储**: 腾讯云 COS（通过 [CosManager.java](src/main/java/com/sean/synovision/manager/CosManager.java) 实现）
- **任务调度**: Spring Scheduler
- **API 文档**: Swagger
- **构建工具**: Maven（[pom.xml](pom.xml)）

---

## 快速开始

### 1. 环境准备
- JDK 1.8+
- Maven 3.x
- MySQL 5.7+
- Redis

### 2. 配置文件修改
编辑 [application.yaml](src/main/resources/application.yaml)，填写正确的数据库连接信息和其他必要参数。

---

## 未来规划

- AI 生成对应图片
- 开发 AI 操作图片功能（目前只支持扩图功能）

---
