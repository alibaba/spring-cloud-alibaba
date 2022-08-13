# Spring Cloud Alibaba容器化部署最佳实践 | 本地部署版本

## 准备工作

### 环境声明

在运行本地示例之前，需要保证本机具备以下的基础环境，如果您的本地没有当前的环境，下面会一步步进行搭建,演示搭建过程

- Nacos服务端
- Seata服务端
- RocketMQ服务端
- Mysql服务端

### 组件服务版本

本项目的各个组件版本请移步至各个社区的release页面进行下载并解压

- [Nacos: 2.1.0 版本](https://github.com/alibaba/nacos/releases)
- [Seata: 1.5.1 版本](https://github.com/seata/seata/releases)
- [RocketMQ: 4.9.4版本](https://github.com/apache/rocketmq/releases)
- Mysql: 5.7版本

### 数据库配置

下面开始本地环境搭建准备，在数据库配置开始之前，请确保Mysql的服务端开启

> 初始化业务表

针对第一个场景，订单、账户、库存微服务都需要各自的数据库，而第二个场景模拟点赞也需要存储点赞信息的数据库

运行如下的sql脚本一键创建业务所需的环境

```sql
-- Storage库存微服务的数据库业务初始化
DROP DATABASE IF EXISTS integrated_storage;
CREATE DATABASE integrated_storage;
CREATE TABLE `integrated_storage.storage` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `commodity_code` (`commodity_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
INSERT INTO `integrated_storage.storage` VALUES ('1', '1', '75', '2022-08-07 22:48:29', '2022-08-14 13:49:05');

-- Account账户微服务的数据库业务初始化
DROP DATABASE IF EXISTS integrated_account;
CREATE DATABASE integrated_account;
CREATE TABLE `integrated_account.account` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `money` int(11) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
INSERT INTO `account` VALUES ('1', 'admin', '3', '2022-08-07 22:53:01', '2022-08-14 13:49:05');

-- Order订单微服务的数据库业务初始化
DROP DATABASE IF EXISTS integrated_order;
CREATE DATABASE integrated_order;
CREATE TABLE `integrated_order.order` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `money` int(11) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

-- 点赞业务的数据库初始化
DROP DATABASE IF EXISTS integrated_praise;
CREATE DATABASE integrated_praise;
CREATE TABLE `integrated_praise.item` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `praise` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
INSERT INTO `item` VALUES ('1', '2800', '2022-08-14 00:33:50', '2022-08-14 14:07:34');
```

> Seata的undo_log配置

运行如下的SQL脚本自动创建Seata所需的undo_log表

```sql
-- Storage库存微服务的数据库Seata初始化
CREATE TABLE `integrated_storage.undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Storage库存微服务的数据库Seata初始化
CREATE TABLE `integrated_account.undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Storage库存微服务的数据库Seata初始化
CREATE TABLE `integrated_order.undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### Nacos配置

至此，数据库的服务配置完毕，下面需要配置Nacos的配置中心有关所有的微服务配置文件

> Nacos启动

为了便于example的演示，这里采用Nacos的`standalone`模式启动，进入到nacos解压后的目录下

```sh
#Linux/Mac环境
sh bin/startup.sh -m standalone
#如果您是Ubuntu环境，执行上述命令启动报错提示[[符号找不到，可以执行如下的命令
bash bin/startup.sh -m standalone
#Win环境
.\bin\startup.cmd -m standalone
```

启动后访问8848端口进入Nacos的控制台，默认用户名和密码都是nacos

> 新增配置文件

为了便于演示，所有的配置文件所属组Group都为默认DEFAULT_GROUP，不需要额外填写

- 用于Spring Cloud Gateway整合Sentinel的路由限流配置文件,Data ID:`sentinel-gateway`，格式:json

```json
[
    {
        "resource": "praiseItemSentinel",
        "count": 5,
        "grade": 1,
        "limitApp": "default",
        "strategy": 0,
        "controlBehavior": 0
    }
]
```

- 数据源配置信息，用于Nacos的共享配置，Data ID:`datasorce-config.yaml`，格式:yaml

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    username: #您的数据库用户名
    password: #您的数据库密码
  main:
      allow-bean-definition-overriding: true
mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

- storage库存微服务的配置，Data ID:`integrated-storage.yaml`，格式:yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/integrated_storage?useSSL=false&characterEncoding=utf8
```

- account账户微服务的配置，Data ID:`integrated-account.yaml`，格式:yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/integrated_account?useSSL=false&characterEncoding=utf8
```

- order订单微服务的配置，Data ID:`integrated-order.yaml`，格式:yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/integrated_order?useSSL=false&characterEncoding=utf8
```

- 点赞业务生产者的配置，Data ID:`integrated-provider.yaml`，格式:yaml

```yaml
spring:
    cloud:
      stream:
        bindings:
            praise-output:
                destination: PRAISE-TOPIC-01
                content-type: application/json 
        rocketmq:
            binder:
                name-server: 127.0.0.1:9876 
            bindings:
                praise-output:
                    producer:
                        group: test
```

- 点赞业务消费者的配置，Data ID:`integrated-consumer.yaml`，格式:yaml

```yaml
spring:
  datasource:
   url: jdbc:mysql://localhost:3306/integrated_praise?useSSL=false&characterEncoding=utf8
  cloud:
    stream:
      bindings:
        praise-input:
          destination: PRAISE-TOPIC-01 
          content-type: application/json 
          group: praise-consumer-group-PRAISE-TOPIC-01 
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          praise-input:
            consumer:
              pullInterval: 4000
              pullBatchSize: 4
```

- 整体网关配置，Data ID:`integrated-gateway.yaml`，格式:yaml

```yaml
spring:
    cloud:
        gateway:
            routes: 
            - id: placeOrder
              uri: lb://integrated-order
              predicates: 
                - Path=/order/create
            - id: queryOrder
              uri: lb://integrated-order
              predicates: 
                - Path=/order/query
            - id: praiseItemRocketMQ
              uri: lb://integrated-provider
              predicates: 
                - Path=/praise/rocketmq
            - id: praiseItemSentinel
              uri: lb://integrated-provider
              predicates: 
                - Path=/praise/sentinel
```

### Seata配置

Nacos服务注册中心以及配置中心部署完毕之后，下面是Seata服务端的配置

Seata的db模式需要额外配置数据库信息以及修改seata服务端的配置文件，且在新版本中配置文件相较于旧版本进行了合并，因此这里为了便于演示方便，采用Seata单机的`file`模式启动Seata Server

> 启动Seata Server

进入到Release解压后的seata目录中

```sh
#Linux/Mac环境
sh ./bin/seata-server.sh
#Win环境
bin\seata-server.bat
```

### RocketMQ配置

Seata服务启动后可以启动RocketMQ的NameServer以及Broker服务

进入到Release解压后的rocketmq目录中

> 启动NameServer

```sh
#Linux/Mac环境
sh bin/mqnamesrv
#Win环境
.\bin\mqnamesrv.cmd
```

> 启动Broker

```sh
#Linux/Mac环境
sh bin/mqbroker
#Win环境
.\bin\mqbroker.cmd
```

## 运行Demo示例

准备工作完成后可以运行demo示例，主要根据不同的使用场景，可以分别体验用户下单(分布式事务能力)以及模拟高流量点赞(熔断限流以及削峰填谷的能力)

首先需要分别启动`integrated_business`以及`integrated_gateway`的工程

- gateway示例是整个demo示例的网关
- business模拟用户请求网关

### 分布式事务能力

> 场景说明

针对分布式事务能力，我们提供了**用户下单购买货物的场景**，下单后

- 先请求库存模块，扣减库存
- 扣减账户余额
- 生成订单信息返回响应

> 启动测试

分别启动`integrated_storage`,`integrated_account`,`integrated_order`三个微服务

访问`http://127.0.0.1:8009/order/create?userId=admin&commodityCode=1&count=1` 来模拟发送下单请求

在上述的操作后，我们模拟客户端向网关发送了一个创建订单的请求

- 用户的userId为admin
- 用户下单的商品编号为1号
- 此次订单购买的商品个数为1个

在本demo示例中，为了便于演示，每件商品的单价都为2

而在前面的准备工作中，初始化业务数据库表的时候我们新建了一个用户userId=admin，余额为3元；同时新建了一个编号为1号的商品，库存为75件

因此通过上述的操作，我们会创建一个订单，扣减对应商品编号为1号的库存个数(75-1=74)，扣减admin用户的余额(3-2=1)
![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220823163456869.png)

如果再次请求相同的接口，同样是先扣减库存(74-1=73)，但是会因为admin用户余额不足而抛出异常，并被Seata捕获，执行分布式事务二阶段提交，回滚事务
![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220823163522376.png)

可以看到数据库中库存的记录因为回滚之后仍然为74件

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220823163548727.png)
### 熔断限流，削峰填谷能力
> 场景说明

针对大流量背景下的服务熔断限流，削峰天谷，我们提供了**用户为商品进行点赞的场景**。在此场景下，我们提供了两种应对大流量的处理方式

- Sentinel在网关侧绑定指定网关路由进行服务的熔断降级
- RocketMQ进行流量削峰填谷，在大流量请求下，生产者向RocketMQ发送消息，而消费者则通过可配置的消费速率进行拉取消费，减少大流量直接请求数据库增加点赞请求的压力

> 启动测试

分别启动`integrated_provider`以及`integrated_consumer`模块

- Sentinel服务熔断降级

访问`http://127.0.0.1:8009/praise/sentinel` ，由于我们之前在Nacos中配置了网关路由点赞服务的限流规则为5，而代码中模拟了10个线程进行并发请求

因此可以看到Sentinel在Gateway侧针对多出的流量进行了服务熔断返回fallback给客户端，同时数据库的点赞数进行了更新(+5)

- RocketMQ进行流量削峰填谷

访问`http://127.0.0.1:8009/praise/rocketmq` ，由于我们之前在Nacos中配置了`integrated-consumer`消费者模块的消费速率以及间隔，在接口中我们模拟1000个点赞请求，针对1000个点赞请求，`integrated_provider`会将1000次请求都向Broker投递消息，而在消费者模块中会根据配置的消费速率进行消费，向数据库更新点赞的商品数据，模拟大流量下RocketMQ削峰填谷的特性

可以看到数据库中点赞的个数正在动态更新

## 其他

本示例**仅是针对各个组件选取出了较为典型的功能特性来服务应用场景**

当然各个组件的功能特性不仅仅只包含本demo中演示的这些，如果您感兴趣或是想要深入了解，欢迎学习各个组件的独立example相关文档

- Nacos Examples
  - [nacos-config-example](../../nacos-example/nacos-config-example/readme-zh.md)
  - [nacos-discovery-example](../../nacos-example/nacos-discovery-example/readme-zh.md)
- [Sentinel-Core-Example](../../sentinel-example/sentinel-core-example/readme-zh.md)
- [Seata Examples](../../seata-example/readme-zh.md)
- [RocketMQ Example](../../rocketmq-example/readme-zh.md)