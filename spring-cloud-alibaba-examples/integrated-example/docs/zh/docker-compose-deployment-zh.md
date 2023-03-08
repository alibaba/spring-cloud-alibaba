# Spring Cloud Alibaba 容器化部署最佳实践 | Docker-Compose版本

## 准备工作

如果您还未安装docker或者docker-compose环境，请移步官方文档进行环境搭建

- docker：https://docs.docker.com/desktop/install/linux-install/
- docker-compose：https://docs.docker.com/compose/install/

### 环境声明

### Host配置
为了保证代码可以正常启动，请先配置好本机的 host 映射，在配置文件中新增如下的映射。
```sh
# for integrated-example
127.0.0.1 integrated-mysql
127.0.0.1 nacos-server
127.0.0.1 seata-server
127.0.0.1 rocketmq
127.0.0.1 gateway-service
127.0.0.1 integrated-frontend
```

### 快速启动组件
进入到`spring-cloud-alibaba-examples/integrated-example` 目录下，执行如下命令执行`docker-compose -f compose-quickstart.yml up -d`快速部署组件

#### 新增配置文件

运行成功docker-compose文件之后，新增nacos配置：运行`spring-cloud-alibaba-examples/integrated-example/scripts/nacos-config-quick.sh` 来完成所有微服务配置的一键导入。

## 运行 Demo 示例

准备工作完成后可以运行 demo 示例，主要根据不同的使用场景，可以分别体验用户下单(分布式事务能力)以及模拟高流量点赞(熔断限流以及削峰填谷的能力)。

首先需要分别启动`integrated_frontend`以及`integrated_gateway`应用。

- `integrated_gateway` 模块是整个最佳实践示例的网关。
- `integrated_frontend` 模块是最佳实践示例的简易前端页面。

### 分布式事务能力

#### 场景说明

针对分布式事务能力，我们提供了**用户下单购买货物的场景**，下单后：

- 先请求库存模块，扣减库存
- 扣减账户余额
- 生成订单信息返回响应

##### 启动测试

分别启动`integrated_storage`,`integrated_account`,`integrated_order`三个微服务模块。

访问`http://integrated-frontend:8080/order` 来体验对应场景。

直接点击下单按钮提交表单，模拟客户端向网关发送了一个创建订单的请求。

- 用户的 userId 为 admin
- 用户下单的商品编号为1号
- 此次订单购买的商品个数为1个

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155416524.png)

在本 demo 示例中，为了便于演示，每件商品的单价都为2。

而在前面的准备工作中，**初始化业务数据库表**的时候，新建了一个用户 userId = admin，余额为 3 元；同时新建了一个编号为 1 号的商品，库存为 100 件。

因此通过上述的操作，我们会创建一个订单，扣减对应商品编号为 1 号的库存个数(100-1=99)，扣减 admin 用户的余额(3-2=1)。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155429801.png)

如果再次请求相同的接口，同样是先扣减库存(99-1=98)，但是会因为 admin 用户余额不足而抛出异常，并被 Seata 捕获，执行分布式事务二阶段提交，回滚事务。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155436112.png)

可以看到数据库中库存的记录因为回滚之后仍然为 99 件。

### 熔断限流，削峰填谷能力

#### 场景说明

针对大流量背景下的服务熔断限流，削峰填谷，我们提供了**用户为商品进行点赞的场景**。在此场景下，我们提供了两种应对大流量的处理方式。

- Sentinel 在网关侧绑定指定网关路由进行服务的熔断降级。
- RocketMQ 进行流量削峰填谷，在大流量请求下，生产者向 RocketMQ 发送消息，而消费者则通过可配置的消费速率进行拉取消费，减少大流量直接请求数据库增加点赞请求的压力。

#### 启动测试

分别启动`integrated_provider`以及`integrated_consumer`模块。

- Sentinel 服务熔断降级

访问`http://integrated-frontend:8080/sentinel` 体验对应场景。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155501290.png)

网关路由点赞服务的限流规则为 5，而在前端通过异步处理模拟了 10 次并发请求。

因此可以看到 Sentinel 在 Gateway 侧针对多出的流量进行了服务熔断返回 fallback 给客户端，同时数据库的点赞数进行了更新(+5)。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914155755103.png)

- RocketMQ 进行流量削峰填谷

访问`http://integrated-frontend:8080/rocketmq` 体验对应场景。

由于我们之前在 Nacos 中配置了`integrated-consumer`消费者模块的消费速率以及间隔，在点击按钮时我们模拟 1000 个点赞请求，针对 1000 个点赞请求，`integrated_provider`
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