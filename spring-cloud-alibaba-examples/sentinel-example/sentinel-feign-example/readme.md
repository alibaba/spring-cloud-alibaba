# Sentinel Feign Example

Project description

This project demonstrates how to use Sentinel starter to complete the Spring Cloud application call.

[Sentinel](https://github.com/alibaba/Sentinel) is alibaba open source distributed system flow defense components, Sentinel flow as the breakthrough point, from the flow control, fusing the drop, the stability of the system load multiple dimensions, such as protection services.

[OpenFeign](https://github.com/spring-cloud/spring-cloud-openfeign) is a declarative, templated HTTP client, Feign can help us faster and gracefully HTTP API calls.

By focusing on this project, the integration of Sentinel and Feign more characteristics about Sentinel can view [Sentinel - core - example](https://github.com/alibaba/spring-cloud-alibaba/tree/master/spring-cloud-alibaba-examples/sentinel-example/sentinel-core-example).

## sample

Service consumer
Before launching the example, let's see how Feign can access Sentinel.
** note that this section is for your convenience only. The access has been completed in this sample code and you do not need to modify it. * *

First, modify the pom.xml file to introduce Sentinel starter and Dubbo starter.

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
2. Secondly, nacos registries are used

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

3. Define the FeignClient and its degraded configuration

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
- define a fallback factory to get an exception

```java
@Component
public class EchoServiceFallbackFactory implements FallbackFactory<EchoServiceFallback> {
    @Override
    public EchoServiceFallback create(Throwable throwable) {
        return new EchoServiceFallback(throwable);
    }
}
```

- define a specific fallback implementation
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
Service provider

1. First, rely on the nacos registry

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```


2. Define the service provider interface

```java
@RestController
public class EchoController {

    @GetMapping("/echo/{str}")
    public String echo(@PathVariable String str) {
        return "provider-" + str;
    }

}
```

Application launch


Support for IDE startup directly and after compilation and packaging.

- launch the nacos registry

- starting service provider:

1. IDE starts directly: find the main class `ProviderApplication` and execute the main method to start the application.
2. Start after packaging and compilation: first execute `mvn clean package` to compile and package the project, and then execute `java-jar sentinel-feign-provider-example.jar` to start the application.

- starting service consumer:

1. IDE launch directly: find the main class `ConsumerApplication` and execute the main method to launch the application.
2. Start after packaging and compilation: first execute `mvn clean package` to compile and package the project, and then execute `java-jar sentinel-feign-consumer-example.jar` to start the application.