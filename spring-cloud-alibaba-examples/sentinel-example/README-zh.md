# Spring Cloud Alibaba Sentinel Example

## 项目说明

本 Example 项目演示如何使用 `spring-cloud-starter-alibaba-sentinel` 完成 Spring Cloud 应用中的流量治理功能。

[Sentinel](https://github.com/alibaba/Sentinel) 是阿里巴巴开源的分布式系统的流量防卫组件，Sentinel 以流量作为切入点，从流量控制，熔断降级，系统负载保护等多个维度保护服务的稳定性。

## Sentinel Example 

在本 Example 项目中，主要演示 Sentinel 断路器，整合 Spring Cloud Gateway 和 OpenFeign、RestTemplate 以及 Webclient 的使用。

### 下载并启动 Sentinel Console

1. 首先需要获取 Sentinel 控制台，Sentinel Console 支持直接下载和源码构建两种方式

   1. 直接下载：[下载 Sentinel 控制台](https://github.com/alibaba/Sentinel/releases)
   2. 源码构建：进入 Sentinel [Github 项目页面](https://github.com/alibaba/Sentinel)，将代码 clone 到本地自行编译打包，[参考此文档](https://github.com/alibaba/Sentinel/blob/1.8/sentinel-dashboard/README.md)。
      

2. 启动控制台，执行 Java 命令 `java -jar sentinel-dashboard.jar` 完成 Sentinel 控制台的启动。

   控制台默认的监听端口为 `8080`。Sentinel 控制台使用 Spring Boot 编程模型开发，如果需要指定其他端口，请使用 Spring Boot 容器配置的标准方式，详情请参考 [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-customizing-embedded-containers)。

### Sentinel Core Example

在此 Example 模块中，主要演示如何使用 Sentinel 的基本功能完成 Spring Cloud 应用的流量管控。 在启动 Example 进行演示之前，先了解一下如何在 Spring Cloud 应用中接入 Sentinel 组件。

#### 项目编写

> **注意：本文档只是为了便于理解接入方式。本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 `pom.xml` 文件，引入 Sentinel starter。

   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
   </dependency>
   ```

2. 接入限流埋点

    - HTTP 埋点
      `spring-cloud-starter-alibaba-sentinel` 默认为所有的 HTTP 服务提供了限流埋点，如果只想对 HTTP 服务进行限流，那么只需要引入依赖，无需修改代码。

    - 自定义埋点
      如果需要对某个特定的方法进行限流或降级，可以通过 `@SentinelResource` 注解来完成限流的埋点，示例代码如下：

      ```java
      @SentinelResource("resource")
      public String hello() {
          return "Hello";
      }
      ```

    当然也可以通过原始的 `SphU.entry(xxx)` 方法进行埋点，可以参见 [Sentinel 文档](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8#%E5%AE%9A%E4%B9%89%E8%B5%84%E6%BA%90)。

3. 配置限流规则

   Sentinel 提供了两种配置限流规则的方式：代码配置 和 控制台配置。本示例使用的方式为通过代码配置。

    1. 通过代码来实现限流规则的配置。一个简单的限流规则配置示例代码如下，更多限流规则配置详情请参考 [Sentinel 文档](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8#%E5%AE%9A%E4%B9%89%E8%A7%84%E5%88%99)。

       ```java
       List<FlowRule> rules = new ArrayList<FlowRule>();
       FlowRule rule = new FlowRule();
       rule.setResource(str);
       // set limit qps to 10
       rule.setCount(10);
       rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
       rule.setLimitApp("default");
       rules.add(rule);
       FlowRuleManager.loadRules(rules);
       ```

    2. 通过控制台进行限流规则配置请参考文章后面的图文说明。

#### 应用启动

1. 增加配置，在应用的 `/src/main/resources/application.yml` 中添加基本配置信息

    ```yaml
    server:
      port: 18083
      
    spring:
      application:
        name: sentinel-core-example
    
      cloud:
        sentinel:
          transport:
            dashboard: localhost:8080
    ```

2. 启动应用，支持 IDE 直接启动和编译打包后启动。

    1. IDE直接启动：找到主类 `SentinelCoreApplication`，执行 main 方法启动应用。
    2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar sentinel-core-example.jar` 启动应用。

#### 调用服务验证

使用 curl 命令分别调用两个 URL，可以看到访问成功。

```shell
$ curl http://localhost:18083/test
Blocked by Sentinel (flow limiting)

$ curl http://localhost:18083/hello
Hello
```

#### 配置限流规则并验证

1. 访问 http://localhost:8080 页面，进行登陆，默认用户名和密码均为：`sentinel`。

   可以在左侧看到 `sentinel-core-example` 应用已经注册到了控制台，单击 **流控规则** ，可以看到目前的流控规则为空。

   > **注意：如果您在控制台没有找到应用，请调用一下进行了 Sentinel 埋点的 URL 或方法，因为 Sentinel 使用了 lazy load 策略。详细的排查过程请参见 [Sentinel FAQ](https://github.com/alibaba/Sentinel/wiki/FAQ)。**

<img src="./images/image-20240428171413303.png" alt="image-20240428171413303" style="zoom:50%;" />

2. 配置 URL 限流规则：点击新增流控规则，资源名填写需要限流的 URL 相对路径，单机阈值选择需要限流的阈值，点击新增进行确认。(为了便于演示效果，这里将值设置成了 1)。

<img src="./images/image-20240428171245517.png" alt="image-20240428171245517" style="zoom: 50%;" />

3. 配置自定义限流规则：点击新增流控规则，资源名填写 `@SentinelResource` 注解 `value` 字段的值，单机阈值选择需要限流的阈值，点击新增进行确认。(为了便于演示效果，这里将值设置成了 1)。

<img src="./images/image-20240428171608519.png" alt="image-20240428171608519" style="zoom:50%;" />

4. 访问 URL，当 QPS 超过 1 时,可以看到限流效果如下。

   ![image-20240428171907705](./images/image-20240428171907705.png)

#### 自定义限流处理逻辑

* 默认限流异常处理

  URL 限流触发后默认处理逻辑是，直接返回 "Blocked by Sentinel (flow limiting)"。 如果需要自定义处理逻辑，实现的方式如下：

  ```java
  public class CustomUrlBlockHandler implements UrlBlockHandler {
      @Override
      public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
          // todo add your logic
      }
  }
  
  WebCallbackManager.setUrlBlockHandler(new CustomUrlBlockHandler());
  ```

* 使用 `@SentinelResource` 注解下的限流异常处理

  如果需要自定义处理逻辑，填写 `@SentinelResource` 注解的 `blockHandler` 属性（针对所有类型的 `BlockException`，需自行判断）或 `fallback` 属性（针对熔断降级异常），注意**对应方法的签名和位置有限制**，详情见 [Sentinel 注解支持文档](https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81#sentinelresource-%E6%B3%A8%E8%A7%A3)。示例实现如下：

  ```java
  public class TestService {
  
      // blockHandler 是位于 ExceptionUtil 类下的 handleException 静态方法，需符合对应的类型限制.
      @SentinelResource(value = "test", blockHandler = "handleException", blockHandlerClass = {ExceptionUtil.class})
      public void test() {
          System.out.println("Test");
      }
  
      // blockHandler 是位于当前类下的 exceptionHandler 方法，需符合对应的类型限制.
      @SentinelResource(value = "hello", blockHandler = "exceptionHandler")
      public String hello(long s) {
          return String.format("Hello at %d", s);
      }
  
      public String exceptionHandler(long s, BlockException ex) {
          // Do some log here.
          ex.printStackTrace();
          return "Oops, error occurred at " + s;
      }
  }
  
  public final class ExceptionUtil {
  
      public static void handleException(BlockException ex) {
          System.out.println("Oops: " + ex.getClass().getCanonicalName());
      }
  }
  ```

一个简单的 `@SentinelResource` 示例可以见 [sentinel-demo-annotation-spring-aop](https://github.com/alibaba/Sentinel/tree/2021.x/sentinel-demo/sentinel-demo-annotation-spring-aop)。

### Sentinel Circuitbreaker Example

本 Example 主要演示 OpenFeign 整合 Sentinel 断路器的使用。

#### 准备配置文件

1. 添加配置到配置中心。dataId 为 `sentinel-circuitbreaker-rules.yml`

   ```yml
   feign:
     circuitbreaker:
       enabled: true # 开启 feign 断路器支持
     sentinel:
       default-rule: default # 默认规则名称
       rules:
         # 默认规则, 对所有 feign client 生效
         default:
           - grade: 2 # 根据异常数目降级
             count: 1
             timeWindow: 15 # 降级后到半开状态的时间
             statIntervalMs: 1000
             minRequestAmount: 1
         # 只对 feign client user 生效
         user:
           - grade: 2
             count: 1
             timeWindow: 15
             statIntervalMs: 1000
             minRequestAmount: 1
         # 只对 feign client user 的方法 feignMethod 生效
         # 括号里是参数类型, 多个逗号分割, 比如 user#method(boolean,String,Map)
         "[user#feignMethod(boolean)]":
           - grade: 2
             count: 1
             timeWindow: 10
             statIntervalMs: 1000
             minRequestAmount: 1
   ```

#### 验证配置生效

启动项目主类 `FeignCircuitBreakerApplication`

##### 验证默认 Feign client 生效

先访问 http://localhost/test/default/false 2 次 （1秒内）  
再访问 http://localhost/test/default/true 断路器处于打开状态

##### 验证指定 Feign client 生效

先访问 http://localhost/test/feign/false 2 次 （1秒内）  
再访问 http://localhost/test/feign/true 断路器处于打开状态

##### 验证 Feign client 指定方法生效

先访问 http://localhost/test/feignMethod/false 2次 （1秒内）  
再访问 http://localhost/test/feignMethod/true 断路器处于打开状态

#### 规则动态刷新

修改配置中心的规则, 再访问上述接口。

### Sentinel OpenFeign Example

本 Example  演示 OpenFeing 与 Sentinel 的整合。Example 中使用 httpbin 充当后台 API 接口服务。

#### 项目编写

> **注意：本项目中代码已经完成相对应的功能，不需要再进行任何修改。**

项目支持两种启动方式：通过主类 `OpenFeignApplication` 启动和 Jar 包启动两种方式。

#### 调用测试

项目启动完成之后，可以通过访问对应的 URL 访问，查看对应的 Sentinel 流控效果。

> **注意：项目中提供的 RestTemplate 和 Webclient Example 同理。**

## Endpoint 信息查看

Spring Boot 应用支持通过 Endpoint 来暴露相关信息，`spring-cloud-starter-alibaba-sentinel` 也支持这一点。

在使用之前需要在 Maven 中添加 `spring-boot-starter-actuator`依赖，并在配置中允许 Endpoints 的访问。
* Spring Boot 1.x 中添加配置 `management.security.enabled=false`
* Spring Boot 2.x 中添加配置 `management.endpoints.web.exposure.include=*`

Spring Boot 1.x 可以通过访问 http://127.0.0.1:18083/sentinel 来查看 Sentinel Endpoint 的信息。Spring Boot 2.x 可以通过访问 http://127.0.0.1:18083/actuator/sentinel 来访问。

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532084199224-1a41591d-7a06-4680-be8a-5de319ac635d.png" width="480" heigh='360' ></p>

## 查看实时监控
Sentinel 控制台支持实时监控查看，您可以通过 Sentinel 控制台查看各链路的请求的通过数和被限流数等信息。
其中 `p_qps` 为通过(pass) 流控的 QPS，`b_qps` 为被限流 (block) 的 QPS。

<p align="center"><img src="https://cdn.nlark.com/lark/0/2018/png/54319/1532313595369-8428cd7d-9eb7-4786-a149-acf0da4a2daf.png" width="480" heigh='180' ></p>

## ReadableDataSource 支持

Sentinel 内部提供了[动态规则的扩展实现 ReadableDataSource](https://github.com/alibaba/Sentinel/wiki/%E5%8A%A8%E6%80%81%E8%A7%84%E5%88%99%E6%89%A9%E5%B1%95#datasource-%E6%89%A9%E5%B1%95)。

Sentinel starter 整合了目前存在的几类 ReadableDataSource。只需要在配置文件中进行相关配置，即可在 Spring 容器中自动注册 DataSource。

比如要定义两个ReadableDataSource，分别是 `FileRefreshableDataSource` 和 `NacosDataSource`，配置如下：

```properties
spring.cloud.sentinel.datasource.ds1.file.file=classpath: degraderule.json
spring.cloud.sentinel.datasource.ds1.file.data-type=json

spring.cloud.sentinel.datasource.ds2.nacos.server-addr=127.0.0.1:8848
spring.cloud.sentinel.datasource.ds2.nacos.dataId=sentinel
spring.cloud.sentinel.datasource.ds2.nacos.groupId=DEFAULT_GROUP
spring.cloud.sentinel.datasource.ds2.nacos.data-type=json
```

`ds1` 和 `ds2` 表示ReadableDataSource的名称，可随意编写。`ds1` 和 `ds2` 后面的 `file` 和 `nacos` 表示ReadableDataSource的类型。

目前支持`file`, `nacos`, `zk`, `apollo`，`redis` 这5种类型。

其中`nacos`，`zk`，`apollo`，`redis` 这4种类型的使用需要加上对应的依赖`sentinel-datasource-nacos`, `sentinel-datasource-zookeeper`, `sentinel-datasource-apollo`, `sentinel-datasource-redis`。

当 `ReadableDataSource` 加载规则数据成功的时候，控制台会打印出相应的日志信息：

```
[Sentinel Starter] DataSource ds1-sentinel-file-datasource load 3 DegradeRule
[Sentinel Starter] DataSource ds2-sentinel-nacos-datasource load 2 FlowRule
```

## More
Sentinel 是一款功能强大的中间件，从流量控制，熔断降级，系统负载保护等多个维度保护服务的稳定性。此 Demo 仅演示了 使用 Sentinel 作为限流工具的使用，更多 Sentinel 相关的信息，请参考 [Sentinel 项目](https://github.com/alibaba/Sentinel)。

如果您对 `spring-cloud-starter-alibaba-sentinel` 有任何建议或想法，欢迎在 issue 中或者通过其他社区渠道向我们提出。
