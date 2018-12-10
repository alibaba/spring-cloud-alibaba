# Spring Cloud Alibaba

Spring Cloud Alibaba 致力于提供微服务开发的一站式解决方案。此项目包含开发分布式应用微服务的必需组件，方便开发者通过 Spring Cloud 编程模型轻松使用这些组件来开发分布式应用服务。

依托 Spring Cloud Alibaba，您只需要添加一些注解和少量配置，就可以将 Spring Cloud 应用接入阿里微服务解决方案，通过阿里中间件来迅速搭建分布式应用系统。

参考文档 请查看 [WIKI](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki)

## 主要功能

* **服务限流降级**：默认支持 Servlet、Feign、RestTemplate、Dubbo 和 RocketMQ 限流降级功能的接入，可以在运行时通过控制台实时修改限流降级规则，还支持查看限流降级 Metrics 监控。
* **服务注册与发现**：适配 Spring Cloud 服务注册与发现标准，默认集成了 Ribbon 的支持。
* **分布式配置管理**：支持分布式系统中的外部化配置，配置更改时自动刷新。
* **消息驱动能力**：基于 Spring Cloud Stream 为微服务应用构建消息驱动能力。
* **阿里云对象存储**：阿里云提供的海量、安全、低成本、高可靠的云存储服务。支持在任何应用、任何时间、任何地点存储和访问任意类型的数据。


更多功能请参考 [Roadmap](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/Roadmap-zh.md)。

## 组件

**[Sentinel](https://github.com/alibaba/Sentinel)**：把流量作为切入点，从流量控制、熔断降级、系统负载保护等多个维度保护服务的稳定性。

**[Nacos](https://github.com/alibaba/Nacos)**：一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。

**[RocketMQ](https://rocketmq.apache.org/)**：一款开源的分布式消息系统，基于高可用分布式集群技术，提供低延时的、高可靠的消息发布与订阅服务。

**[AliCloud ACM](https://www.aliyun.com/product/acm)**：一款在分布式架构环境中对应用配置进行集中管理和推送的应用配置中心产品。

**[AliCloud OSS](https://www.aliyun.com/product/oss)**: 阿里云对象存储服务（Object Storage Service，简称 OSS），是阿里云提供的海量、安全、低成本、高可靠的云存储服务。您可以在任何应用、任何时间、任何地点存储和访问任意类型的数据。

更多组件请参考 [Roadmap](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/Roadmap-zh.md)。

## 如何构建

* master 分支对应的是 Spring Cloud Finchley，最低支持 JDK 1.8。
* 1.x 分支对应的是 Spring Cloud Edgware，最低支持 JDK 1.7。

Spring Cloud 使用 Maven 来构建，最快的使用方式是将本项目clone到本地，然后执行以下命令：

	./mvnw install

执行完毕后，项目将被安装到本地 Maven 仓库。

## 如何使用

### 如何引入依赖
项目已经发布了第一个版本，版本 0.2.0.RELEASE 对应的是 Spring Cloud Finchley 版本，版本 0.1.0.RELEASE 对应的是 Spring Cloud Edgware 版本。

如果需要使用已发布的版本，在 `dependencyManagement` 中添加如下配置。

	<dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>0.2.0.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

然后再 `dependencies` 中添加自己所需使用的依赖即可使用。

如果您想体验最新的 BUILD-SNAPSHOT 的新功能，则可以将版本换成最新的版本，但是需要在 pom.xml 中配置 Spring BUILDSNAPSHOT 仓库，**注意: SNAPSHOT 版本随时可能更新**

	<repositories>
        <repository>
            <id>spring-snapshot</id>
            <name>Spring Snapshot Repository</name>
            <url>https://repo.spring.io/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

## 演示 Demo

为了演示如何使用，Spring Cloud Alibaba 项目包含了一个子模块`spring-cloud-alibaba-examples`。此模块中提供了演示用的 example ，您可以阅读对应的 example 工程下的 readme 文档，根据里面的步骤来体验。

Example 列表：

[Sentinel Example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/tree/master/spring-cloud-alibaba-examples/sentinel-example/sentinel-core-example/readme-zh.md)

[Nacos Config Example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/nacos-example/nacos-config-example/readme-zh.md)

[Nacos Discovery Example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/nacos-example/nacos-discovery-example/readme-zh.md)

[RocketMQ Example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/rocketmq-example/readme-zh.md)

[AliCloud OSS Example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/oss-example/readme-zh.md)

[AliCloud ANS Example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/ans-example/ans-provider-example/readme-zh.md)

[AliCloud ACM Example](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/acm-example/acm-local-example/readme-zh.md)

## 版本管理规范
项目的版本号格式为 x.x.x 的形式，其中 x 的数值类型为数字，从0开始取值，且不限于 0~9 这个范围。项目处于孵化器阶段时，第一位版本号固定使用0，即版本号为 0.x.x 的格式。

由于 Spring Boot 1 和 Spring Boot 2 在 Actuator 模块的接口和注解有很大的变更，且 spring-cloud-commons 从 1.x.x 版本升级到 2.0.0 版本也有较大的变更，因此我们使用了两个不同分支来分别支持 Spring Boot 1 和 Spring Boot 2:
* 0.1.x 版本适用于 Spring Boot 1
* 0.2.x 版本适用于 Spring Boot 2

项目孵化阶段，项目版本升级机制如下：
* 功能改动的升级会增加第三位版本号的数值，例如 0.1.0 的下一个版本为0.1.1。



## 社区交流

### 邮件列表

spring-cloud-alibaba@googlegroups.com，欢迎通过此邮件列表讨论与 spring-cloud-alibaba 相关的一切。

### 钉钉群

![DingQR](https://cdn.nlark.com/lark/0/2018/png/64647/1535108150178-409a1689-437f-495b-8dcb-b667ccb32f85.png) 
