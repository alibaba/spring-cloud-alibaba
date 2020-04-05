# Dubbo Spring Cloud 示例工程


## 快速开始

### 定义 Dubbo 服务接口

Dubbo 服务接口是服务提供方与消费方的远程通讯契约，通常由普通的 Java 接口（interface）来声明，如 `EchoService` 接口：

```java
public interface EchoService {

    String echo(String message);
}
```

为了确保契约的一致性，推荐的做法是将 Dubbo 服务接口打包在第二方或者第三方的 artifact（jar）中，如以上接口就存放在
 artifact [spring-cloud-dubbo-sample-api](https://github.com/alibaba/spring-cloud-alibaba/tree/master/spring-cloud-alibaba-examples/spring-cloud-alibaba-dubbo-examples/spring-cloud-dubbo-sample-api) 之中。
对于服务提供方而言，不仅通过依赖 artifact 的形式引入 Dubbo 服务接口，而且需要将其实现。对应的服务消费端，同样地需要依赖该 artifact，
并以接口调用的方式执行远程方法。接下来进一步讨论怎样实现 Dubbo 服务提供方和消费方。


### 实现 Dubbo 服务提供方



#### 初始化 `spring-cloud-dubbo-server-sample` Maven 工程

首先，创建 `artifactId` 名为 `spring-cloud-dubbo-server-sample` 的 Maven 工程，并在其  `pom.xml` 文件中增添 
Dubbo Spring Cloud 必要的依赖：

```xml
<dependencies>
    <!-- Sample API -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dubbo-sample-api</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- Spring Boot dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-actuator</artifactId>
    </dependency>

    <!-- Dubbo Spring Cloud Starter -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-dubbo</artifactId>
    </dependency>

    <!-- Spring Cloud Nacos Service Discovery -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
</dependencies>
```

以上依赖 artifact 说明如下：

- `spring-cloud-dubbo-sample-api` : 提供 `EchoService` 接口的 artifact
- `spring-boot-actuator` : Spring Boot Production-Ready artifact，间接引入 `spring-boot` artifact
- `spring-cloud-starter-dubbo` : Dubbo Spring Cloud Starter `artifact`，间接引入 `dubbo-spring-boot-starter` 等 artifact
- `spring-cloud-starter-alibaba-nacos-discovery` : Nacos Spring Cloud 服务注册与发现 `artifact`


值得注意的是，以上 artifact 未指定版本(version)，因此，还需显示地声明 `<dependencyManagement>` :

```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Cloud Alibaba dependencies -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>0.9.0.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

> 以上完整的 Maven 依赖配置，请参考 `spring-cloud-dubbo-server-sample` [`pom.xml`](spring-cloud-dubbo-server-sample/pom.xml) 文件

完成以上步骤之后，下一步则是实现 Dubbo 服务


#### 实现 Dubbo 服务

`EchoService` 作为暴露的 Dubbo 服务接口，服务提供方 `spring-cloud-dubbo-server-sample` 需要将其实现：

```java
@org.apache.dubbo.config.annotation.Service
class EchoServiceImpl implements EchoService {

    @Override
    public String echo(String message) {
        return "[echo] Hello, " + message;
    }
}
```

其中，`@org.apache.dubbo.config.annotation.Service` 是 Dubbo 服务注解，仅声明该 Java 服务（本地）实现为 Dubbo 服务。
因此，下一步需要将其配置 Dubbo 服务（远程）。



#### 配置 Dubbo 服务提供方

在暴露 Dubbo 服务方面，推荐开发人员外部化配置的方式，即指定 Java 服务实现类的扫描基准包。
> Dubbo Spring Cloud 继承了 Dubbo Spring Boot 的外部化配置特性，也可以通过标注 `@DubboComponentScan` 来实现基准包扫描。

同时，Dubbo 远程服务需要暴露网络端口，并设定通讯协议，完整的 YAML 配置如下所示：

```yaml
dubbo:
  scan:
    # dubbo 服务扫描基准包
    base-packages: com.alibaba.cloud.dubbo.bootstrap
  protocol:
    # dubbo 协议
    name: dubbo
    # dubbo 协议端口（ -1 表示自增端口，从 20880 开始）
    port: -1
  registry:
    # 挂载到 Spring Cloud 注册中心
    address: spring-cloud://localhost
    
spring:
  application:
    # Dubbo 应用名称
    name: spring-cloud-alibaba-dubbo-server
  main:
    # Spring Boot 2.1 需要设定
    allow-bean-definition-overriding: true
  cloud:
    nacos:
      # Nacos 服务发现与注册配置
      discovery:
        server-addr: 127.0.0.1:8848
```

以上 YAML 内容，上半部分为 Dubbo 的配置：

- `dubbo.scan.base-packages` : 指定 Dubbo 服务实现类的扫描基准包
- `dubbo.protocol` : Dubbo 服务暴露的协议配置，其中子属性 `name` 为协议名称，`port` 为协议端口（ -1 表示自增端口，从 20880 开始）
- `dubbo.registry` : Dubbo 服务注册中心配置，其中子属性 `address` 的值 "spring-cloud://localhost"，说明挂载到 Spring Cloud 注册中心
> 当前 Dubbo Spring Cloud 实现必须配置 `dubbo.registry.address = spring-cloud://localhost`，下一个版本将其配置变为可选
（参考 [issue #592](https://github.com/alibaba/spring-cloud-alibaba/issues/592)），
> 并且支持传统 Dubbo 协议的支持（参考 [issue #588](https://github.com/alibaba/spring-cloud-alibaba/issues/588)）

下半部分则是 Spring Cloud 相关配置：

- `spring.application.name` : Spring 应用名称，用于 Spring Cloud 服务注册和发现。
> 该值在 Dubbo Spring Cloud 加持下被视作 `dubbo.application.name`，因此，无需再显示地配置 `dubbo.application.name`
- `spring.main.allow-bean-definition-overriding` : 在 Spring Boot 2.1 以及更高的版本增加该设定，
因为 Spring Boot 默认调整了 Bean 定义覆盖行为。（推荐一个好的 Dubbo 讨论 [issue #3193](https://github.com/apache/dubbo/issues/3193#issuecomment-474340165)）
- `spring.cloud.nacos.discovery` : Nacos 服务发现与注册配置，其中子属性 server-addr 指定 Nacos 服务器主机和端口

> 以上完整的 YAML 配置文件，请参考 `spring-cloud-dubbo-server-sample` [`bootstrap.yaml`](spring-cloud-dubbo-server-sample/src/main/resources/bootstrap.yaml) 文件


完成以上步骤后，还需编写一个 Dubbo Spring Cloud 引导类。


#### 引导 Dubbo Spring Cloud 服务提供方应用

Dubbo Spring Cloud 引导类与普通 Spring Cloud 应用并无差别，如下所示：
```java
@EnableDiscoveryClient
@EnableAutoConfiguration
public class DubboSpringCloudServerBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(DubboSpringCloudServerBootstrap.class);
    }
}
```

在引导 `DubboSpringCloudServerBootstrap` 之前，请提前启动 Nacos 服务器。
当 `DubboSpringCloudServerBootstrap` 启动后，将应用 `spring-cloud-dubbo-server-sample` 将出现在 Nacos 控制台界面。


当 Dubbo 服务提供方启动后，下一步实现一个 Dubbo 服务消费方。



### 实现 Dubbo 服务消费方

由于 Java 服务就 `EchoService`、服务提供方应用 `spring-cloud-dubbo-server-sample` 以及 Nacos 服务器均已准备完毕。Dubbo 服务消费方
只需初始化服务消费方 Maven 工程 `spring-cloud-dubbo-client-sample` 以及消费 Dubbo 服务。



#### 初始化 `spring-cloud-dubbo-client-sample` Maven 工程

与服务提供方 Maven 工程类，需添加相关 Maven 依赖：

```xml
<dependencyManagement>
    <dependencies>
        <!-- Spring Cloud Alibaba dependencies -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>0.9.0.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Sample API -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dubbo-sample-api</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- Spring Boot dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-actuator</artifactId>
    </dependency>

    <!-- Dubbo Spring Cloud Starter -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-dubbo</artifactId>
    </dependency>

    <!-- Spring Cloud Nacos Service Discovery -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
</dependencies>
```

与应用 `spring-cloud-dubbo-server-sample` 不同的是，当前应用依赖 `spring-boot-starter-web`，表明它属于 Web Servlet 应用。

> 以上完整的 Maven 依赖配置，请参考 `spring-cloud-dubbo-client-sample` [`pom.xml`](spring-cloud-dubbo-client-sample/pom.xml) 文件


#### 配置 Dubbo 服务消费方

Dubbo 服务消费方配置与服务提供方类似，当前应用 `spring-cloud-dubbo-client-sample` 属于纯服务消费方，因此，所需的外部化配置更精简：

```yaml
dubbo:
  registry:
    # 挂载到 Spring Cloud 注册中心
    address: spring-cloud://localhost
  cloud:
    subscribed-services: spring-cloud-alibaba-dubbo-server
    
spring:
  application:
    # Dubbo 应用名称
    name: spring-cloud-alibaba-dubbo-client
  main:
    # Spring Boot 2.1 需要设定
    allow-bean-definition-overriding: true
  cloud:
    nacos:
      # Nacos 服务发现与注册配置
      discovery:
        server-addr: 127.0.0.1:8848
```

对比应用 `spring-cloud-dubbo-server-sample`，除应用名称 `spring.application.name` 存在差异外，`spring-cloud-dubbo-client-sample`
新增了属性 `dubbo.cloud.subscribed-services` 的设置。并且该值为服务提供方应用 "spring-cloud-dubbo-server-sample"。

- `dubbo.cloud.subscribed-services` : 用于服务消费方订阅服务提供方的应用名称的列表，若需订阅多应用，使用 "," 分割。
不推荐使用默认值为 "*"，它将订阅所有应用。
> 当应用使用属性 `dubbo.cloud.subscribed-services` 默认值时，日志中将会输出一行警告：
> > Current application will subscribe all services(size:x) in registry, a lot of memory and CPU cycles may be used,
> > thus it's strongly recommend you using the externalized property 'dubbo.cloud.subscribed-services' to specify the services

由于当前应用属于 Web 应用，它会默认地使用 8080 作为 Web 服务端口，如果需要自定义，可通过属性 `server.port` 调整。

> 以上完整的 YAML 配置文件，请参考 `spring-cloud-dubbo-client-sample` [`bootstrap.yaml`](spring-cloud-dubbo-client-sample/src/main/resources/bootstrap.yaml) 文件



#### 引导 Dubbo Spring Cloud 服务消费方应用

为了减少实现步骤，以下引导类将 Dubbo 服务消费以及引导功能合二为一：

```java
@EnableDiscoveryClient
@EnableAutoConfiguration
@RestController
public class DubboSpringCloudClientBootstrap {

    @Reference
    private EchoService echoService;

    @GetMapping("/echo")
    public String echo(String message) {
        return echoService.echo(message);
    }

    public static void main(String[] args) {
        SpringApplication.run(DubboSpringCloudClientBootstrap.class);
    }
}
```

不仅如此，`DubboSpringCloudClientBootstrap` 也作为 REST Endpoint，通过暴露 `/echo` Web 服务，消费 Dubbo `EchoService` 服务。因此，
可通过 `curl` 命令执行 HTTP GET 方法：

```
$ curl http://127.0.0.1:8080/echo?message=%E5%B0%8F%E9%A9%AC%E5%93%A5%EF%BC%88mercyblitz%EF%BC%89
```

HTTP 响应为：

```
[echo] Hello, 小马哥（mercyblitz）
```

以上结果说明应用 `spring-cloud-dubbo-client-sample` 通过消费 Dubbo 服务，返回服务提供方 `spring-cloud-dubbo-server-sample` 
运算后的内容。

以上操作就一套完整的 Dubbo 服务提供方和消费方的运用，更多的详情请直接参考模块：
- [`spring-cloud-dubbo-server-sample` ](spring-cloud-dubbo-server-sample)
- [`spring-cloud-dubbo-client-sample`](spring-cloud-dubbo-client-sample)




## 模块说明

- [spring-cloud-dubbo-sample-api](spring-cloud-dubbo-sample-api)：API 模块，存放 Dubbo 服务接口和模型定义
- [spring-cloud-dubbo-provider-web-sample](spring-cloud-dubbo-provider-web-sample)：Dubbo Spring Cloud 服务提供方示例（Web 应用）
- [spring-cloud-dubbo-provider-sample](spring-cloud-dubbo-provider-sample)：Dubbo Spring Cloud 服务提供方示例（非 Web 应用）
- [spring-cloud-dubbo-consumer-sample](spring-cloud-dubbo-consumer-sample)：Dubbo Spring Cloud 服务消费方示例
- [spring-cloud-dubbo-servlet-gateway](spring-cloud-dubbo-servlet-gateway-sample)：Dubbo Spring Cloud Servlet 网关简易实现示例
