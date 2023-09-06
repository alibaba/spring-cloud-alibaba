# Spring Cloud Alibaba容器化部署最佳实践 | Kubernetes Helm-Chart 版本

## 准备工作

此版本为 Spring Cloud Alibaba （后文简称为SCA）最佳实践 Kubernetes 部署版本，运行示例需要准备如下环境：

- Kubernetes（建议使用 Docker Desktop 内置集成的 Kubernetes 环境进行体验。）
- Helm

如果测试机器上还未具备如上环境，请移步至对应官方文档进行环境搭建。

- [Helm 安装](https://helm.sh/zh/docs/intro/install/)
- [Kubernetes Docker Desktop 快捷安装](https://docs.docker.com/desktop/kubernetes/)

在这里通过 NodePort 的方式来向外界暴露 Kubernetes 中 Pod 的服务，在启动测试前还需配置好 Kubernetes 集群节点的 ip 映射。

```sh
# 实际情况请结合您的 K8S 节点的公网 ip 进行调整
120.24.xxx.xxx integrated-frontend
120.24.xxx.xxx gateway-service
120.24.xxx.xxx integrated-mysql-web
120.24.xxx.xxx nacos-mysql-web
120.24.xxx.xxx nacos-svc
```

## 启动测试

进入到 `spring-cloud-alibaba-examples/integrated-example` 目录下，执行如下命令利用 Helm 部署应用程序。
```shell
helm package helm-chart

helm install integrated-example integrated-example-1.0.0.tgz
```
通过运行上述命令，根据SCA社区提供的 Helm Chart 文档通过 Helm 快速完成最佳实践示例的部署。

可以通过 Kubernetes 提供的 `kubectl` 命令查看各容器资源部署的情况，耐心等待**所有容器完成启动后**即可到对应页面体验各个组件的使用场景及能力。

如果您想停止体验，输入如下命令。
```shell
helm uninstall integrated-example
```

### 分布式事务能力

#### 场景说明

针对分布式事务能力，SCA社区提供了**用户下单购买货物的场景**，下单后：

- 先请求库存模块，扣减库存
- 扣减账户余额
- 生成订单信息返回响应

##### 启动测试

访问`http://integrated-frontend:30080/order` 来体验对应场景。

直接点击下单按钮提交表单，模拟客户端向网关发送了一个创建订单的请求。

- 用户的 userId 为 admin
- 用户下单的商品编号为1号
- 此次订单购买的商品个数为1个

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143033445.png)

在本 demo 示例中，为了便于演示，每件商品的单价都为2。

而在 `integrated-mysql` 容器的初始化时，**初始化业务数据库表**的时候新建了一个用户，用户的userId为admin，余额为 3 元；同时新建了一个编号为 1 号的商品，库存为 100 件。

因此通过上述的操作，应用会创建一个订单，扣减对应商品编号为 1 号的库存个数(100-1=99)，扣减 admin 用户的余额(3-2=1)。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143057730.png)

如果再次请求相同的接口，同样是先扣减库存(99-1=98)，但是会因为 admin 用户余额不足而抛出异常，并被 Seata 捕获，执行分布式事务二阶段提交，回滚事务。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143104810.png)

可以看到数据库中库存的记录因为回滚之后仍然为 99 件。

### 熔断限流，削峰填谷能力

#### 场景说明

针对大流量背景下的服务熔断限流，削峰填谷，SCA社区提供了**用户为商品进行点赞的场景**。在此场景下，SCA社区提供了两种应对大流量的处理方式。

- Sentinel 在网关侧绑定指定网关路由进行服务的熔断降级。
- RocketMQ 进行流量削峰填谷，在大流量请求下，生产者向 RocketMQ 发送消息，而消费者则通过可配置的消费速率进行拉取消费，减少大流量直接请求数据库增加点赞请求的压力。

#### 启动测试

- Sentinel 服务熔断降级

访问`http://integrated-frontend:30080/sentinel` 体验对应场景。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143120697.png)

网关路由点赞服务的限流规则为 5，而在前端通过异步处理模拟了 10 次并发请求。

因此可以看到 Sentinel 在 Gateway 侧针对多出的流量进行了服务熔断返回 fallback 给客户端，同时数据库的点赞数进行了更新(+5)。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143203773.png)

- RocketMQ 进行流量削峰填谷

访问`http://integrated-frontend:30080/rocketmq` 体验对应场景。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143342664.png)

由于之前在 Nacos 中配置了`integrated-praise-consumer`消费者模块的消费速率以及间隔，在点击按钮时应用将会模拟 1000 个点赞请求，针对 1000 个点赞请求，`integrated-praise-provider`
会将 1000 次请求都向 Broker 投递消息，而在消费者模块中会根据配置的消费速率进行消费，向数据库更新点赞的商品数据，模拟大流量下 RocketMQ 削峰填谷的特性。

可以看到数据库中点赞的个数正在动态更新。

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143352619.png)

## 其他

本示例**仅是针对各个组件选取出了较为典型的功能特性来服务应用场景**。

当然各个组件的功能特性不仅仅只包含最佳实践中演示的这些，如果您对SCA感兴趣或是想要深入了解SCA项目，欢迎阅览各个组件的独立 example 相关文档。

- Nacos examples
    - [Nacos config example](../../../nacos-example/nacos-config-example/readme-zh.md)
    - [Nacos discovery example](../../../nacos-example/nacos-discovery-example/readme-zh.md)
- [Sentinel core example](../../../sentinel-example/sentinel-core-example/readme-zh.md)
- [Seata example](../../../seata-example/readme-zh.md)
- [RocketMQ example](../../../rocketmq-example/readme-zh.md)