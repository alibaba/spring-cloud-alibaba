# Spring Cloud Alibaba Containerized Deployment Best Practices | Local Deployment Version

## Preparation

### Environment Statement

Before running the local example, you need to ensure that the following base environment is available locally, if you don't have the current environment locally, the following will be built step by step

- Nacos server
- Seata server
- RocketMQ server
- Mysql server

### Component Service Versions

For each component version of this project, please go to the release page of each community to download and unpack

- [Nacos: 2.1.0](https://github.com/alibaba/nacos/releases)
- [Seata: 1.5.1](https://github.com/seata/seata/releases)
- [RocketMQ: 4.9.4](https://github.com/apache/rocketmq/releases)
- Mysql: 5.7

### Database configuration

The following begins the local environment build preparation, before the database configuration begins, please ensure that the server side of Mysql is open

> Initialize business tables

For the first scenario, the order, account and inventory microservices need their own databases, while the second scenario simulates the likes and also needs a database to store the likes information

Run the following sql script to create the required environment for the business with one click

```sql
-- Database Business Initialization for Storage Microservice
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

-- Database Business Initialization for Account Microservice
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

-- Database Business Initialization for Order Microservice
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

-- Database Business Initialization for Praise Microservice
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

> Seata's undo_log configuration

Run the following SQL script to automatically create the undo_log table required by Seata

```sql
-- Database Seata Initialization for Storage Microservices
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

-- Database Seata Initialization for Account Microservices
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

-- Database Seata Initialization for Order Microservices
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
### Nacos configuration

At this point, the database services are configured, and the following configuration center of Nacos needs to be configured regarding all microservice configuration files

> Nacos startup

To facilitate the demonstration of the example, here we use the ``standalone`` mode of Nacos to start up, go to the directory where nacos is unpacked

```sh
#Linux/Mac environment
sh bin/startup.sh -m standalone
#If you are in Ubuntu and the above command gives you an error [[symbol not found, you can run the following command
bash bin/startup.sh -m standalone
#Win environment
. \bin\startup.cmd -m standalone
```

After startup access port 8848 on your device to access the Nacos console, default username and password are nacos

> Adding configuration files

For demonstration purposes, all configuration files belong to the  DEFAULT_GROUP , no need to fill in additional

- Routing restriction configuration file for Spring Cloud Gateway integration with Sentinel, Data ID: `sentinel-gateway`, format: json

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

- Data source configuration information for Nacos shared configuration, Data ID:`datasorce-config.yaml`, format:yaml

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    username: #username for your mysql
    password: #password for your mysql
  main:
      allow-bean-definition-overriding: true
mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

- Configuration of storage inventory microservice, Data ID:`integrated-storage.yaml`, format:yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/integrated_storage?useSSL=false&characterEncoding=utf8
```

- Configuration of account microservice, Data ID:`integrated-account.yaml`, format:yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/integrated_account?useSSL=false&characterEncoding=utf8
```

- Configuration of  order microservice, Data ID:`integrated-order.yaml`, format:yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/integrated_order?useSSL=false&characterEncoding=utf8
```

- The configuration of the dotted business producer, Data ID:`integrated-provider.yaml`, format:yaml

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

- The configuration of the dotted business consumer, Data ID:`integrated-consumer.yaml`, format:yaml

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

- Integrated gateway configuration, Data ID:`integrated-gateway.yaml`, format:yaml

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

### Seata Configuration

After the Nacos service registry and configuration center are deployed, here is the configuration of the Seata server

Seata's db mode requires additional configuration of database information and modification of the Seata Server configuration file, and the configuration file has been merged in the new version compared to the old one.

> Start Seata Server

Go to the seata directory after Release unpacking

```sh
#Linux/Mac environment
sh . /bin/seata-server.sh
#Win environment
bin\seata-server.bat
````

### RocketMQ configuration

After starting the Seata service, you can start the NameServer and Broker services of RocketMQ

Go to the release unpacked rocketmq directory

> Start the NameServer

```sh
# Linux/Mac environment
sh bin/mqnamesrv
#Win environment
. \bin\mqnamesrv.cmd
```

> Start Broker

```sh
# Linux/Mac environment
sh bin/mqbroker
#Win environment
. \bin\mqbroker.cmd
```

## Run the demo example

After the preparation work is finished, you can run the demo, mainly according to different usage scenarios, you can experience the user order (distributed transaction capability) and simulate the high traffic point (meltdown and limit the flow, and the ability to cut the peak and fill the valley) respectively

First, you need to start the `integrated_business` and `integrated_gateway` projects respectively.

- The gateway example is the gateway for the entire demo example
- business simulates a user request gateway

### Distributed transaction capability

> Scenario Description

For the distributed transaction capability, we provide a scenario **where a user places an order for goods**, and after placing the order

- first request the inventory module and deduct the inventory
- Deduct the account balance
- Generate order information to return a response

> Start test

Start `integrated_storage`,`integrated_account`,`integrated_order` microservices respectively

Visit `http://127.0.0.1:8009/order/create?userId=admin&commodityCode=1&count=1` to simulate sending an order request

After the above operation, we simulate the client sending an order creation request to the gateway

- The user's userId is admin
- The item number of the order placed by the user is number 1
- The number of products purchased in this order is 1

In this demo example, for the sake of demonstration, the unit price of each item is 2

In the previous preparation, when initializing the business database table, we created a new user userId=admin with a balance of $3; at the same time, we created a new item numbered 1, with 75 items in stock

Therefore, through the above operation, we will create an order, deduct the number of items in stock corresponding to item number 1 (75-1=74) and deduct the balance of the admin user (3-2=1)

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220823163456869.png)

If the same interface is requested again, again the inventory will be deducted first (74-1=73), but an exception will be thrown because the balance of the admin user is insufficient and will be caught by Seata, which will perform a distributed transaction with a two-stage commit, roll back the transaction.
![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220823163522376.png)

And you can see that the inventory record in the database is still 74 pieces because of the rollback

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220823163548727.png)

### Fusion limit & Peak shaving capability

> Scenario Description

For the service fusion flow limiting and peak and valley cutting in the context of high traffic, we provide a scenario **where users make likes for products**. In this scenario, we provide two ways to deal with high traffic

- Sentinel on the gateway side to bind the specified gateway route for service fusion degradation
- RocketMQ performs peak and valley shaving, where the producer sends messages to RocketMQ under high traffic requests, while the consumer pulls and consumes through configurable consumption rates, reducing the pressure of high traffic direct requests to the database to increase the number of likes

> Start test

Start `integrated_provider` and `integrated_consumer` modules respectively

- Sentinel service meltdown degradation

Visit `http://127.0.0.1:8009/praise/sentinel`, as we have configured the gateway to route the likes service with a flow limit rule of 5 in Nacos, and the code simulates 10 threads for concurrent requests

Therefore, we can see that Sentinel performs a service fusion on the Gateway side to return the fallback to the client for the extra traffic, while the number of likes in the database is updated (+5)

- RocketMQ does peak and valley reduction

Visit `http://127.0.0.1:8009/praise/rocketmq`, as we previously configured the consumption rate and interval of the `integrated-consumer` consumer module in Nacos, we simulate 1000 likes requests in the interface, and for 1000 likes requests, ` integrated_provider` will deliver all 1000 requests to Broker, and the consumer module will consume according to the configured consumption rate, and update the database with the product data of the likes, simulating the characteristics of RocketMQ to cut the peaks and fill the valleys under high traffic.

You can see that the number of likes in the database is being dynamically updated

## Other

This example **is only a selection of typical features for each component to serve the application scenario**.

Of course, the functionalities of each component are not only the ones demonstrated in this demo, if you are interested or want to learn more, you are welcome to study the separate example documentation of each component

- Nacos Examples
    - [nacos-config-example](../../nacos-example/nacos-config-example/readme.md)
    - [nacos-discovery-example](../../nacos-example/nacos-discovery-example/readme.md)
- [Sentinel-Core-Example](../../sentinel-example/sentinel-core-example/readme.md)
- [Seata Examples](../../seata-example/readme.md)
- [RocketMQ Example](../../rocketmq-example/readme.md)