#  Spring Cloud Gateway、 Nacos Discovery Example

## 项目说明

本项目演示如何使用 Nacos Discovery Starter 、 Spring Cloud Gateway Starter 完成 Spring Cloud 服务路由。

[Nacos](https://github.com/alibaba/Nacos) 是阿里巴巴开源的一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。  
[Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) 是spring cloud 官方开源的一个在SpringMVC 上可以构建API网关的库。

## 示例

### 如何接入
在启动示例进行演示之前，我们先了解一下 Spring Cloud 应用如何接入 Spring Cloud 如何接入Nacos Discovery、Spring Cloud Gateway。
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 pom.xml 文件，引入 Nacos Discovery Starter、Spring Cloud Gateway Starter。

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
	
2. 在应用的 /src/main/resources/application.properties 配置文件中配置 Nacos Server 地址
	
		spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

3. 在应用的 /src/main/resources/application.properties 配置文件中配置 Spring Cloud Gateway 路由

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
```

### 启动 Nacos Server

1. 首先需要获取 Nacos Server，支持直接下载和源码构建两种方式。

	1. 直接下载：[Nacos Server 下载页](https://github.com/alibaba/nacos/releases) 
	2. 源码构建：进入 Nacos [Github 项目页面](https://github.com/alibaba/nacos)，将代码 git clone 到本地自行编译打包，[参考此文档](https://nacos.io/zh-cn/docs/quick-start.html)。**推荐使用源码构建方式以获取最新版本**

2. 启动 Server，进入解压后文件夹或编译打包好的文件夹，找到如下相对文件夹 nacos/bin，并对照操作系统实际情况之下如下命令。
	
	1. Linux/Unix/Mac 操作系统，执行命令 `sh startup.sh -m standalone`
	1. Windows 操作系统，执行命令 `cmd startup.cmd`

### Spring Cloud Gateway应用启动
启动应用，支持 IDE 直接启动和编译打包后启动。

1. IDE直接启动：找到 nacos-gateway-discovery-example 项目的主类 `GatewayApplication`，执行 main 方法启动应用。
2. 打包编译后启动：在 nacos-gateway-discovery-example 项目中执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar nacos-gateway-discovery-example.jar`启动应用。

### 服务提供方应用启动
启动应用，支持 IDE 直接启动和编译打包后启动。
1. IDE直接启动：找到 nacos-gateway-provider-example 项目的主类 `ProviderApplication`，执行 main 方法启动应用。
2. 打包编译后启动：在 nacos-gateway-provider-example 项目中执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar nacos-gateway-provider-example.jar`启动应用。

### 验证
1. 
```bash
 curl 'http://127.0.0.1:18085/nacos/echo/hello-world' 
 
 hello Nacos Discovery hello-world⏎
```
1. 
```bash
 curl 'http://127.0.0.1:18085/nacos/divide?a=6&b=2' 

 3⏎              
```
#### 更多介绍
Nacos为用户提供包括动态服务发现，配置管理，服务管理等服务基础设施，帮助用户更灵活，更轻松地构建，交付和管理他们的微服务平台，基于Nacos, 用户可以更快速的构建以“服务”为中心的现代云原生应用。Nacos可以和Spring Cloud、Kubernetes/CNCF、Dubbo 等微服务生态无缝融合，为用户提供更卓越的体验。更多 Nacos 相关的信息，请参考 [Nacos 项目](https://github.com/alibaba/Nacos)。

如果您对 Spring Cloud Nacos Discovery 有任何建议或想法，欢迎在 issue 中或者通过其他社区渠道向我们提出。

