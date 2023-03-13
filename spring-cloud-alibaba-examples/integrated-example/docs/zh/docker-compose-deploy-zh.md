# Spring Cloud Alibaba容器化部署最佳实践 | Docker-Compose 版本

## 准备工作

> Note: 使用Docker-Compose方式体验Demo时，请确保本地机器内存资源 >= 24G！

如果您还没有安装Docker和Docker-Compose，请按照官方文档来构建运行环境：

- Docker：https://docs.docker.com/desktop/install/linux-install/
- Docker-Compose：https://docs.docker.com/compose/install/

### Hosts 配置

为确保代码能够正常启动，请先配置本地主机映射，将以下映射添加到配置文件中。

```shell
# for integrated-example
127.0.0.1 integrated-mysql
127.0.0.1 nacos-server
127.0.0.1 seata-server
127.0.0.1 rocketmq
127.0.0.1 gateway-service
127.0.0.1 integrated-frontend
```

### 准备jar包

进入`spring-cloud-alibaba-examples`目录下，执行`mvn package`命令编译项目生成jar包，为后续Docker构建服务镜像做准备。

## 快速启动

### 组件启动

进入`spring-cloud-alibaba-examples/integrated-example`目录下，在终端中执行以下命令`docker-compose -f ./docker-compose/docker-compose-env.yml up -d`来快速部署运行example所需组件。

### 添加配置

docker-compose-env.yml文件运行成功之后，添加Nacos配置：

1. 进入`spring-cloud-alibaba-examples/integrated-example`目录下；
2. 在终端中执行`config-init/scripts/nacos-config-quick.sh`脚本文件。

完成所有微服务配置的一键导入。

> 注意：windows操作系统可以通过`git bash`执行shell脚本文件完成配置导入。

### 服务启动

进入`spring-cloud-alibaba-examples/integrated-example`目录下，在终端中执行以下命令`docker-compose -f ./docker-compose/docker-compose-service.yml up -d`来快速部署运行example所需服务。

## 停止所有容器

### 停止服务容器

进入`spring-cloud-alibaba-examples/integrated-example`目录下，在终端中执行以下命令`docker-compose -f ./docker-compose/docker-compose-service.yml down`来停止正在运行的example服务容器。


### 停止组件容器

进入`spring-cloud-alibaba-examples/integrated-example`目录下，在终端中执行以下命令`docker-compose -f ./docker-compose/docker-compose-env.yml down`来停止正在运行的example组件容器。

> 在容器启动时，可以通过`docker-compose -f docker-compose-*.yml up`观察容器的启动过程！

## 体验Demo

准备工作完成后可以运行 demo 示例，主要根据不同的使用场景，可以分别体验用户下单(分布式事务能力)以及模拟高流量点赞(熔断限流以及削峰填谷的能力)。

首先需要分别启动`integrated-frontend`以及`integrated-gateway`微服务应用。

- `integrated-gateway` 模块是整个最佳实践示例的网关。
- `integrated-frontend` 为最佳实践示例的简易前端页面。

### 分布式事务能力

#### 场景说明

针对分布式事务能力，SCA社区提供了**用户下单购买货物的场景**，下单后：

- 先请求库存模块，扣减库存
- 扣减账户余额
- 生成订单信息返回响应

##### 启动测试

分别启动`integrated-storage`,`integrated-account`,`integrated-order`三个微服务应用。

访问`http://integrated-frontend:8080/order` 来体验对应场景。

直接点击下单按钮提交表单，应用模拟客户端向网关发送了一个创建订单的请求。

- 用户的 userId 为 admin
- 用户下单的商品编号为1号
- 此次订单购买的商品个数为1个

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155416524.png)

在本 demo 示例中，为了便于演示，每件商品的单价都为2。

而在前面的准备工作中，**初始化业务数据库表**的时候应用新建了一个用户，用户userId 为 admin，余额为 3 元；同时新建了一个编号为 1 号的商品，库存为 100 件。

因此通过上述的操作，应用会创建一个订单，扣减对应商品编号为 1 号的库存个数(100-1=99)，扣减 admin 用户的余额(3-2=1)。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155429801.png)

如果再次请求相同的接口，同样是先扣减库存(99-1=98)，但是会因为 admin 用户余额不足而抛出异常，并被 Seata 捕获，执行分布式事务二阶段提交，回滚事务。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155436112.png)

可以看到数据库中库存的记录因为回滚之后仍然为 99 件。

### 熔断限流，削峰填谷能力

#### 场景说明

针对大流量背景下的服务熔断限流，削峰填谷，SCA社区提供了**用户为商品进行点赞的场景**。在此场景下，SCA社区提供了两种应对大流量的处理方式。

- Sentinel 在网关侧绑定指定网关路由进行服务的熔断降级。
- RocketMQ 进行流量削峰填谷，在大流量请求下，生产者向 RocketMQ 发送消息，而消费者则通过可配置的消费速率进行拉取消费，减少大流量直接请求数据库增加点赞请求的压力。

#### 启动测试

分别启动`integrated-praise-provider`以及`integrated-praise-consumer`模块。

- Sentinel 服务熔断降级

访问`http://integrated-frontend:8080/sentinel` 体验对应场景。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155501290.png)

网关路由点赞服务的限流规则为 5，而在前端通过异步处理模拟了 10 次并发请求。

因此可以看到 Sentinel 在 Gateway 侧针对多出的流量进行了服务熔断返回 fallback 给客户端，同时数据库的点赞数进行了更新(+5)。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914155755103.png)

- RocketMQ 进行流量削峰填谷

访问`http://integrated-frontend:8080/rocketmq` 体验对应场景。

由于之前在 Nacos 中配置了`integrated-praise-consumer`消费者模块的消费速率以及间隔，在点击按钮时应用模拟 1000 个点赞请求，针对 1000 个点赞请求，`integrated-praise-provider`
会将 1000 次请求都向 Broker 投递消息，而在消费者模块中会根据配置的消费速率进行消费，向数据库更新点赞的商品数据，模拟大流量下 RocketMQ 削峰填谷的特性。

可以看到数据库中点赞的个数正在动态更新。

![image-20221016173604059](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016173604059.png)

## 其他

本示例**仅是针对各个组件选取出了较为典型的功能特性来服务应用场景**

当然各个组件的功能特性不仅仅只包含最佳实践中演示的这些，如果您感兴趣或是想要深入了解，欢迎学习各个组件的独立 example 相关文档。

- Nacos examples
  - [Nacos config example](../../../nacos-example/nacos-config-example/readme-zh.md)
  - [Nacos discovery example](../../../nacos-example/nacos-discovery-example/readme-zh.md)
- [Sentinel core example](../../../sentinel-example/sentinel-core-example/readme-zh.md)
- [Seata example](../../../seata-example/readme-zh.md)
- [RocketMQ example](../../../rocketmq-example/readme-zh.md)

