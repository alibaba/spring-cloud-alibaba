# Spring Cloud Alibaba Sentinel Example

## Project description

This Example project demonstrates how to use `spring-cloud-starter-alibaba-sentinel` to complete the traffic management function in Spring Cloud applications.

[Sentinel](https://github.com/alibaba/Sentinel) It is the traffic defense component of Alibaba's open source distributed system. Sentinel takes traffic as the entry point to protect the stability of services from multiple dimensions such as traffic control, fuse degradation, system load protection, etc.

## Sentinel Example

In this Example project, the Sentinel circuit breaker is mainly demonstrated, and the Spring Cloud Gateway is integrated with the use of OpenFeign, RestTemplate, and Webclient.

### Download and launch Sentinel Console

1. First, you need to obtain the Sentinel Console, which supports direct download and source code construction

   1. Direct download: [Download the Sentinel console](https://github.com/alibaba/Sentinel/releases)
   2. Source code construction: Enter Sentinel [Github project page](https://github.com/alibaba/Sentinel), clone the code to the local compilation and packaging [参考此文档](https://github.com/alibaba/Sentinel/blob/1.8/sentinel-dashboard/README.md).
      

2. Start the console and execute the Java command `java -jar sentinel-dashboard.jar` to finish starting the Sentinel console.

   The default console listening port is `8080`. The Sentinel console is developed using the Spring Boot programming model. If you need to specify other ports, please use the standard method of Spring Boot container configuration. For details, please refer to [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-customizing-embedded-containers).

### Sentinel Core Example

This Example module mainly demonstrates how to use the basic functions of Sentinel to complete the traffic control of Spring Cloud applications. Before launching Example for a demonstration, let's look at how to access the Sentinel component in a Spring Cloud application.

#### Project preparation

> **Note: This document is only for the purpose of understanding the access method. The access work is done in this sample code, and you do not need to modify it.**

1. First, modify `pom.xml` the file to introduce Sentinel starter.


   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
   </dependency>
   ```

2. Access current-limiting buried point

    -  `spring-cloud-starter-alibaba-sentinel` By default, all HTTP services provide the current limiting embedded point. If you only want to limit the current of the HTTP service, you only need to introduce the dependency and do not need to modify the code.

    - Custom Buried Point If you need to limit or degrade a specific method, you can use `@SentinelResource` annotations to complete the current limiting buried point. The example code is as follows:


      ```java
      @SentinelResource("resource")
      public String hello() {
          return "Hello";
      }
      ```

    Of course, it can also be buried by the original `SphU.entry(xxx)` method, which can be seen [Sentinel documentation](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8#%E5%AE%9A%E4%B9%89%E8%B5%84%E6%BA%90).

3. Configure the current limit rule

   Sentinel provides two ways to configure throttling rules: code configuration and console configuration. The method used in this example is configuration through code.

    1. Configure rate limits by code. Here is a simple code example of rate limit configuration. For more configuration details, see [Sentinel documentation](https://github.com/alibaba/Sentinel/wiki/%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8#%E5%AE%9A%E4%B9%89%E8%A7%84%E5%88%99).

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
    2. Please refer to the graphic description at the end of the article for current limiting rule configuration through the console.

#### The application starts

1. Add configuration, and add basic configuration information in the application `/src/main/resources/application.yml`

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

2. Start the application, support IDE direct start and start after compilation and packaging.

    1. IDE direct startup: find the main class `SentinelCoreApplication` and execute the main method to start the application.
    2. Start after packaging and compiling: First execute `mvn clean package` to package the project compilation, and then execute `java -jar sentinel-core-example.jar` to start the application.

#### Invoke service validation

Use the curl command to call the two URLs separately, and you can see that the access is successful.

```shell
$ curl http://localhost:18083/test
Blocked by Sentinel (flow limiting)

$ curl http://localhost:18083/hello
Hello
```

#### Configure and verify the current limit rule

1. Visit the http://localhost:8080  page and log in. The default user name and password are: `sentinel`.

   On the left, you can see that the Sentinel-Example application has been registered to the console. Click **Flow control rules** it, and you can see that the current flow control rule is empty.

   > ** Note: If you do not find the application in the console, please call the URL or method with Sentinel buried points, because Sentinel uses lazy load policy. For detailed troubleshooting procedures, see [Sentinel FAQ](https://github.com/alibaba/Sentinel/wiki/FAQ). **

<p align="center"><img src="https://cdn.nlark.com/lark/0/2018/png/54319/1532315951819-9ffd959e-0547-4f61-8f06-91374cfe7f21.png" width="700" heigh='400' ></p>

2. Configure URL flow limit rule: click Add Flow Control Rule, fill in the URL relative path to be restricted for the resource name, select the threshold to be restricted for the single machine threshold, and click Add to confirm. (The value is set to 1 for demonstration purposes.).

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532078717483-62ab74cd-e5da-4241-a45d-66166b1bde99.png" width="480" heigh='180' ></p>

3. Configure custom current limiting rules: click Add a flow control rule, fill in `@SentinelResource` the value of the comment `value` field for the resource name, select the threshold for current limiting for the single machine threshold, and click Add to confirm. (The value is set to 1 for demonstration purposes.).

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532080384317-2943ce0a-daaf-495d-8afc-79a0248a119a.png" width="480" heigh='180' ></p>

4. Visit the URL. When the QPS exceeds 1, you can see the effect of limiting the current as follows.

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532080652178-be119c4a-2a08-4f67-be70-fe5ed9a248a3.png" width="480" heigh='180' ></p>

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532080661437-b84ee161-6c2d-4df2-bdb7-7cf0d5be92fb.png" width="480" heigh='180' ></p>

#### Custom current limit processing logic

* Default Current Limit Exception Handling

  After the URL current limiting is triggered, the default processing logic is to directly return to "Blocked by Sentinel". If you need to customize the processing logic, the implementation method is as follows:

  ```java
  public class CustomUrlBlockHandler implements UrlBlockHandler {
      @Override
      public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
          // todo add your logic
      }
  }
  
  WebCallbackManager.setUrlBlockHandler(new CustomUrlBlockHandler());
  ```

* Current limit exception handling using `@SentinelResource` annotations

  If you need to customize the processing logic, fill in `@SentinelResource` the attribute of the note `blockHandler` (for all types `BlockException`, you need to make your own judgment) or `fallback` the attribute (for the fuse degradation exception). **There are restrictions on the signature and location of the corresponding method** See for [Sentinel Annotation Support Document](https://github.com/alibaba/Sentinel/wiki/%E6%B3%A8%E8%A7%A3%E6%94%AF%E6%8C%81#sentinelresource-%E6%B3%A8%E8%A7%A3) details. An example implementation is as follows:

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

A simple `@SentinelResource` example can be found in [sentinel-demo-annotation-spring-aop](https://github.com/alibaba/Sentinel/tree/2021.x/sentinel-demo/sentinel-demo-annotation-spring-aop).

### Sentinel Circuitbreaker Example

This Example mainly demonstrates the use of OpenFeign integrated Sentinel circuit breaker.

#### Prepare the configuration file

1. Add a configuration to the Configuration Center. DataId is `sentinel-circuitbreaker-rules.yml`

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

#### Verify that the configuration is in effect

Start the project main class `FeignCircuitBreakerApplication`

##### Verify that the default Feign client takes effect

Access http://localhost/test/default/false  2 times (within 1 second) and then access http://localhost/test/default/true  the circuit breaker in the open state

##### Verify that the specified Feign client is in effect

Access http://localhost/test/feign/false  2 times (within 1 second) and then access http://localhost/test/feign/true  the circuit breaker in the open state

##### Verify that the Feign client specified method takes effect

Access http://localhost/test/feignMethod/false  2 times (within 1 second) and then access http://localhost/test/feignMethod/true  the circuit breaker in the open state

#### Rule dynamic refresh

Modify the rules of the configuration center, and then access the above interface.

### Sentinel OpenFeign Example

This Example demonstrates the integration of OpenFeing and Sentinel. In Example, httpbin is used as a background API interface service.

#### Project preparation

> **Note: The code in this project has completed the corresponding function and does not need to be modified.**

The project supports two startup modes: startup through the main class `OpenFeignApplication` and startup through the Jar package.

#### Call the test

After the project is started, you can access the corresponding URL to view the corresponding Sentinel flow control effect.

> **Note: The RestTemplate provided in the project is the same as the Webclient Example.**

## Endpoint information viewing

Spring Boot applications support the exposure of relevant information through Endpoints, `spring-cloud-starter-alibaba-sentinel` as well.

Before using it, you need to add `spring-boot-starter-actuator` dependencies in Maven and allow Endpoints access in the configuration.
* Add configuration in Spring Boot 1.x
* Adding Configuration in Spring Boot 2.x

Spring Boot 1.x can view Sentinel Endpoint information by visiting http://127.0.0.1:18083/sentinel . Spring Boot 2.x can be accessed by visiting http://127.0.0.1:18083/actuator/sentinel .

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532084199224-1a41591d-7a06-4680-be8a-5de319ac635d.png" width="480" heigh='360' ></p>

## View real-time monitoring
The Sentinel console supports real-time monitoring and viewing. You can view information such as the number of requests passed and the number of throttled flows for each link through the Sentinel console. Where `p_qps` is a pass flow controlled QPS and `b_qps` is a blocked QPS.

<p align="center"><img src="https://cdn.nlark.com/lark/0/2018/png/54319/1532313595369-8428cd7d-9eb7-4786-a149-acf0da4a2daf.png" width="480" heigh='180' ></p>

## ReadableData Source support

The Sentinel provides [Implementation of ReadableDataSource by Extending Dynamic Rule](https://github.com/alibaba/Sentinel/wiki/%E5%8A%A8%E6%80%81%E8%A7%84%E5%88%99%E6%89%A9%E5%B1%95#datasource-%E6%89%A9%E5%B1%95) internally.

Sentinel starter incorporates several classes of ReadableDataSources that exist today. The DataSource is automatically registered in the Spring container by simply making the relevant configuration in the configuration file.

 `FileRefreshableDataSource` and `NacosDataSource`, configured as follows:

```properties
spring.cloud.sentinel.datasource.ds1.file.file=classpath: degraderule.json
spring.cloud.sentinel.datasource.ds1.file.data-type=json

spring.cloud.sentinel.datasource.ds2.nacos.server-addr=127.0.0.1:8848
spring.cloud.sentinel.datasource.ds2.nacos.dataId=sentinel
spring.cloud.sentinel.datasource.ds2.nacos.groupId=DEFAULT_GROUP
spring.cloud.sentinel.datasource.ds2.nacos.data-type=json
```

 `ds1` And `ds2` the name that represents the ReadableDataSource. Feel free to write. The sum `nacos` following `file` the `ds1` and `ds2` represents the type of the ReadableDataSource.

Currently supports `file`, `nacos`, `zk` `apollo`, `redis` these 5 types.

Among them `nacos` `zk` `apollo`, the use of `redis` these four types requires the addition of corresponding dependencies `sentinel-datasource-nacos` `sentinel-datasource-zookeeper` `sentinel-datasource-apollo` `sentinel-datasource-redis`.

When `ReadableDataSource` the rule data is loaded successfully, the console will print out the corresponding log information:

```
[Sentinel Starter] DataSource ds1-sentinel-file-datasource load 3 DegradeRule
[Sentinel Starter] DataSource ds2-sentinel-nacos-datasource load 2 FlowRule
```

## More
Sentinel is a powerful middleware that protects the stability of services from multiple dimensions such as flow control, fuse degradation, and system load protection. This Demo only demonstrates the use of Sentinel as a current limiting tool. For more information about Sentinel, please refer to [Project Sentinel](https://github.com/alibaba/Sentinel).

If you `spring-cloud-starter-alibaba-sentinel` have any suggestions or ideas, please feel free to send them to us in the issue or through other community channels.
