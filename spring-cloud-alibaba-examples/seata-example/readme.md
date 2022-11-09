# Seata Example

## Project Instruction


This project demonstrates how to use Seata starter to complete the distributed transaction access of spring cloud applications.

[Seata](https://github.com/seata/seata) It is Alibaba open source distributed transaction middleware, which solves the distributed transaction problem in the microservice scenario in an efficient and non-invasive way.



## Preparations

Before running this example, you need to complete the following steps:

1. Configure the database

2. Create UNDO_ LOG table

3. Create the database tables needed by the business in the example

4. Create the Nacos configuration in the example, data id: `seata.properties` , Group: `SEATA_ Group` (Seata 1.5.1 default group) configuration import [nacos configuration](https://github.com/seata/seata/blob/1.5.0/script/config-center/config.txt)
  At seata Add the following [transaction group configuration](https://seata.io/zh-cn/docs/user/configurations.html) required in the example to properties
```
   service.vgroupMapping.order-service-tx-group=default
   service.vgroupMapping.account-service-tx-group=default
   service.vgroupMapping.business-service-tx-group=default
   service.vgroupMapping.storage-service-tx-group=default
``` 
5. Start Seata Server
   Since 1.5.1, Seata supports Seata console local access console address: http://127.0.0.1:7091
   Through the Seata console, you can observe the executing transaction information and global lock information, and delete the relevant information when the transaction is completed.

### Configuration database

First, you need a MySQL database that supports the InnoDB engine.

**NOTE**: In fact, Seata supports different applications that use totally unrelated databases, but here we chose to use only one database for a simple demonstration of one principle.

Will application in the resources directory of the `account-server`, `order-service`, `storage-service` three applications. The following configuration in the yml file is modified to the actual configuration in your running environment.

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

### Create undo_ Log table

Seata AT Mode Need to use undo_ Log table.

``` $sql
-- Notice here that 0.3.0+ increases the unique index ux_ Undo_ Log
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
### Database tables needed to import seata-server DB schema
Initialize in database [global_table、branch_table、lock_table、distributed_lock](https://github.com/seata/seata/blob/1.5.0/script/server/db/mysql.sql)
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
### Create the database tables needed by the business in the example

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

### Start Seata Server This describes SpringBoot and download server in two ways

1.Run seata-server to start Seata server
The example uses Nacos as the configuration and the registry storage mode is: DB uses MySQL

2. Or click on this page GitHub, the official website of [Seata](https://github.com/seata/seata/releases ), download the latest version of Sata Server.
Enter the bin directory after unzipping and execute the following command to start with all the startup parameters optional.

```$shell
sh seata-server.sh -p $LISTEN_PORT -m $MODE(file or db) -h $HOST -e $ENV
```
-p seata-server listening service port number
-m storage mode, optional values: file, db. File is for single-point mode and DB is for HA mode. When using DB storage mode, you need to modify the database configuration of the store configuration node in the configuration and initialize [global_table, branch_table, and
Lock_ Table](https://github.com/seata/seata/blob/1.5.0/script/server/db/mysql.sql )
-h is used to solve seata-server and business side cross-network problems. The configured host value is displayed directly to the registry service available address host, which needs to be configured as public network IP or NATIP when cross-network. If both are in the same local area network, no configuration is required 
-e for multi-environment configuration center isolation   
Start Seata Server with the following command

```$shell
sh seata-server.sh -p 8091 -m file
```

**Note** If you modified the endpoint and the registry uses the default file type, remember the file you need in each of the sample projects. In the conf`file, modify the value of grouplist (when registry.type or config.type in registry.conf is file, the file name in the internal file node is read; if type is not file, the data is read directly from the registry configuration center for the corresponding metadata of the configuration type), Nacos is recommended as the configuration registry.

## Run Example

Run the Main functions of the three applications `account-server`, `order-service`, `storage-service` and `business-service`, respectively, to start the example.

After launching the example, the following URLs are accessed through the GET method of HTTP to validate scenarios where other services are invoked through RestTemplate and FeignClient in `business-service` respectively.

```$xslt
http://127.0.0.1:18081/seata/feign

http://127.0.0.1:18081/seata/rest

```

## How do I verify the success of a distributed transaction?

### Whether Xid information was successfully transmitted

In the Controller of the three services `account-server`, `order-service` and `storage-service`, the first logic executed is to output the Xid information in the RootContext. If you see that the correct Xid information is output, it changes every time and the Xid of all services in the same call is consistent. This indicates that the transfer and restore of Seata's Xid are normal.
### Consistency of data in database

In this example, we simulate a scenario where a user purchases goods, StorageService is responsible for deducting the inventory quantity, OrderService is responsible for saving the order, and AccountService is responsible for deducting the user account balance.

To demonstrate the sample, we used Random in OrderService and AcountService. NextBoolean () randomly throws exceptions, simulating a scenario in which exceptions occur randomly when a service is invoked.

If the distributed transaction is valid, then the following equation should be true


- User Original Amount (1000) = User Existing Amount + Goods Unit Price (2) * Order Quantity * Goods Quantity per Order (2)

- Initial Quantity of Goods (100) = Existing Quantity of Goods + Order Quantity * Quantity of Goods per Order (2)

## Support points for Spring Cloud

- Service providers that provide services through Spring MVC can automatically restore the Seata context when they receive HTTP requests with Seata information in the header.

- Support for automatic delivery of Seata context when service callers invoke through RestTemplate.

- Supports automatic delivery of the Seata context when a service caller invokes through a FeignClient.

- Supports scenarios where both SeataClient and Hystrix are used.

- Supports scenarios used by both SeataClient and entinel.
