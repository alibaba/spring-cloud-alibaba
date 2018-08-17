# Sentinel Dubbo Provider Example

## 项目说明

本项目演示如何使用 Sentinel starter 完成 Dubbo 应用的限流管理。

[Sentinel](https://github.com/alibaba/Sentinel) 是阿里巴巴开源的分布式系统的流量防卫组件，Sentinel 把流量作为切入点，从流量控制，熔断降级，系统负载保护等多个维度保护服务的稳定性。

[Dubbo](http://dubbo.apache.org/)是一款高性能Java RPC框架，有对应的[SpringBoot工程](https://github.com/apache/incubator-dubbo-spring-boot-project)。

本项目需要配合`sentinel-dubbo-consumer-example`模块一起完成演示。

本项目专注于Sentinel与Dubbo的整合，关于Sentinel的更多特性可以查看[sentinel-example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/tree/master/spring-cloud-alibaba-examples/sentinel-example)。

## 示例

### 如何接入
在启动示例进行演示之前，我们先了解一下 Dubbo 如何接入 Sentinel。
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 pom.xml 文件，引入 Sentinel starter 和 Dubbo starter。

	    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-sentinel</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.alibaba.boot</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
        </dependency>
		  
2. 配置限流规则
	
	Sentinel提供了[sentinel-dubbo-adapter](https://github.com/alibaba/Sentinel/tree/master/sentinel-adapter/sentinel-dubbo-adapter)模块用来支持Dubbo服务调用的限流降级。sentinel-starter默认也集成了该功能。
	
	sentinel-dubbo-adapter内部的Dubbo Filter会根据资源名进行限流降级处理。只需要配置规则即可：

        FlowRule flowRule = new FlowRule();
        flowRule.setResource("dubboResource");
        flowRule.setCount(10);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
  

### 服务定义及发布

在application.properties文件中定义dubbo相关的配置，比如协议，注册中心：

    spring.application.name = dubbo-provider-demo
    
    foo.service.version = 1.0.0
    
    dubbo.scan.basePackages = org.springframework.cloud.alibaba.cloud.examples
    
    dubbo.application.id = dubbo-provider-demo
    dubbo.application.name = dubbo-provider-demo
    
    dubbo.protocol.id = dubbo
    dubbo.protocol.name = dubbo
    dubbo.protocol.port = 12345
    dubbo.protocol.status = server
    
    dubbo.registry.id = my-registry
    dubbo.registry.address = N/A


`sentinel-dubbo-api`模块中定义了FooService服务，内容如下：

    package org.springframework.cloud.alibaba.cloud.examples.FooService;
    public interface FooService {
        String hello(String name);
    }

定义具体的服务：

    @Service(
            version = "${foo.service.version}",
            application = "${dubbo.application.id}",
            protocol = "${dubbo.protocol.id}",
            registry = "${dubbo.registry.id}"
    )
    public class FooServiceImpl implements FooService {
    
        @Override
        public String hello(String name) {
            return "hello, " + name;
        }
    }


### 应用启动 


支持 IDE 直接启动和编译打包后启动。

1. IDE直接启动：找到主类 `SentinelDubboProviderApp`，执行 main 方法启动应用。
2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar sentinel-dubbo-provider-example.jar`启动应用。


