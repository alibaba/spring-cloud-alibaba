# Spring Cloud Alibaba Nacos Example

## 项目说明

本项目演示如何使用 Spring Cloud Alibaba Nacos 相关 Starter 完成 Spring Cloud 应用的服务发现和配置管理。

[Nacos](https://github.com/alibaba/Nacos) 是阿里巴巴开源的一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。

## 正确配置并启动 Nacos Server 2.2.x

在 Nacos 2.2.x 中，加入了用户鉴权相关的功能，在首次启动 Nacos Server 时，需要正确配置，避免出现启动失败的问题。

### 下载 Nacos Server

> 本示例中使用 Nacos Server 版本为 2.2.3！

Nacos 支持直接下载和源码构建两种方式。**推荐在 Spring Cloud Alibaba 2022.x 中使用 Nacos Server 2.2.3 版本。**

1. 直接下载：[Nacos Server 下载页](https://github.com/alibaba/nacos/releases)
2. 源码构建：进入 Nacos [Github 项目页面](https://github.com/alibaba/nacos)，将代码 git clone 到本地自行编译打包，[参考文档](https://nacos.io/zh-cn/docs/quick-start.html)。

### 配置 Nacos Server

打开 `\nacos-server-2.2.3\conf\application.properties` 配置文件，修改以下配置项：

#### 配置数据源

此处以 MySQL 数据库为例，使用 `nacos-server-2.2.3\conf\mysql-schema.sql` 初始化数据库表文件。同时修改以下配置

```properties
#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
spring.datasource.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user.0=root
db.password.0=root

### Connection pool configuration: hikariCP
db.pool.config.connectionTimeout=30000
db.pool.config.validationTimeout=10000
db.pool.config.maximumPoolSize=20
db.pool.config.minimumIdle=2
```

#### 开启鉴权

**注意：不开启在 2.2.x 中会出现登陆失败异常！**

```properties
### The auth system to use, currently only 'nacos' and 'ldap' is supported:
nacos.core.auth.system.type=nacos

### If turn on auth system:
nacos.core.auth.enabled=true
```

#### 设置服务端验证 key

```properties
nacos.core.auth.server.identity.key=test
nacos.core.auth.server.identity.value=test
```

#### 设置默认 token

```properties
### The default token (Base64 String):
nacos.core.auth.plugin.nacos.token.secret.key=SecretKey012345678901234567890123456789012345678901234567890123456789
```

**在使用 Nacos 服务发现和配置功能时，一定要配置 `username` 和 `password` 属性，否则会出现用户未找到异常！**

#### Open API 鉴权

在 nacos server 2.2.x 中使用 Open api 接口时需要鉴权：更多细节请参考：[Nacos api 鉴权](https://nacos.io/zh-cn/docs/auth.html)

1. 获取 accessToken：使用用户名和密码登陆 nacos server：

   `curl -X POST '127.0.0.1:8848/nacos/v1/auth/login' -d 'username=nacos&password=nacos'`

   若用户名和密码正确,返回信息如下:

   `{"accessToken":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuYWNvcyIsImV4cCI6MTYwNTYyOTE2Nn0.2TogGhhr11_vLEjqKko1HJHUJEmsPuCxkur-CfNojDo","tokenTtl":18000,"globalAdmin":true}`

2. 使用 accessToken 请求 nacos api 接口：
   
   `curl -X GET '127.0.0.1:8848/nacos/v1/cs/configs?accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuYWNvcyIsImV4cCI6MTYwNTYyMzkyM30.O-s2yWfDSUZ7Svd3Vs7jy9tsfDNHs1SuebJB4KlNY8Q&dataId=nacos.example.1&group=nacos_group'`

### 启动 Nacos Server

1. 启动 Nacos Server，进入下载到本地并解压完成后的文件夹(使用源码构建的方式则进入编译打包好的文件夹)，再进去其相对文件夹 `nacos/bin`，并对照操作系统实际情况执行如下命令。[详情参考此文档](https://nacos.io/zh-cn/docs/quick-start.html)。

   1. Linux/Unix/Mac 操作系统，执行命令 

      `sh startup.sh -m standalone`

   2. Windows 操作系统，执行命令 

      `cmd startup.cmd`

2. 访问 Nacos Server Console

   浏览器输入地址 http://127.0.0.1:8848/nacos ，**首次登陆需要绑定 nacos 用户，因为新版本增加了鉴权，需要应用注册和配置绑定时配置用户名和密码。**



## Nacos 应用示例

### Spring Cloud Alibaba Nacos Config

#### 应用接入

在启动应用示例进行项目功能演示之前，先了解一下 Spring Cloud 应用如何接入 Nacos Config 作为服务配置中心。

**注意 本章节只是为了便于理解接入方式，本示例代码中已经完成接入工作，无需再进行修改。**

1. 首先，修改 `pom.xml` 文件，引入 spring-cloud-starter-alibaba-nacos-config ；

   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
   </dependency>
   ```

2. 在应用的 `/src/main/resources/application.yaml` 配置文件中配置 Nacos 地址并引入服务配置；

   ```yml
   spring:
     cloud:
       nacos:
         serverAddr: 127.0.0.1:8848
         # 以下配置项必须填写
         username: 'nacos'
         password: 'nacos'
     config:
       import:
         - nacos:nacos-config-example.properties?refresh=true&group=DEFAULT_GROUP
   ```

3. 完成上述两步后，应用会从 Nacos Server 中获取相应的配置，并添加在 Spring Environment 的 PropertySources 中。使用 Nacos 配置中心保存 Nacos 的部分配置时，有以下四种方式:
   - BeanAutoRefreshConfigExample:  通过将配置信息配置为bean，支持配置变自动刷新的例子；
   - ConfigListenerExample:               监听配置信息的例子；
   - DockingInterfaceExample:            对接 Nacos 接口，通过接口完成对配置信息增删改查的例子；
   - ValueAnnotationExample:            通过 @Value 注解进行配置信息获取的例子。

#### Nacos Server 中添加配置

在命令行执行如下命令，向 Nacos Server 中添加一条配置。**可直接通过 Nacos 控制台注入！**

> **注意：需要替换 accessToken。**

```shell
$ curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?accessToken=XXXXXXXXXXXXXXXXXXXXXXXX&dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=spring.cloud.nacos.config.serverAddr=127.0.0.1:8848%0Aspring.cloud.nacos.config.prefix=PREFIX%0Aspring.cloud.nacos.config.group=GROUP%0Aspring.cloud.nacos.config.namespace=NAMESPACE"
```

添加的配置详情如下：

```properties
# dataId 为 nacos-config-example.properties
# group 为 DEFAULT_GROUP

# 内容如下:

spring.cloud.nacos.config.serveraddr=127.0.0.1:8848
spring.cloud.nacos.config.prefix=PREFIX
spring.cloud.nacos.config.group=GROUP
spring.cloud.nacos.config.namespace=NAMESPACE
```

#### 应用启动

1. 增加配置，在应用的 `/src/main/resources/application.yml` 中添加基本配置信息；

   ```yml
   server:
     port: 18084
   management:
     endpoints:
       web:
         exposure:
           include: '*'
   ```

2. 启动应用，支持 IDE 直接启动和编译打包后启动。

    1. IDE直接启动：找到主类 `NacosConfigApplication`，执行 main 方法启动应用。
    2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，进入 `target` 文件夹执行 `java -jar nacos-config-example.jar` 启动应用。

#### 验证

##### 验证自动注入

在浏览器地址栏输入 `http://127.0.0.1:18084/nacos/bean`，并点击调转，可以看到成功从 Nacos Server 中获取了数据。

![get](https://tva1.sinaimg.cn/large/e6c9d24ely1h2gbowleyrj20o40bo753.jpg)

##### 验证动态刷新

1. 执行如下命令，修改 Nacos Server 端的配置数据

   ```shell
   $ curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?accessToken=XXXXXXXXXXXXXXXXXXXXXXXXXXX&dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=spring.cloud.nacos.config.serveraddr=127.0.0.1:8848%0Aspring.cloud.nacos.config.prefix=PREFIX%0Aspring.cloud.nacos.config.group=DEFAULT_GROUP%0Aspring.cloud.nacos.config.namespace=NAMESPACE"
   ```

2. 在浏览器地址栏输入 `http://127.0.0.1:18084/nacos/bean`，并点击调转，可以看到应用从 Nacos Server 中获取了最新的数据，group 变成了 DEFAULT_GROUP。

![refresh](https://tva1.sinaimg.cn/large/e6c9d24ely1h2gbpram9rj20nq0ccmxz.jpg)

#### 原理

##### Nacos Config 数据结构

Nacos Config 主要通过 dataId 和 group 来唯一确定一条配置，假定您已经了解此背景。如果不了解，请参考 [Nacos 文档](https://nacos.io/zh-cn/docs/concepts.html)。

Nacos Client 从 Nacos Server 端获取数据时，调用的是此接口 `ConfigService.getConfig(String dataId, String group, long timeoutMs)`。

##### Spring Cloud 应用获取数据

###### dataID

在 Nacos Config Starter 中，dataId 的拼接格式如下

	${prefix} - ${spring.profiles.active} . ${file-extension}

* `prefix` 默认为 `spring.application.name` 的值，也可以通过配置项 `spring.cloud.nacos.config.prefix`来配置。

* `spring.profiles.active` 即为当前环境对应的 profile，详情可以参考 [Spring Boot文档](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html#boot-features-profiles)

  **注意，当 active profile 为空时，对应的连接符 `-` 也将不存在，dataId 的拼接格式变成 `${prefix}`.`${file-extension}`**

* `file-extension` 为配置内容的数据格式，可以通过配置项 `spring.cloud.nacos.config.file-extension`来配置。
  目前只支持 `properties` 类型。

###### group
* `group` 默认为 `DEFAULT_GROUP`，可以通过 `spring.cloud.nacos.config.group` 配置。


##### 自动注入
Spring Cloud Alibaba Nacos Config Starter 实现了 `org.springframework.boot.context.config.ConfigDataLoader` 接口，并将优先级设置成了最高。

在 Spring Cloud 应用启动阶段，会主动从 Nacos Server 端获取对应的数据，并将获取到的数据转换成 PropertySource 且注入到 Environment 的 PropertySources 属性中，所以使用 @Value 注解也能直接获取 Nacos Server 端配置的内容。

##### 动态刷新

Nacos Config Starter 默认为所有获取数据成功的 Nacos 的配置项添加了监听功能，在监听到服务端配置发生变化时会实时触发 `org.springframework.cloud.context.refresh.ContextRefresher` 的 refresh 方法 。

如果需要对 Bean 进行动态刷新，请参照 Spring 和 Spring Cloud 规范。推荐给类添加 `@RefreshScope` 或 `@ConfigurationProperties ` 注解，

更多详情请参考 [ContextRefresher Java Doc](http://static.javadoc.io/org.springframework.cloud/spring-cloud-context/2.0.0.RELEASE/org/springframework/cloud/context/refresh/ContextRefresher.html)。

#### Endpoint 信息查看

Spring Boot 应用支持通过 Endpoint 来暴露相关信息，Spring Cloud Alibaba Nacos Config Starter 也支持这一点。

在使用之前需要在 maven 中添加 `spring-boot-starter-actuator`依赖，并在配置中允许 Endpoints 的访问。

Spring Boot 3.x 可以通过访问 http://127.0.0.1:18084/actuator/nacosconfig 来访问。

![actuator](https://cdn.nlark.com/lark/0/2018/png/54319/1536986344822-279e1edc-ebca-4201-8362-0ddeff240b85.png)

如上图所示，Sources 表示此客户端从哪些 Nacos Config 配置项中获取了信息，RefreshHistory 表示动态刷新的历史记录，最多保存20条，NacosConfigProperties 则为 Nacos Config Starter 本身的配置。

#### More

##### 更多配置项
配置项|key|默认值|说明
----|----|-----|-----
服务端地址|spring.cloud.nacos.config.server-addr||服务器ip和端口
DataId前缀|spring.cloud.nacos.config.prefix|${spring.application.name}|DataId的前缀，默认值为应用名称
Group|spring.cloud.nacos.config.group|DEFAULT_GROUP|
DataId后缀及内容文件格式|spring.cloud.nacos.config.file-extension|properties|DataId的后缀，同时也是配置内容的文件格式，目前只支持 properties
配置内容的编码方式|spring.cloud.nacos.config.encode|UTF-8|配置的编码
获取配置的超时时间|spring.cloud.nacos.config.timeout|3000|单位为 ms
配置的命名空间|spring.cloud.nacos.config.namespace||常用场景之一是不同环境的配置的区分隔离，例如开发测试环境和生产环境的资源隔离等。
AccessKey|spring.cloud.nacos.config.access-key||
SecretKey|spring.cloud.nacos.config.secret-key||
相对路径|spring.cloud.nacos.config.context-path||服务端 API 的相对路径
接入点|spring.cloud.nacos.config.endpoint||地域的某个服务的入口域名，通过此域名可以动态地拿到服务端地址
是否开启监听和自动刷新|spring.cloud.nacos.config.refresh-enabled|true|
集群服务名|spring.cloud.nacos.config.cluster-name||

### Spring Cloud Alibaba Nacos Discovery

#### 如何接入

在启动 Nacos Discovery 示例进行演示之前，了解一下 Spring Cloud 应用如何接入 Nacos Discovery。

**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 `pom.xml` 文件，引入 spring-cloud-alibaba-nacos-discovery-starter；

   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
   </dependency>
   ```

2. 在应用的 `/src/main/resources/application.properties` 配置文件中配置 Nacos Server 地址；

   ```properties
   spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
   ```

3. 使用 @EnableDiscoveryClient 注解开启服务注册与发现功能；

   ```java
   @SpringBootApplication
   @EnableDiscoveryClient
   public class ProviderApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(ProviderApplication.class, args);
       }
   
       @RestController
       class EchoController {
           @GetMapping(value = "/echo/{string}")
           public String echo(@PathVariable String string) {
                   return string;
           }
       }
   }
   ```

#### 应用启动

1. 增加配置，在 nacos-discovery-provider-example 项目的 `/src/main/resources/application.properties` 中添加基本配置信息；

   ```properties
   spring.application.name=service-provider
   server.port=18082
   ```

2. 启动应用，支持 IDE 直接启动和编译打包后启动。

    1. IDE直接启动：找到 nacos-discovery-provider-example 项目的主类 `ProviderApplication`，执行 main 方法启动应用。
    2. 打包编译后启动：在 nacos-discovery-provider-example 项目中执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar nacos-discovery-provider-example.jar`启动应用。

#### 查询服务验证

在浏览器输入此地址 `http://127.0.0.1:8848/nacos/v1/ns/catalog/instances?serviceName=service-provider&clusterName=DEFAULT&pageSize=10&pageNo=1&namespaceId=`，并点击跳转，可以看到服务节点已经成功注册到 Nacos Server。

![查询服务](https://cdn.nlark.com/lark/0/2018/png/54319/1536986288092-5cf96af9-9a26-466b-85f6-39ad1d92dfdc.png)

#### 服务发现集成 Spring Cloud Loadbalancer

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

增加如下配置，使用 Spring Cloud Alibaba 社区针对 Spring Cloud Loadbalancer 负载均衡依赖提供的负载均衡策略，以便使用 Spring Cloud Alibaba 提供的所有的能力：

```properties
spring.cloud.loadbalancer.ribbon.enabled=false
spring.cloud.loadbalancer.nacos.enabled=true
```

#### IPv4 至 IPv6 地址迁移方案

##### IPv4 和 IPv6 地址双注册

在配置完成 Spring Cloud Loadbalancer 作为负载均衡策略后，应用启动后会默认将微服务的 IPv4 地址和 IPv6 地址注册到注册中心中，其中 IPv4 地址会存放在 Nacos 服务列表中的 IP 字段下，IPv6 地址在 Nacos 的 metadata 字段中，其对应的 Key 为 IPv6。当服务消费者调用服务提供者时，会根据自身的 IP 地址栈支持情况，选择合适的 IP 地址类型发起服务调用。具体规则：
（1）服务消费者本身支持 IPv4 和 IPv6 双地址栈或仅支持 IPv6 地址栈的情况下，服务消费者会使用服务提供的 IPv6 地址发起服务调用，IPv6 地址调用失败如本身还同事支持 IPv4 地址栈时，暂不支持切换到 IPv4 再发起重试调用；
（2）服务消费者本身仅支持 IPv4 单地址栈的情况下，服务消费者会使用服务提供的 IPv4 地址发起服务调用。

##### 仅注册 IPv4
如果您只想使用 IPv4 地址进行注册，可以在 application.properties 使用以下配置：

```properties
spring.cloud.nacos.discovery.ip-type=IPv4
```

##### 仅注册 IPv6

如果您只想使用 IPv6 地址，可以在 application.properties 使用以下配置：

```properties
spring.cloud.nacos.discovery.ip-type=IPv6
```

#### 使用 RestTemplate 和 FeignClient

下面将分析 nacos-discovery-consumer-example 项目的代码，演示如何 RestTemplate 与 FeignClient。

**注意 本章节只是为了便于理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。此处只涉及 Ribbon、RestTemplate、FeignClient 相关的内容，如果已经使用了其他服务发现组件，可以通过直接替换依赖来接入 Nacos Discovery。**

1. 添加 @LoadBalanced 注解，使得 RestTemplate 接入 Ribbon

   ```java
   @Bean
   @LoadBalanced
   public RestTemplate restTemplate() {
      return new RestTemplate();
   }
   ```

2. FeignClient 已经默认集成了 Ribbon ，此处演示如何配置一个 FeignClient。

   ```java
   @FeignClient(name = "service-provider")
   public interface EchoService {
      @GetMapping(value = "/echo/{str}")
      String echo(@PathVariable("str") String str);
   }
   ```

   使用 @FeignClient 注解将 EchoService 这个接口包装成一个 FeignClient，属性 name 对应服务名 service-provider。

   echo 方法上的 @RequestMapping 注解将 echo 方法与 URL "/echo/{str}" 相对应，@PathVariable 注解将 URL 路径中的 `{str}` 对应成 echo 方法的参数 str。

3. 完成以上配置后，将两者自动注入到 TestController 中。

   ```java
   @RestController
   public class TestController {
   
       @Autowired
       private RestTemplate restTemplate;
       @Autowired
       private EchoService echoService;
   
       @GetMapping(value = "/echo-rest/{str}")
       public String rest(@PathVariable String str) {
           return restTemplate.getForObject("http://service-provider/echo/" + str, String.class);
       }
       @GetMapping(value = "/echo-feign/{str}")
       public String feign(@PathVariable String str) {
           return echoService.echo(str);
       }
   }
   ```

4. 配置必要的配置，在 nacos-discovery-consumer-example 项目的 `/src/main/resources/application.properties` 中添加基本配置信息

   ```java
   @RestController
   public class TestController {
   
       @Autowired
       private RestTemplate restTemplate;
       @Autowired
       private EchoService echoService;
   
       @GetMapping(value = "/echo-rest/{str}")
       public String rest(@PathVariable String str) {
           return restTemplate.getForObject("http://service-provider/echo/" + str, String.class);
       }
       @GetMapping(value = "/echo-feign/{str}")
       public String feign(@PathVariable String str) {
           return echoService.echo(str);
       }
   }
   ```

5.启动应用，支持 IDE 直接启动和编译打包后启动。

1. IDE 直接启动：找到 nacos-discovery-consumer-example 项目的主类 `ConsumerApplication`，执行 main 方法启动应用。
2. 打包编译后启动：在 nacos-discovery-consumer-example 项目中执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar nacos-discovery-consumer-example.jar` 启动应用。

#### 验证

1. 在浏览器地址栏中输入 http://127.0.0.1:18083/echo-rest/1234，点击跳转，可以看到浏览器显示了 nacos-discovery-provider-example 返回的消息 "hello Nacos Discovery 1234"，证明服务发现生效。

![rest](https://cdn.nlark.com/lark/0/2018/png/54319/1536986302124-ee27670d-bdcc-4210-9f5d-875acec6d3ea.png)

2. 在浏览器地址栏中输入 http://127.0.0.1:18083/echo-feign/12345，点击跳转，可以看到浏览器显示 nacos-discovery-provider-example 返回的消息 "hello Nacos Discovery 12345"，证明服务发现生效。

![feign](https://cdn.nlark.com/lark/0/2018/png/54319/1536986311685-6d0c1f9b-a453-4ec3-88ab-f7922d210f65.png)

#### 原理

##### 服务注册

Spring Cloud Alibaba Nacos Discovery 遵循了 Spring Cloud Common 标准，实现了 AutoServiceRegistration、ServiceRegistry、Registration 这三个接口。

在 Spring Cloud 应用的启动阶段，监听了 WebServerInitializedEvent 事件，当 Web 容器初始化完成后，即收到 WebServerInitializedEvent 事件后，会触发注册的动作，调用 ServiceRegistry 的 register 方法，将服务注册到 Nacos Server。

#### Endpoint 信息查看

Spring Boot 应用支持通过 Endpoint 来暴露相关信息，Spring Cloud Alibaba Nacos Discovery Starter 也支持这一点。

在使用之前需要在 maven 中添加 `spring-boot-starter-actuator`依赖，并在配置中允许 Endpoints 的访问。

Spring Boot 3.x 可以通过访问 http://127.0.0.1:18083/actuator/nacos-discovery 来访问。

![actuator](https://cdn.nlark.com/lark/0/2018/png/54319/1536986319285-d542dc5f-5dff-462a-9f52-7254776bcd99.png)

如上图所示，NacosDiscoveryProperties 则为 Spring Cloud Alibaba Nacos Discovery 本身的配置，也包括本机注册的内容，subscribe 为本机已订阅的服务信息。

#### More

##### 更多配置项

配置项|key|默认值|说明
----|----|-----|-----
服务端地址|spring.cloud.nacos.discovery.server-addr||
服务名|spring.cloud.nacos.discovery.service|${spring.application.name}|注册到Nacos上的服务名称，默认值为应用名称
权重|spring.cloud.nacos.discovery.weight|1|取值范围 1 到 100，数值越大，权重越大
网卡名|spring.cloud.nacos.discovery.network-interface||当IP未配置时，注册的IP为此网卡所对应的IP地址，如果此项也未配置，则默认取第一块网卡的地址
注册的IP地址|spring.cloud.nacos.discovery.ip||优先级最高
注册的IP地址类型|spring.cloud.nacos.discovery.ip-type|双栈地址|可以配置IPv4和IPv6两种类型，如果网卡同类型IP地址存在多个，希望制定特定网段地址，可使用`spring.cloud.inetutils.preferred-networks`配置筛选地址
注册的端口|spring.cloud.nacos.discovery.port|-1|默认情况下不用配置，会自动探测
命名空间|spring.cloud.nacos.discovery.namespace||常用场景之一是不同环境的注册的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等。
AccessKey|spring.cloud.nacos.discovery.access-key||
SecretKey|spring.cloud.nacos.discovery.secret-key||
Metadata|spring.cloud.nacos.discovery.metadata||使用Map格式配置
日志文件名|spring.cloud.nacos.discovery.log-name||
集群|spring.cloud.nacos.discovery.cluster-name|DEFAULT|Nacos集群名称
接入点|spring.cloud.nacos.discovery.endpoint||地域的某个服务的入口域名，通过此域名可以动态地拿到服务端地址
是否集成LoadBalancer|spring.cloud.loadbalancer.nacos.enabled|false|
是否开启Nacos Watch|spring.cloud.nacos.discovery.watch.enabled|false|可以设置成true来开启 watch
是否启用Nacos|spring.cloud.nacos.discovery.enabled|true|默认启动，设置为false时会关闭自动向Nacos注册的功能

### Spring Cloud Alibaba Nacos 集成 Spring Cloud Gateway

#### 如何接入

在启动示例进行演示之前，了解一下 Spring Cloud 应用如何接入 Spring Cloud 如何接入 Nacos Discovery、Spring Cloud Gateway。

**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 `pom.xml` 文件，引入 Spring Cloud Alibaba Nacos Discovery Starter、Spring Cloud Gateway Starter。

   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-gateway</artifactId>
   </dependency>
   ```

2. 在应用的 `/src/main/resources/application.properties` 配置文件中配置 Nacos Server 地址

   ```properties
   spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
   ```

3. 在应用的 `/src/main/resources/application.properties` 配置文件中配置 Spring Cloud Gateway 路由

   ```properties
   spring.cloud.gateway.routes[0].id=nacos-route
   spring.cloud.gateway.routes[0].uri=lb://service-gateway-provider
   spring.cloud.gateway.routes[0].predicates[0].name=Path
   spring.cloud.gateway.routes[0].predicates[0].args[pattern]=/nacos/**
   spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1
   ```

4. 使用 @EnableDiscoveryClient 注解开启服务注册与发现功能

   ```java
   @SpringBootApplication
   @EnableDiscoveryClient
   public class GatewayApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(GatewayApplication.class, args);
       }
   
   }
   ```c

#### Spring Cloud Gateway 应用启动

启动应用，支持 IDE 直接启动和编译打包后启动。

1. IDE直接启动：找到 nacos-gateway-discovery-example 项目的主类 `GatewayApplication`，执行 main 方法启动应用。
2. 打包编译后启动：在 nacos-gateway-discovery-example 项目中执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar nacos-gateway-discovery-example.jar`启动应用。

#### 服务提供方应用启动

启动应用，支持 IDE 直接启动和编译打包后启动。

1. IDE 直接启动：找到 nacos-gateway-provider-example 项目的主类 `ProviderApplication`，执行 main 方法启动应用。
2. 打包编译后启动：在 nacos-gateway-provider-example 项目中执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar nacos-gateway-provider-example.jar`启动应用。

#### 验证

1.

```bash
 curl 'http://127.0.0.1:18085/nacos/echo/hello-world' 
 
 hello Nacos Discovery hello-world⏎
```

2.

```bash
 curl 'http://127.0.0.1:18085/nacos/divide?a=6&b=2' 

 3⏎              
```

## Native Image构建

请参考 Spring Cloud Alibaba 官网中的 [Graalvm 快速开始](https://sca.aliyun.com/zh-cn/docs/2022.0.0.0/user-guide/graalvm/quick-start)

## 更多介绍

Nacos 为用户提供包括动态服务发现，配置管理，服务管理等服务基础设施，帮助用户更灵活，更轻松地构建，交付和管理他们的微服务平台，基于 Nacos, 用户可以更快速的构建以“服务”为中心的现代云原生应用。Nacos 可以和 Spring Cloud、Kubernetes/CNCF、Dubbo 等微服务生态无缝融合，为用户提供更卓越的体验。更多 Nacos 相关的信息，请参考 [Nacos 项目](https://github.com/alibaba/Nacos)。

如果您对 Spring Cloud Nacos Discovery 有任何建议或想法，欢迎在 issue 中或者通过其他社区渠道向我们提出。

