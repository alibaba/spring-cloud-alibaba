# Sentinel RestClient 集成（Spring Cloud Alibaba）

> 为 Spring Framework **RestClient** 提供 Sentinel 拦截器集成。
>  **JDK 17+ · Spring Boot 3.5+ · Spring Framework 6.1+ · Spring Cloud Alibaba 2025.x**

## 特性

- 为 **Spring 管理的** `RestClient.Builder` 自动注册 `SentinelRestClientInterceptor`。
- 通过全局开关 **`spring.cloud.sentinel.enabled`** 统一控制是否装配（默认开启）。
- 使用 **BeanPostProcessor** 将拦截器**插入到索引 0（优先级更高）**，并做去重。
- 支持 **非 Spring 管理**的 `RestClient` 手动注册拦截器。

------

## 快速开始

在你的应用里引入 SCA BOM 与 Sentinel Starter（版本号按你的工程为准）：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-alibaba-dependencies</artifactId>
      <version>2025.0.x</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
  </dependency>
</dependencies>
```

无需额外代码，框架会在 `RestClient.Builder` 上自动注入拦截器。

------

## 使用说明

### 1) Spring 管理的 RestClient（默认）

当 `RestClient.Builder` 由 Spring 容器创建/管理时，本模块会自动注册拦截器并**放到列表最前**：

```java
// 由自动配置与 BPP 完成，无需手写：
// builder.requestInterceptors(list -> list.add(0, new SentinelRestClientInterceptor()));
```

> 说明：使用 `list.add(0, interceptor)` 等价 “addFirst”，优先级更高。
>  若用户在自定义扩展机制里改变了顺序，以最终应用的拦截器链为准。

### 2) 非 Spring 管理的 RestClient（手动示例）

如果没有通过 Spring 创建 `RestClient`，请**手动注册**拦截器：

```java
import org.springframework.web.client.RestClient;
import com.alibaba.cloud.sentinel.restclient.SentinelRestClientInterceptor;

RestClient client = RestClient.builder()
        .requestInterceptor(new SentinelRestClientInterceptor())
        .build();
```

------

## 配置

### 开关（统一入口）

是否启用由**全局**开关控制（**不要**在拦截器中再做 `enabled` 判断）：

```yaml
spring:
  cloud:
    sentinel:
      enabled: true   # 默认即为 true；设为 false 时，本模块相关 Bean 不装配
```

### RestClient 专属配置

`SentinelRestClientProperties` 仅作为扩展占位，**不包含 `enabled` 字段**（按建议去除）。后续如有资源名策略等真实配置项，将以
 `spring.cloud.sentinel.restclient.*` 前缀承载。

------

## 工作机制（简述）

- **自动装配类**：`SentinelRestClientAutoConfiguration`

  - 受 `spring.cloud.sentinel.enabled` 控制（`@ConditionalOnProperty`，`matchIfMissing=true`）。
  - 声明两个 Bean：
    - `SentinelRestClientInterceptor`（无参构造；不做 `System.out.println` 调试输出），
    - `SentinelRestClientBeanPostProcessor`（把拦截器插入到索引 0，并去重）。

- **注册方式（Boot 3）**：
   `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

  ```
  com.alibaba.cloud.sentinel.restclient.SentinelRestClientAutoConfiguration
  ```

------

