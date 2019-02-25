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
	
2. 在应用的 /src/main/resources/bootstrap.yaml 配置文件中配置 Nacos Server 地址
	
		  cloud:
            nacos:
              discovery:
                server-addr: 127.0.0.1:8848
              config:
                server-addr: 127.0.0.1:8848

2. 在应用的 /src/main/resources/application.yaml 配置文件中配置应用端口，dubbo协议端口和rest协议端口

          dubbo:
            scan:
              base-packages: org.springframework.cloud.alibaba.dubbo.examples
            protocols:
              dubbo:
                name: dubbo
                port: 12345
              rest:
                name: rest
                port: 9090
                server: netty
            registry:
              address: spring-cloud://nacos
          
          server:
            port: 8081
		  


### 针对服务提供者

1. 定义服务接口，如示例中RestService

        public interface RestService {
        
            String echo(String param);

        }


2. 对于服务接口的实现类，添加注解以将传统rest服务通过dubbo进行暴露

	    @com.alibaba.dubbo.config.annotation.Service(version = "1.0.0", protocol = {"dubbo", "rest"})
        @RestController
        public class StandardRestService implements RestService {
   
            @Override
            @GetMapping(value = "/echo")
            @Path("/echo")
            @GET
            public String echo(String param) {
        
                return param;
            }
               
        }

### 针对服务调用者

1. 定义服务接口，如示例中RestService
   
           public interface RestService {
           
               String echo(String param);
   
           }
    

		
2. 采用dubbo调用

           @Reference(version = "1.0.0")
           private RestService restService;
           
           restService.echo("hello")

3. 采用RestTemplate方式调用, 在原有RestTemplate类上添加@DubboTransported注解，即可在底层采用dubbo

           @Bean
           @LoadBalanced
           @DubboTransported
           public RestTemplate restTemplate() {
               return new RestTemplate();
           }
           
           restTemplate().getForEntity("http://spring-cloud-alibaba-dubbo/echo?param=hello",String.class);


4. 采用Feign方式调用,在原有Feign类上添加@DubboTransported注解，即可在底层采用dubbo

           @Autowired
           private DubboFeignRestService dubboFeignRestService;
           @FeignClient("spring-cloud-alibaba-dubbo")
           @DubboTransported
           public interface DubboFeignRestService {
           
               @GetMapping(value = "/echo")
               String echo(String param);
           }
            
           dubboFeignRestService.echo("hello")


### 验证

#### 查询服务
在浏览器输入此地址 `http://127.0.0.1:8848/nacos/v1/ns/instances?serviceName=service-provider`，并点击跳转，可以看到服务节点已经成功注册到 Nacos Server。

![查询服务](https://cdn.nlark.com/lark/0/2018/png/54319/1536986288092-5cf96af9-9a26-466b-85f6-39ad1d92dfdc.png)

#### 查看log



#### 更多介绍
Apache Dubbo (incubating) |ˈdʌbəʊ| 是一款高性能、轻量级的开源Java RPC框架，它提供了三大核心能力：面向接口的远程方法调用，智能容错和负载均衡，以及服务自动注册和发现。

如果您对 Spring Cloud Dubbo 有任何建议或想法，欢迎在 issue 中或者通过其他社区渠道向我们提出。

