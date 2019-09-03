# Nacos Config Example

## 项目说明

本项目演示如何使用 Nacos Config Starter 完成 Spring Cloud 应用的配置管理。

[Nacos](https://github.com/alibaba/Nacos) 是阿里巴巴开源的一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。

## 示例

### 如何接入
在启动示例进行演示之前，我们先了解一下 Spring Cloud 应用如何接入 Nacos Config。
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 pom.xml 文件，引入 Nacos Config Starter。

	    <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
	
2. 在应用的 /src/main/resources/bootstrap.properties 配置文件中配置 Nacos Config 元数据
	
        spring.application.name=nacos-config-example
        spring.cloud.nacos.config.server-addr=127.0.0.1:8848
		  
3. 完成上述两步后，应用会从 Nacos Config 中获取相应的配置，并添加在 Spring Environment 的 PropertySources 中。这里我们使用 @Value 注解来将对应的配置注入到 SampleController 的 userName 和 age 字段，并添加 @RefreshScope 打开动态刷新功能
		
		@RefreshScope
		class SampleController {
	
    		@Value("${user.name}")
    		String userName;
	
    		@Value("${user.age}")
    		int age;
		}

### 启动 Nacos Server 并添加配置

1. 首先需要获取 Nacos Server，支持直接下载和源码构建两种方式。**推荐使用最新版本 Nacos Server**

	1. 直接下载：[Nacos Server 下载页](https://github.com/alibaba/nacos/releases) 
	2. 源码构建：进入 Nacos [Github 项目页面](https://github.com/alibaba/nacos)，将代码 git clone 到本地自行编译打包，[参考此文档](https://nacos.io/zh-cn/docs/quick-start.html)。
	
2. 启动 Server，进入下载到本地并解压完成后的文件夹(使用源码构建的方式则进入编译打包好的文件夹)，再进去其相对文件夹 nacos/bin，并对照操作系统实际情况执行如下命令。[详情参考此文档](https://nacos.io/zh-cn/docs/quick-start.html)。
	
	1. Linux/Unix/Mac 操作系统，执行命令 `sh startup.sh -m standalone`
	2. Windows 操作系统，执行命令 `cmd startup.cmd`

3. 在命令行执行如下命令，向 Nacos Server 中添加一条配置。
	
		curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=user.id=1%0Auser.name=james%0Auser.age=17"
		
	**注：你也可以使用其他方式添加，遵循 HTTP API 规范即可，若您使用的 Nacos 版本自带控制台，建议直接使用控制台进行配置**
	
	添加的配置的详情如下
	
		dataId 为 nacos-config-example.properties
		group 为 DEFAULT_GROUP
		
		内容如下
		
			user.id=1
			user.name=james
			user.age=17	

### 应用启动

1. 增加配置，在应用的 /src/main/resources/application.properties 中添加基本配置信息
	
        server.port=18084
        management.endpoints.web.exposure.include=*

		
2. 启动应用，支持 IDE 直接启动和编译打包后启动。

	1. IDE直接启动：找到主类 `Application`，执行 main 方法启动应用。
	2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar nacos-config-example.jar`启动应用。

### 验证

#### 验证自动注入
在浏览器地址栏输入 `http://127.0.0.1:18084/user`，并点击调转，可以看到成功从 Nacos Config Server 中获取了数据。

![get](https://cdn.nlark.com/lark/0/2018/png/54319/1536986328663-5e3503c2-7e14-4c56-b5f9-72fecc6898d2.png)

#### 验证动态刷新
1. 执行如下命令，修改 Nacos Server 端的配置数据

		curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=user.id=1%0Auser.name=james%0Auser.age=18"

2. 在浏览器地址栏输入 `http://127.0.0.1:18084/user`，并点击调转，可以看到应用从 Nacos Server 中获取了最新的数据，age 变成了 18。

![refresh](https://cdn.nlark.com/lark/0/2018/png/54319/1536986336535-c0efdf6d-a5d3-4f33-8d26-fe3a36cdacf6.png)


## 原理


### Nacos Config 数据结构

Nacos Config 主要通过 dataId 和 group 来唯一确定一条配置，我们假定你已经了解此背景。如果不了解，请参考 [Nacos 文档](https://nacos.io/zh-cn/docs/concepts.html)。

Nacos Client 从 Nacos Server 端获取数据时，调用的是此接口 `ConfigService.getConfig(String dataId, String group, long timeoutMs)`。


### Spring Cloud 应用获取数据

#### dataID

在 Nacos Config Starter 中，dataId 的拼接格式如下

	${prefix} - ${spring.profiles.active} . ${file-extension}

* `prefix` 默认为 `spring.application.name` 的值，也可以通过配置项 `spring.cloud.nacos.config.prefix`来配置。

* `spring.profiles.active` 即为当前环境对应的 profile，详情可以参考 [Spring Boot文档](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html#boot-features-profiles)

	**注意，当 activeprofile 为空时，对应的连接符 `-` 也将不存在，dataId 的拼接格式变成 `${prefix}`.`${file-extension}`**

* `file-extension` 为配置内容的数据格式，可以通过配置项 `spring.cloud.nacos.config.file-extension`来配置。
目前只支持 `properties` 类型。

#### group
* `group` 默认为 `DEFAULT_GROUP`，可以通过 `spring.cloud.nacos.config.group` 配置。


### 自动注入
Nacos Config Starter 实现了 `org.springframework.cloud.bootstrap.config.PropertySourceLocator`接口，并将优先级设置成了最高。

在 Spring Cloud 应用启动阶段，会主动从 Nacos Server 端获取对应的数据，并将获取到的数据转换成 PropertySource 且注入到 Environment 的 PropertySources 属性中，所以使用 @Value 注解也能直接获取 Nacos Server 端配置的内容。

### 动态刷新

Nacos Config Starter 默认为所有获取数据成功的 Nacos 的配置项添加了监听功能，在监听到服务端配置发生变化时会实时触发 `org.springframework.cloud.context.refresh.ContextRefresher` 的 refresh 方法 。
		
如果需要对 Bean 进行动态刷新，请参照 Spring 和 Spring Cloud 规范。推荐给类添加 `@RefreshScope` 或 `@ConfigurationProperties ` 注解，

更多详情请参考 [ContextRefresher Java Doc](http://static.javadoc.io/org.springframework.cloud/spring-cloud-context/2.0.0.RELEASE/org/springframework/cloud/context/refresh/ContextRefresher.html)。

	


## Endpoint 信息查看

Spring Boot 应用支持通过 Endpoint 来暴露相关信息，Nacos Config Starter 也支持这一点。

在使用之前需要在 maven 中添加 `spring-boot-starter-actuator`依赖，并在配置中允许 Endpoints 的访问。

* Spring Boot 1.x 中添加配置 management.security.enabled=false
* Spring Boot 2.x 中添加配置 management.endpoints.web.exposure.include=*

Spring Boot 1.x 可以通过访问 http://127.0.0.1:18084/nacos_config 来查看 Nacos Endpoint 的信息。

Spring Boot 2.x 可以通过访问 http://127.0.0.1:18084/actuator/nacos-config 来访问。

![actuator](https://cdn.nlark.com/lark/0/2018/png/54319/1536986344822-279e1edc-ebca-4201-8362-0ddeff240b85.png)

如上图所示，Sources 表示此客户端从哪些 Nacos Config 配置项中获取了信息，RefreshHistory 表示动态刷新的历史记录，最多保存20条，NacosConfigProperties 则为 Nacos Config Starter 本身的配置。
    	
## More

#### 更多配置项
配置项|key|默认值|说明
----|----|-----|-----
服务端地址|spring.cloud.nacos.config.server-addr||
DataId前缀|spring.cloud.nacos.config.prefix||spring.application.name
Group|spring.cloud.nacos.config.group|DEFAULT_GROUP|
dataID后缀及内容文件格式|spring.cloud.nacos.config.file-extension|properties|dataId的后缀，同时也是配置内容的文件格式，目前只支持 properties
配置内容的编码方式|spring.cloud.nacos.config.encode|UTF-8|配置的编码
获取配置的超时时间|spring.cloud.nacos.config.timeout|3000|单位为 ms
配置的命名空间|spring.cloud.nacos.config.namespace||常用场景之一是不同环境的配置的区分隔离，例如开发测试环境和生产环境的资源隔离等。
AccessKey|spring.cloud.nacos.config.access-key||
SecretKey|spring.cloud.nacos.config.secret-key||
相对路径|spring.cloud.nacos.config.context-path||服务端 API 的相对路径
接入点|spring.cloud.nacos.config.endpoint|UTF-8|地域的某个服务的入口域名，通过此域名可以动态地拿到服务端地址
是否开启监听和自动刷新|spring.cloud.nacos.config.refresh.enabled|true|



#### 更多介绍
Nacos为用户提供包括动态服务发现，配置管理，服务管理等服务基础设施，帮助用户更灵活，更轻松地构建，交付和管理他们的微服务平台，基于Nacos, 用户可以更快速的构建以“服务”为中心的现代云原生应用。Nacos可以和Spring Cloud、Kubernetes/CNCF、Dubbo 等微服务生态无缝融合，为用户提供更卓越的体验。更多 Nacos 相关的信息，请参考 [Nacos 项目](https://github.com/alibaba/Nacos)。

如果您对 Spring Cloud Nacos Config Starter 有任何建议或想法，欢迎在 issue 中或者通过其他社区渠道向我们提出。

