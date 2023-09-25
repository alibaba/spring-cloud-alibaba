# Spring Cloud Alibaba灰度发布能力扩展所支持的客户端类型

## 项目信息

|  item  | content |
|:------:|:-------:|
|  中选学生  |  姬世文  |
| 项目社区导师 |  阮胜   |
|  项目难度  |  基础   |
|   pr   |   https://github.com/alibaba/spring-cloud-alibaba/pull/3449    |

> 题目要求：
>
> 1. 对 Spring Cloud Alibaba 服务实现蓝绿部署，通过传递路由信息，达到流量隔离的目的 实现区域亲和性功能，优先选取给定的可用区实现亲和性，不满足则路由全体区域， 支持粗粒度和细粒度双重亲和。
> 2. 要求支持主流的所有客户端类型，比如 RestTemplate、WebClient 以及常见的网关比如 Spring Cloud Gateway 和 Zuul

## 项目完成度

### 网关适配

通过 starter-adapter 适配的方式适配了 spring-cloud-gateway 网关和 zuul 网关。具体代码可见 `spring-cloud-starter-adlibaba-governance-routing-gateway-adapter` 和 `spring-cloud-starter-alibaba-governance-routing-zuul-adapter`。

### 请求客户端适配

通过请求拦截器的方式适配 Feign 和 RestTemplate 以及 WebClient 请求客户端，具体代码可见 `spring-cloud-starter-alibba-goveranance-routing-starter/com/alibaba/cloud/routing/aop` 包下。

## 项目结构

在此项目中，项目分为 starter 项目和 example 功能测试项目。

> Note: 如果文中没有特殊说明，出现的子级目录都在 `spring-cloud-alibaba-examples/governance-example/label-routing-example` 目录下！

### Example 测试项目

```shell
├─gateway-consumer-example                # 网关消费者 example
│  ├─gateway-consumer-common              # 通用组件模块，包含一些常量定义
│  ├─gateway-consumer-example
│  ├─resources                            # postman 请求脚本
│  └─zuul-consumer-example
├─routing-service-provider-example        # 服务提供者 example
└─web-client-consumer-example             # 请求客户端消费 example
    ├─openfeign-consumer-example          # feign 消费者客户端
    ├─resources                           # postman 请求脚本
    ├─restTemplate-consumer-example       # rest 消费者客户端
    ├─web-client-consumer-common          # 客户端请求常量定义
    └─webClient-consumer-example          # reactive 消费者客户端
```

### spring-cloud-starter-alibaba-governance-routing 项目结构

```shell
├─spring-cloud-starter-aibaba-governance-routing                        # routing 父工程
│  ├─spring-cloud-starter-alibaba-governance-routing-gateway-adapter    # gateway 适配器工程
│  ├─spring-cloud-starter-alibaba-governance-routing-service-adapter    # routing service 适配器工程
│  ├─spring-cloud-starter-alibaba-governance-routing-starter            # routing starter 工程
│  ├─spring-cloud-starter-alibaba-governance-routing-zuul-adapter       # zuul 适配器工程
```

## 项目测试

> Note: 本功能模块编写了 Docker Compose 的启动方式，具体可见 `docker-compose-example-quickstart/label-routing-quickstart` 目录。

因为项目是基于已有的 routing 模块进行功能开发，所以在测试功能时整合了已有的流量标签功能完成区域亲和性路由功能，完善 spring cloud alibaba 流量治理功能模块。

### 导入 postman 启动脚本

1. 进入 `web-client-consumer-example/resources`文件夹将**客户端**功能请求测试脚本导入到 postman ；
2. gateway-consumer-example/resources` 文件夹将**网关**功能请求测试脚本导入到 postman；

完成以上步骤，方便后续的功能测试。

### 应用启动

#### 启动 nacos-server

本项目的注册中心为 nacos，在进行功能测试启动 example 应用之前需要先启动 nacos-server，确保服务能够被发现和调用！

### 启动服务提供者

进入 `routing-service-provider-example` 文件夹，启动四个服务实例，分别为 A1ProviderApplication 到 A4ProviderApplication 将其注入到 Nacos 注册中心中。

### 客户端消费者测试（以 Feign 客户端为例，其他客户端类似。）

1. 进入 `/web-client-consumer-example` 文件夹启动三个模块的启动类，分别为 ConsumerFeignApplication，ConsumerReactiveApplication 和 ConsumerRestApplication。
2. 逐个点击 v1 和 v2 版本请求，查看四个服务实例是否可以被正常消费 **（不设置任何路由规则情况下）**。

#### 功能测试

##### 当区域亲和性路由存在时

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：不满足路由规则，路由到v1版本中，且区域亲和性路由规则为 region=dev，zone=zone1，预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   # v2：观察 service-provider 的元数据发现，没有 region=dev，zone=zone1，version=v2 的服务实例，因此区域亲和性路由会退化为标签路由效果，预期为以下结果：
   # 	 Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   #	 Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   
   # 测试发现和预期结果匹配！
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果：
   # v1：不满足路由规则，路由到 v1 版本中，且区域亲和性路由规则为 region=dev，zone=zone1，实例打印返回如下结果：
   #	 Route in 172.18.0.3:19093, region: dev, zone: zone1, version: v1
   # v2：因为设置了区域亲和性路由规则，所以即使 v1 和 v2 版本各自 50% 的权重，但是还是会根据区域亲和性路由规则选取服务实例, 预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
   # 测试发现和预期结果匹配！
   ```

