# Dubbo Example

## 项目说明

本项目演示如何使用 Dubbo Starter 完成 Spring Cloud 应用的rpc调用。包括原生dubbo服务以及dubbo对于RestTemplate和Feign的支持。

[dubbo](https://github.com/apache/incubator-dubbo) Apache Dubbo™ (incubating) 是一款高性能 Java RPC 框架。

## 示例

### 如何接入
在启动示例进行演示之前，我们先了解一下 Spring Cloud 应用如何使用Dubbo。
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 pom.xml 文件，引入 Dubbo Starter。

	     <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-starter-dubbo</artifactId>
         </dependency>
	

		  
2. 定义服务接口,在服务提供方和消费方之间共享（spring-cloud-dubbo-sample-api）

          public interface RestService {
          ...
          }


### 针对服务提供者（spring-cloud-dubbo-provider-sample）

1. 在应用的 /src/main/resources/bootstrap.yaml 配置文件中配置注册中心地址

   当前支持nacos,zookeeper,consul,eureka等注册中心，示例中可以通过选择不同profiles来使用不同的注册中心
	
	     ---
         spring:
           profiles: nacos
         
           cloud:
             nacos:
               discovery:
                 enabled: true
                 register-enabled: true
                 server-addr: 127.0.0.1:8848
         
         ---
         spring:
           profiles: eureka
         
         eureka:
           client:
             enabled: true
             service-url:
               defaultZone: http://127.0.0.1:9090/eureka/
         
         ---
         spring:
           profiles: zookeeper
           cloud:
             zookeeper:
               enabled: true
               connect-string: 127.0.0.1:2181
         
         ---
         spring:
           profiles: consul
         
           cloud:
             consul:
               enabled: true
               host: 127.0.0.1
               port: 8500   

2. 在应用的 /src/main/resources/application.yaml 配置文件中配置应用端口，dubbo协议端口

          dubbo:
            scan:
              base-packages: org.springframework.cloud.alibaba.dubbo.service
            protocols:
              dubbo:
                name: dubbo
                port: 12345
            registry:
              address: spring-cloud://nacos         
          
          server:
            port: 8080

3. 对于服务接口的实现类，添加注解以将传统rest服务通过dubbo进行暴露

	    @com.alibaba.dubbo.config.annotation.Service(version = "1.0.0", protocol = {"dubbo"})
        @RestController
        public class StandardRestService implements RestService 

### 针对服务消费者（spring-cloud-dubbo-consumer-sample）

1. 在应用的 /src/main/resources/bootstrap.yaml 配置文件中配置注册中心地址

   当前支持nacos,zookeeper,consul,eureka等注册中心，示例中可以通过选择不同profiles来使用不同的注册中心
	
	     ---
         spring:
           profiles: nacos
         
           cloud:
             nacos:
               discovery:
                 enabled: true
                 register-enabled: true
                 server-addr: 127.0.0.1:8848
         
         ---
         spring:
           profiles: eureka
         
         eureka:
           client:
             enabled: true
             service-url:
               defaultZone: http://127.0.0.1:9090/eureka/
         
         ---
         spring:
           profiles: zookeeper
           cloud:
             zookeeper:
               enabled: true
               connect-string: 127.0.0.1:2181
         
         ---
         spring:
           profiles: consul
         
           cloud:
             consul:
               enabled: true
               host: 127.0.0.1
               port: 8500   

2. 在应用的 /src/main/resources/application.yaml 配置文件中配置应用端口
         
          
          server:
            port: 7070
           
		
3. 采用dubbo调用

           @Reference(version = "1.0.0")
           private RestService restService;
           

4. 采用RestTemplate方式调用, 在原有RestTemplate类上添加@DubboTransported注解，即可在底层采用dubbo

           @Bean
           @LoadBalanced
           @DubboTransported
           public RestTemplate restTemplate() {
               return new RestTemplate();
           }
           

5. 采用Feign方式调用,在原有Feign类上添加@DubboTransported注解，即可在底层采用dubbo

           @Autowired
           private DubboFeignRestService dubboFeignRestService;
           @FeignClient("${provider.application.name}")
           @DubboTransported
           public interface DubboFeignRestService
            
6. 示例中分别演示来对请求路径，带有请求头，带有单个URL参数，带有多个URL参数，带有请求体等方式进行调用

### 验证
启动本地nacos,运行DubboSpringCloudProviderBootstrap,DubboSpringCloudConsumerBootstrap
#### 查询服务
可以在nacos控制台服务列表中查找RestService，并在服务详情中查看对应dubbo相关配置信息


#### 查看log
可以通过在日志中搜索dubbo-test查看对应输出
          



#### 更多介绍
Apache Dubbo (incubating) |ˈdʌbəʊ| 是一款高性能、轻量级的开源Java RPC框架，它提供了三大核心能力：面向接口的远程方法调用，智能容错和负载均衡，以及服务自动注册和发现。

如果您对 Spring Cloud Dubbo 有任何建议或想法，欢迎在 issue 中或者通过其他社区渠道向我们提出。

