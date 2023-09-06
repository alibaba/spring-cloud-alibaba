# Spring Cloud Alibaba 容器化部署最佳实践 | 本地部署版本

## 准备工作

### 环境声明

在运行本地示例之前，需要保证本机具备以下的基础环境，如果您的本地没有当前的环境，下面会一步步进行搭建，演示搭建过程。
当然您也可以通过 Spring Cloud Alibaba （下文简称为SCA）社区提供的docker-compose文件快速启动相应组件。

- Nacos 服务端
- Seata 服务端
- RocketMQ 服务端
- MySQL 服务端

### 组件服务版本

本项目的各个组件版本请移步至各个社区的 release 页面进行下载并解压运行。

- [Nacos: 2.1.0 版本](https://github.com/alibaba/nacos/releases)
- [Seata: 1.5.1 版本](https://github.com/seata/seata/releases)
- [RocketMQ: 4.9.4 版本](https://github.com/apache/rocketmq/releases)
- MySQL: 5.7 版本

### Hosts配置

为了保证代码可以正常启动，请先配置好本机的 host 映射，在配置文件中新增如下的映射。
```shell
# for integrated-example
127.0.0.1 integrated-mysql
127.0.0.1 nacos-server
127.0.0.1 seata-server
127.0.0.1 rocketmq
127.0.0.1 gateway-service
127.0.0.1 integrated-frontend
```

### 数据库配置

下面开始本地环境搭建准备，在数据库配置开始之前，请确保 MySQL 的服务端开启。

#### 初始化业务表

针对第一个场景，订单、账户、库存微服务都需要各自的数据库，而第二个场景模拟点赞也需要存储点赞信息的数据库。

运行 `spring-cloud-alibaba-examples/integrated-example/config-init/sql/init.sql` 的 sql 脚本一键创建业务所需的环境以及 Seata 相关的表。

### Nacos配置

至此，数据库的服务配置完毕，下面需要配置 Nacos 的配置中心有关所有的微服务配置文件。

#### Nacos启动

为了便于 example 的演示，这里采用 Nacos 的`standalone`模式启动，进入到 Nacos 解压后的目录下，执行如下命令。

```shell
#Linux/Mac环境
sh bin/startup.sh -m standalone
#如果您是Ubuntu环境，执行上述命令启动报错提示[[符号找不到，可以执行如下的命令
bash bin/startup.sh -m standalone
#Win环境
.\bin\startup.cmd -m standalone
```

#### 新增配置文件

在批量导入配置之前，请先修改`spring-cloud-alibaba-examples/integrated-example/config-init/config/datasource-config.yaml` 中的数据源配置**(用户名和密码)**。

之后运行`spring-cloud-alibaba-examples/integrated-example/config-init/scripts/nacos-config-quick.sh` 来完成所有微服务配置的一键导入。

```shell
# linux
sh nacos-config-quick.sh
# windows 可以使用git bash来完成配置的导入 执行命令同上
```

### Seata 配置

Nacos 服务注册中心以及配置中心部署完毕之后，下面是 Seata 服务端的配置。

Seata 的 db 模式需要额外配置数据库信息以及修改 Seata 服务端的配置文件，且在新版本中配置文件相较于旧版本进行了合并，因此这里为了便于演示方便，采用 Seata 单机的`file`模式启动 Seata Server。

#### 启动 Seata Server

进入到 release 解压后的 seata 目录中，执行如下命令。

```shell
#Linux/Mac环境
sh ./bin/seata-server.sh
#Win环境
bin\seata-server.bat
```

### RocketMQ 配置

Seata 服务启动后可以启动 RocketMQ 的 NameServer 以及 Broker 服务。

进入到 release 解压后的 rocketmq 目录中，执行如下命令。

#### 启动 NameServer

```shell
#Linux/Mac环境
sh bin/mqnamesrv
#Win环境
.\bin\mqnamesrv.cmd
```

#### 启动 Broker

```shell
#Linux/Mac环境
sh bin/mqbroker
#Win环境
.\bin\mqbroker.cmd -n localhost:9876
```

## 运行 Demo 示例

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