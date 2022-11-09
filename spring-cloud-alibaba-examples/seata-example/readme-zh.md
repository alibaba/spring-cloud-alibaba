# Seata Example

## 项目说明


本项目演示如何使用 Seata Starter 完成 Spring Cloud 应用的分布式事务接入。

[Seata](https://github.com/seata/seata) 是 阿里巴巴 开源的 分布式事务中间件，以 高效 并且对业务 0 侵入 的方式，解决 微服务 场景下面临的分布式事务问题。



## 准备工作

在运行此示例之前，你需要先完成如下几步准备工作：

1. 配置数据库

2. 创建 UNDO_LOG 表

3. 创建 示例中 业务所需要的数据库表

4. 创建示例中Nacos data-id: `seata.properties` , Group: `SEATA_GROUP`(seata 1.5.1 默认分组) ,导入 [Nacos 配置](https://github.com/seata/seata/blob/1.5.0/script/config-center/config.txt)
    在seata.properties中增加示例中需要的如下[事务群组配置](https://seata.io/zh-cn/docs/user/configurations.html)
```
   service.vgroupMapping.order-service-tx-group=default
   service.vgroupMapping.account-service-tx-group=default
   service.vgroupMapping.business-service-tx-group=default
   service.vgroupMapping.storage-service-tx-group=default
```   
5. 启动 Seata Server
  Seata 1.5.1 开始支持seata控制台 本地访问控制台地址：http://127.0.0.1:7091
  通过seata控制台可以观察正在执行的事务信息和全局锁信息,事务执行结束即删除相关信息。
### 配置数据库

首先，你需要有一个支持 InnoDB 引擎的 MySQL 数据库。

**注意**： 实际上，Seata 支持不同的应用使用完全不相干的数据库，但是这里为了简单地演示一个原理，所以我们选择了只使用一个数据库。

将 `account-server`、`order-service`、`storage-service` 这三个应用中的 resources 目录下的 `application.yml` 文件中的如下配置修改成你运行环境中的实际配置。

```
base:
  config:
    mdb:
      hostname: your mysql server ip address
      dbname: your database name for test
      port: your mysql server listening port
      username: your mysql server username
      password: your mysql server password
```

### 创建 undo_log 表

Seata AT 模式 需要使用到 undo_log 表。

``` $sql
-- 注意此处0.3.0+ 增加唯一索引 ux_undo_log
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```
### 导入 seata-server db模式所需要的数据库表
在数据库中初始化[global_table、branch_table、lock_table、distributed_lock](https://github.com/seata/seata/blob/1.5.0/script/server/db/mysql.sql)
```$sql
-- -------------------------------- The script used when storeMode is 'db' --------------------------------
-- the table to store GlobalSession data
CREATE TABLE IF NOT EXISTS `global_table`
(
    `xid`                       VARCHAR(128) NOT NULL,
    `transaction_id`            BIGINT,
    `status`                    TINYINT      NOT NULL,
    `application_id`            VARCHAR(32),
    `transaction_service_group` VARCHAR(32),
    `transaction_name`          VARCHAR(128),
    `timeout`                   INT,
    `begin_time`                BIGINT,
    `application_data`          VARCHAR(2000),
    `gmt_create`                DATETIME,
    `gmt_modified`              DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_status_gmt_modified` (`status` , `gmt_modified`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- the table to store BranchSession data
CREATE TABLE IF NOT EXISTS `branch_table`
(
    `branch_id`         BIGINT       NOT NULL,
    `xid`               VARCHAR(128) NOT NULL,
    `transaction_id`    BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id`       VARCHAR(256),
    `branch_type`       VARCHAR(8),
    `status`            TINYINT,
    `client_id`         VARCHAR(64),
    `application_data`  VARCHAR(2000),
    `gmt_create`        DATETIME(6),
    `gmt_modified`      DATETIME(6),
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- the table to store lock data
CREATE TABLE IF NOT EXISTS `lock_table`
(
    `row_key`        VARCHAR(128) NOT NULL,
    `xid`            VARCHAR(128),
    `transaction_id` BIGINT,
    `branch_id`      BIGINT       NOT NULL,
    `resource_id`    VARCHAR(256),
    `table_name`     VARCHAR(32),
    `pk`             VARCHAR(36),
    `status`         TINYINT      NOT NULL DEFAULT '0' COMMENT '0:locked ,1:rollbacking',
    `gmt_create`     DATETIME,
    `gmt_modified`   DATETIME,
    PRIMARY KEY (`row_key`),
    KEY `idx_status` (`status`),
    KEY `idx_branch_id` (`branch_id`),
    KEY `idx_xid_and_branch_id` (`xid` , `branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `distributed_lock`
(
    `lock_key`       CHAR(20) NOT NULL,
    `lock_value`     VARCHAR(20) NOT NULL,
    `expire`         BIGINT,
    primary key (`lock_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('AsyncCommitting', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('RetryCommitting', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('RetryRollbacking', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('TxTimeoutCheck', ' ', 0);
```
### 创建 示例中 业务所需要的数据库表

```$sql
DROP TABLE IF EXISTS `storage_tbl`;
CREATE TABLE `storage_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`commodity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `order_tbl`;
CREATE TABLE `order_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT 0,
  `money` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `account_tbl`;
CREATE TABLE `account_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `money` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

### 启动 Seata Server 这里介绍SpringBoot 和下载server两种方式

1.运行 seata-server 启动Seata server
示例中采用nacos 作为配置，注册中心 存储模式为：db 采用mysql 

2.或点击这个页面 [Seata 官网Github](https://github.com/seata/seata/releases)，下载最新版本的 Seata Server 端.
进入解压之后的 bin 目录，执行如下命令来启动, 所有启动参数为可选项。

```$shell
sh seata-server.sh -p $LISTEN_PORT -m $MODE(file or db) -h $HOST -e $ENV
```
-p seata-server 监听服务端口号   
-m 存储模式，可选值：file、db。file 用于单点模式，db用于ha模式，当使用db存储模式，需要修改配置中store配置节点的数据库配置，同时在数据库中初始化[global_table、branch_table和 
lock_table](https://github.com/seata/seata/blob/1.5.0/script/server/db/mysql.sql)   
-h 用于解决seata-server和业务侧跨网络问题，其配置的host值直接显示到注册中心的服务可用地址host，当跨网络时这里需要配置为公网IP或NATIP，若都在同一局域网则无需配置   
-e 用于解决多环境配置中心隔离问题   
采用如下命令来启动 Seata Server

```$shell
sh seata-server.sh -p 8091 -m file
```

**注意** 如果你修改了endpoint且注册中心使用默认file类型，那么记得需要在各个示例工程中的 `file.conf` 文件中，修改 grouplist 的值(当registry.conf 中registry.type 或 config.type 为file 时会读取内部的file节点中的文件名，若type不为file将直接从配置类型的对应元数据的注册配置中心读取数据)，推荐大家使用 nacos 作为配置注册中心。


## 运行示例

分别运行 `account-server`、`order-service`、`storage-service` 和 `business-service` 这三个应用的 Main 函数，启动示例。

启动示例后，通过 HTTP 的 GET 方法访问如下 URL，可以分别验证在 `business-service` 中 通过 RestTemplate 和 FeignClient 调用其他服务的场景。

```$xslt
http://127.0.0.1:18081/seata/feign

http://127.0.0.1:18081/seata/rest

```

## 如何验证分布式事务成功？

### Xid 信息是否成功传递

在 `account-server`、`order-service` 和 `storage-service` 三个 服务的 Controller 中，第一个执行的逻辑都是输出 RootContext 中的 Xid 信息，如果看到都输出了正确的 Xid 信息，即每次都发生变化，且同一次调用中所有服务的 Xid 都一致。则表明 Seata 的 Xid 的传递和还原是正常的。

### 数据库中数据是否一致

在本示例中，我们模拟了一个用户购买货物的场景，StorageService 负责扣减库存数量，OrderService 负责保存订单，AccountService 负责扣减用户账户余额。

为了演示样例，我们在 OrderService 和 AccountService 中 使用 Random.nextBoolean() 的方式来随机抛出异常,模拟了在服务调用时随机发生异常的场景。

如果分布式事务生效的话， 那么以下等式应该成立


- 用户原始金额(1000) = 用户现存的金额  +  货物单价 (2) * 订单数量 * 每单的货物数量(2)

- 货物的初始数量(100) = 货物的现存数量 + 订单数量 * 每单的货物数量(2)

## 对 Spring Cloud 支持点

- 通过 Spring MVC 提供服务的服务提供者，在收到 header 中含有 Seata 信息的 HTTP 请求时，可以自动还原 Seata 上下文。

- 支持服务调用者通过 RestTemplate 调用时，自动传递 Seata 上下文。

- 支持服务调用者通过 FeignClient 调用时，自动传递 Seata 上下文。

- 支持 SeataClient 和 Hystrix 同时使用的场景。

- 支持 SeataClient 和 Sentinel 同时使用的场景。