##### 当区域亲和性路由不存在时

进入 `web-client-consumer-example/openfeign-consumer-example/src/main/resources/application.yml` 文件中，注释以下配置，再次启动 ConsumerFeignApplication；

```yml
    # label routing configuration
    governance:
      routing:
        # region: dev
        # zone: zone1
      # rule: RandomRule
```

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：因为没有区域亲和性路由限制，所以会在 v1 实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
   #	Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   
   # v2：因为没有区域亲和性路由限制，所以会在 v2 实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
   
   # 测试发现，符合预期结果
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果
   # v1：因为没有区域亲和性路由限制，路由结果按照标签路由选择服务实例，所以会在两个 v1 实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   # 	Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
     
   # v2：v1 和 v2 权重各占 50，所以四种服务实例的调用结果都会出现
   #	Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   #	Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
   #	Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
   
   # 测试发现，符合预期结果
   ```

### 网关消费者效果演示 （以 spring-cloud-gateway 网关为例。）

1. 进入 `gateway-consumer-example` 文件夹分别启动两个网关模块的启动类，分别为 ConsumerZuulApplication 和 ConsumerGatewayApplication。

2. 逐个点击 v1 和 v2 版本请求，查看四个服务实例是否可以被正常消费**（不设置任何路由规则情况下）**。

#### 功能测试

##### 当区域亲和性路由存在时

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：不满足路由规则，路由到v1版本中，且区域亲和性路由规则为 region=dev，zone=zone1，预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   # v2：观察 service-provider 的元数据发现，没有 region=dev，zone=zone1，version=v2 的服务实例，因此区域亲和性路由会退化为标签路由效果，预期为以下结果：
   # 	 Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   #	 Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   
   # 测试发现和预期结果匹配！
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果：
   # v1：不满足标签路由规则，路由到v1版本中，从两个 v1 版本实例中根据区域亲和性标签选择服务实例，实例打印返回如下结果：
   #	 Route in 172.18.0.3:19093, region: dev, zone: zone1, version: v1
   # v2：因为设置了区域亲和性路由规则，所以即使 v1 和 v2 版本各自 50% 的权重，但是还是会根据区域亲和性路由规则选取服务实例, 预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
   # 测试发现和预期结果匹配！
   ```

##### 当区域亲和性路由不存在时

进入 `web-client-consumer-example/routing-feign-consumer-example/src/main/resources/application.yml` 文件中，注释以下配置，再次启动 ConsumerFeignApplication；

```yml
    # label routing configuration
    governance:
      routing:
        # region: dev
        # zone: zone1
      # rule: RandomRule
```

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：因为没有区域亲和性路由限制，所以会在实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
   #	Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   
   # v2：因为没有区域亲和性路由限制，所以会在实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
   
   # 测试发现，符合预期结果
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果
   # v1：因为没有区域亲和性路由限制，路由结果按照标签路由选择服务实例，所以会在两个实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   # 	Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
     
   # v2：v1 和 v2 权重各占 50，所以四种服务实例的调用结果都会出现
   #	Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   #	Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
   #	Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
   
   # 测试发现，符合预期结果
   ```

## 项目总结

在本项目中，流量治理功能的流程是：从控制面拉取流量治理规则，通过 `spring-cloud-alibaba-routing` sdk 进行规则解析并应用流量规则，达到流量治理的目的，其中使用的注册中心组件是 `nacos`，负载均衡组件为 `ribbon`。经过三个月时间的调研和开发，已经完成了题目要求的功能并编写了测试用的 example 应用和测试说明文档。

在本项目中，还存在许多可以进一步扩展的地方：

- 在区域亲和性路由标签中：选择了 region 和 zone 为路由标签。标签可以进一步扩展加入 version，env 和以 git 区分的应用版本等标签；
- 在注册中心方面：除了现有的 `nacos` 注册中心，还可以适配 `eureka` 和 `zookeeper` 等注册中心，为用户提供更多选择；
- 在负载均衡方面：因为 netflix 不在维护 `ribbon` 组件的原因，ribbon 社区逐渐落寞。Spring Cloud 社区维护了 `spring-cloud-loadbalancer`，后续还可以支持其作为负载均衡工具。

通过这次在开源之夏的项目中，我认识了许多优秀的同学和导师，并从中受益良多。感谢[阮胜](https://github.com/ruansheng8)老师在做项目时的悉心指导。让我对服务治理中的的区域亲和性路由和流量治理以及负载均衡工具 `ribbon` 有了更加清晰的认知。感谢 `spring-cloud-alibaba` 社区[铖朴](https://github.com/steverao) 老师在项目上的指导，让我对 `spring-cloud-alibaba` 微服务开发框架的了解更加深入和透彻。感谢[肖倩文](https://github.com/why-ohh)同学在一些服务治理和 `ribbon` 的观点想法上和我进行的讨论，在讨论过程中，让我意识到了自己的不足，同时解答了我很多问题。
