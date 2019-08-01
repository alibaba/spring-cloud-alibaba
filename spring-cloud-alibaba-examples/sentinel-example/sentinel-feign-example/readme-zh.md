# Sentinel Feign Example

## 项目说明

本项目演示如何使用 Sentinel starter 完成 Spring Cloud 应用调用。

[Sentinel](https://github.com/alibaba/Sentinel) 是阿里巴巴开源的分布式系统的流量防卫组件，Sentinel 把流量作为切入点，从流量控制，熔断降级，系统负载保护等多个维度保护服务的稳定性。

[OpenFeign](https://github.com/spring-cloud/spring-cloud-openfeign)是一款声明式、模板化的HTTP客户端， Feign可以帮助我们更快捷、优雅地调用HTTP API。

本项目专注于Sentinel与Feign的整合，关于Sentinel的更多特性可以查看[sentinel-core-example](https://github.com/alibaba/spring-cloud-alibaba/tree/master/spring-cloud-alibaba-examples/sentinel-example/sentinel-core-example)。

## 示例

### 服务消费方
在启动示例进行演示之前，我们先了解一下 Feign 如何接入 Sentinel。
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 pom.xml 文件，引入 Sentinel starter 和 Dubbo starter。

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>

```
2. 其次, 使用nacos 注册中心
	
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

3. 定义FeignClient,及其降级配置

- 定义FeignClient
```java
@FeignClient(name = "service-provider", fallbackFactory = EchoServiceFallbackFactory.class)
public interface EchoService {

    /**
     * 调用服务提供方的输出接口
     *
     * @param str 用户输入
     * @return
     */
    @GetMapping(value = "/echo/{str}")
    String echo(@PathVariable("str") String str);
}
```
- 定义fallback 工厂，获取异常

```java
@Component
public class EchoServiceFallbackFactory implements FallbackFactory<EchoServiceFallback> {
    @Override
    public EchoServiceFallback create(Throwable throwable) {
        return new EchoServiceFallback(throwable);
    }
}
```

- 定义具体的fallback 实现
```java
public class EchoServiceFallback implements EchoService {
    private Throwable throwable;

    EchoServiceFallback(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public String echo(String str) {
        return "consumer-fallback-default-str" + throwable.getMessage();
    }
}
```
### 服务提供方

1. 首先， 依赖nacos 注册中心

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

2. 定义服务提供方接口

```java
@RestController
public class EchoController {

    @GetMapping("/echo/{str}")
    public String echo(@PathVariable String str) {
        return "provider-" + str;
    }

}
```
### 应用启动 


支持 IDE 直接启动和编译打包后启动。

- 启动nacos 注册中心

- 启动服务提供方：

1. IDE直接启动：找到主类 `ProviderApplication`，执行 main 方法启动应用。
2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar sentinel-feign-provider-example.jar`启动应用。

- 启动服务消费方：

1. IDE直接启动：找到主类 `ConsumerApplication`，执行 main 方法启动应用。
2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar sentinel-feign-consumer-example.jar`启动应用。
