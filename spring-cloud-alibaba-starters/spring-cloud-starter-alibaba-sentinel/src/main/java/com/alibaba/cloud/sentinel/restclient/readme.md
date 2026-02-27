# Sentinel RestClient 集成（Spring Cloud Alibaba）

> 为 Spring Framework **RestClient** 提供 Sentinel 拦截器集成。
> **JDK 17+ · Spring Boot 3.5+ · Spring Framework 6.1+ · Spring Cloud Alibaba 2025.x**

## 特性

- 为 **Spring 管理的** `RestClient.Builder` 自动注册 `SentinelRestClientInterceptor`。
- 通过全局开关 **`spring.cloud.sentinel.enabled`** 统一控制是否装配（默认开启）。
- 使用 **BeanPostProcessor** 将拦截器**插入到索引 0（优先级更高）**，并做去重。
- **双级资源**：为每个请求创建 host 级资源和 path 级资源，与 RestTemplate 保持一致。
- 支持自定义 **blockHandler**、**fallback** 和 **urlCleaner**。
- 支持 **非 Spring 管理** 的 `RestClient` 手动注册拦截器。

---

## 快速开始

在你的应用里引入 SCA BOM 与 Sentinel Starter（版本号按你的工程为准）：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-alibaba-dependencies</artifactId>
      <version>2025.1.x</version>
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

---

## 资源命名

每个请求会创建两级 Sentinel 资源：

| 级别 | 格式 | 示例 |
|------|------|------|
| host 级 | `METHOD:scheme://host[:port]` | `GET:https://httpbin.org` |
| path 级 | `METHOD:scheme://host[:port]/path` | `GET:https://httpbin.org/get` |

> **注意**：资源名不包含 query string，避免资源爆炸。

---

## 自定义 blockHandler / fallback / urlCleaner

通过注册特定名称的 Spring Bean 来实现自定义行为：

### blockHandler（限流处理）

当请求被流控拦截时调用：

```java
@Bean("sentinelRestClientBlockHandler")
public BiFunction<HttpRequest, BlockException, ClientHttpResponse> blockHandler() {
    return (request, ex) -> {
        return new SentinelRestClientHttpResponse("Custom block response");
    };
}
```

### fallback（降级处理）

当请求因熔断被拦截时调用：

```java
@Bean("sentinelRestClientFallback")
public BiFunction<HttpRequest, BlockException, ClientHttpResponse> fallback() {
    return (request, ex) -> {
        return new SentinelRestClientHttpResponse("Service degraded");
    };
}
```

### urlCleaner（URL 清洗）

用于归一化 RESTful 风格的 URL（如 `/users/123` → `/users/{id}`）：

```java
@Bean("sentinelRestClientUrlCleaner")
public Function<String, String> urlCleaner() {
    return url -> url.replaceAll("/users/\\d+", "/users/{id}");
}
```

> Bean 名称常量定义在 `SentinelRestClientAutoConfiguration` 中：
> `URL_CLEANER_BEAN_NAME`、`BLOCK_HANDLER_BEAN_NAME`、`FALLBACK_BEAN_NAME`。

---

## 非 Spring 管理的 RestClient（手动示例）

如果没有通过 Spring 创建 `RestClient`，请**手动注册**拦截器：

```java
import org.springframework.web.client.RestClient;
import com.alibaba.cloud.sentinel.restclient.SentinelRestClientInterceptor;

RestClient client = RestClient.builder()
        .requestInterceptor(new SentinelRestClientInterceptor())
        .build();
```

---

## 配置

### 开关（统一入口）

是否启用由**全局**开关控制：

```yaml
spring:
  cloud:
    sentinel:
      enabled: true   # 默认即为 true；设为 false 时，本模块相关 Bean 不装配
```

---

## 工作机制

- **自动装配类**：`SentinelRestClientAutoConfiguration`
  - 受 `spring.cloud.sentinel.enabled` 控制（`@ConditionalOnProperty`，`matchIfMissing=true`）。
  - 声明两个 Bean：
    - `SentinelRestClientInterceptor`（支持可选的 urlCleaner / blockHandler / fallback），
    - `SentinelRestClientBeanPostProcessor`（把拦截器插入到索引 0，并去重）。

- **注册方式（Boot 3）**：
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

  ```
  com.alibaba.cloud.sentinel.restclient.SentinelRestClientAutoConfiguration
  ```
