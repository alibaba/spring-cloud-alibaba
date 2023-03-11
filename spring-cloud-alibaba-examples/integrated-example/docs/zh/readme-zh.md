# Integrated Example

## 项目说明

本项目为 Spring Cloud Alibaba （后文简称为SCA）容器化部署最佳实践的 Demo 演示项目，是整合了 SCA 相关组件( Nacos, Sentinel, Seata, RocketMQ)的 Example 示例项目。

主要使用的组件及及其使用特性如下：

- Spring Cloud Gateway 网关
- Nacos 配置中心和服务注册中心
- Sentinel 熔断限流
- Seata 分布式事务
- RocketMQ 消息队列削峰填谷
- Docker 微服务容器化部署
- Kubernetes Helm Chart 

![整体概览](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220816004541921.png)

## 应用场景说明

在本 demo 示例中，SCA社区提供了两种业务场景。

1)用户下单购买货物的场景，下单后：

- 先请求库存模块，扣减库存

- 扣减账户余额

- 生成订单信息返回响应

2)用户为商品进行点赞(模拟MQ的生产者消费者应用场景)返回商品点赞后的详细信息（点赞数等）。

### 组件详细说明

1)其中，用户下单购买货物的场景主要使用 Seata 来进行分布式事务的能力体现。

2)用户为商品进行点赞的场景，模拟大流量环境下通过 Sentinel 进行限流或是 RocketMQ 进行削峰填谷。在此场景下，SCA社区提供了两种应对大流量的处理方式：

- Sentinel 在网关侧绑定指定网关路由进行服务的熔断降级。
- RocketMQ 进行流量削峰填谷，在大流量请求下，生产者向 RocketMQ 发送消息，而消费者则通过可配置的消费速率进行拉取消费，减少大流量直接请求数据库增加点赞请求的压力。

#### SpringCloud Gateway

微服务模块的网关。

Spring Cloud GateWay 整合 Nacos,实现动态路由配置。

通过监听 Nacos 配置的改变，实现服务网关路由配置动态刷新，每次路由信息变更，无需修改配置文件而后重启服务。

#### Nacos

各个微服务的配置中心，服务注册中心。

- 配置中心
  - 共享配置：MySQL 数据源相关信息配置。

- 注册中心
  - 所有的微服务模块都注册到 Nacos 中进行服务注册与发现。
  - 整合 SpringCloud Gateway 网关。

#### Seata

基于 Seata 的 AT 模式，用于库存模块，账户模块，订单模块的分布式事务处理。

只要库存不足/账户余额不足，回滚事务。

#### Sentinel

用于点赞场景的服务熔断限流。

整合 Nacos 配置中心与 Spring Cloud Gateway，实现指定路由规则熔断限流规则动态配置。

#### RocketMQ

用于进行点赞服务流量的削峰填谷。

通过将大流量的点赞请求从生产者发送到mq，消费者模块从mq中拉取进行一定频率的消费，不是简单的直接服务熔断限流降级，实现 RocketMQ 针对大流量的削峰填谷能力。

## 版本说明

本项目提供了[本地部署运行版本](local-deployment-zh.md)、[docker-compose版本](docker-compose-deployment-zh.md)以及[Kubernetes Helm-Chart 版本](kubernetes-deployment-zh.md)。

- 如果想要了解具体如何配置各项组件以及完整环境搭建，推荐学习[本地部署运行版本](local-deployment-zh.md)。

- 如果只想运行示例代码，避免繁琐的本地环境搭建过程，又不想使用k8s集群。您可以尝试使用[docker-compose版本](docker-compose-deployment-zh.md)。

- 如果想要在K8S集群上快速体验组件效果，跳过各个组件环境部署等过程，请查看[Kubernetes Helm-Chart 版本](kubernetes-deployment-zh.md)。