# Roadmap

[Spring Cloud Alibaba](https://github.com/alibaba/spring-cloud-alibaba) 致力于提供微服务开发的一站式解决方案。此项目包含开发分布式应用服务的必需组件，方便开发者通过 Spring Cloud 编程模型轻松使用这些组件来开发分布式应用服务。

此项目包含的组件内容，主要选取自阿里巴巴开源中间件，但也不限定于这些产品。

如果您对 Roadmap 有任何建议或想法，欢迎在 issues 中或者通过其他社区渠道向我们提出，一起讨论。

- Github Issue：https://github.com/alibaba/spring-cloud-alibaba/issues
- 钉钉交流群：“群8 Spring Cloud Alibaba交流群”群的钉钉群号： 33610001098


## 已包含的组件

**Sentinel**

阿里巴巴开源产品，把流量作为切入点，从流量控制、熔断降级、系统负载保护等多个维度保护服务的稳定性。

**Nacos**

阿里巴巴开源产品，一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。

**RocketMQ**

Apache RocketMQ™ 一款开源的分布式消息系统，基于高可用分布式集群技术，提供低延时的、高可靠的消息发布与订阅服务。

**Seata**

阿里巴巴开源产品（现捐赠给 Apache 基金会），一款开源的分布式事务解决方案，致力于在微服务架构下提供高性能和简单易用的分布式事务服务。

## 未来发展方向

## Spring Cloud Admin 服务治理（可观测方向）

Spring Cloud Admin 定位为一款可视化的微服务管控平台，通过它能够查看整个 Spring Cloud 微服务状态（包括服务数、实例数、应用数等）。同时 Spring Cloud Admin 还应与主流的 [Apache SkyWalking](https://skywalking.apache.org/)，[OpenTelemetry](https://opentelemetry.io/) 等可观测性系统集成，提供对集群状态的指标查询与监控能力。

## Spring Cloud Alibaba AI

随着 LLM 的爆火，各种 AI 应用开发框架应运而生。其中包括 LangChain，LangChain4J，Spring AI 等项目，为 AI 应用开发提供了一系列的解决方案。

本项目是基于 Spring AI 提供对阿里通义系列大模型的完整支持，包括对话，文生图，文生语音，语音转录等功能。旨在为开发微服务 AI 应用提供便利，屏蔽底层复杂性，使得 AI 可以快速接入 Spring Cloud 微服务体系。

## Proxyless Mesh

Spring Cloud Alibaba 也积极在 Proxyless 方向上探索，目前已经完成了 Routing，Xds-adapter 等功能。未来会推出更多云原生场景下的 Proxyless 功能特性。

## RPC 方向上的探索

Spring Cloud Alibaba RPC 组件主要依赖于 OpenFeign，RestTemplate 等。社区计划通过加入 GRPC，Dubbo 的 rpc 解决方案，进一步增强社区的 RPC 组件能力。
