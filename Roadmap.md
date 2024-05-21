# Roadmap

See the [中文文档](https://github.com/alibaba/spring-cloud-alibaba/blob/2023.x/Roadmap-zh.md) for Chinese Roadmap.


Spring Cloud Alibaba provides a one-stop solution for microservices development. It contains all the components required to develop distributed applications, making it easy for you to develop your applications through the Spring Cloud programming model.

This project contains components from both open-source and commercialized Alibaba middleware products，but are not limited to them.

If you have any suggestions on our roadmap, feel free to submit issues or contact us via the other channels.

- Github Issue：https://github.com/alibaba/spring-cloud-alibaba/issues
- DingTalk communication group: "Group 8 Spring Cloud Alibaba communication group" DingTalk group number: 33610001098


## Components

**Sentinel**

An open-source project of Alibaba, Sentinel takes "flow" as breakthrough point, and provides solutions in areas such as flow control, concurrency, circuit breaking, and load protection to protect service stability.

**Nacos**

An opensource project of Alibaba, an easy-to-use dynamic service discovery, configuration and service management platform for building cloud native applications.

**RocketMQ**

Apache RocketMQ™ is an open source distributed messaging system based on highly available distributed cluster technology, providing low latency, highly reliable message publishing and subscription services.

**Seata**

An opensource project of Alibaba(now donated to the Apache Foundation), a distributed transaction solution dedicated to providing high-performance and easy-to-use distributed transaction services under a microservice architecture.


## Future Development Direction

## Spring Cloud Admin Service Governance (Observable Direction)

Spring Cloud Admin is positioned as a visual microservice management and control platform, through which you can view the status of the entire Spring Cloud microservice (including the number of services, the number of instances, the number of applications, etc.). At the same time, Spring Cloud Admin should also be integrated with mainstream observability systems such as [Apache SkyWalking](https://skywalking.apache.org/) and [OpenTelemetry](https://opentelemetry.io/) to provide indicator query and monitoring capabilities for cluster status.

## Spring Cloud Alibaba AI

With the explosion of LLM, various AI application development frameworks have emerged as the times require. These include LangChain, LangChain4J, Spring AI and other projects, providing a series of solutions for AI application development.

This project is based on Spring AI to provide complete support for Ali Tongyi series large models, providing for chat, text-to-image, audiotranscription and other functions. It aims to facilitate the development of microservice AI applications, shield the underlying complexity, and enable AI to quickly access the Spring Cloud microservice system.

## Proxyless Mesh

Spring Cloud Alibaba is also actively exploring in the direction of Proxyless, and has completed functions such as Routing and Xds-adapter. In the future, more Proxyless features in cloud-native scenarios will be launched.

## Exploration in the direction of RPC

Spring Cloud Alibaba RPC components mainly rely on OpenFeign, RestTemplate, etc. The community plans to further enhance the community's RPC component capabilities by joining GRPC, Dubbo's RPC solution.
