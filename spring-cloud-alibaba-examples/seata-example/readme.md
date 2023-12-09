# Seata Example

## Project description

This project demonstrates how to use Seata Starter to complete the distributed transaction access of Spring Cloud Alibaba application.

[Seata](https://github.com/seata/seata) It is Alibaba's open source distributed transaction middleware, which solves the distributed transaction problems faced by micro-service scenarios in an efficient and zero-intrusion way.

## Preparations

Before you run this sample, you need to complete the following steps:

### 1. Configure the database

> Seata **Notice** actually supports disparate databases for different applications, but Mysql was chosen here for a simple demonstration of how Seata can be used in a Spring Cloud application.

Modify the following configuration in the files under the `application.yml` resources directory in the three applications `account-server`, `order-service`, `storage-service` to the database configuration in the local environment.

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

#### Create the undo _ log table

Seata AT mode requires the undo_log table.

```sql
-- Note that 0.3.0+ adds unique index ux_undo_log here
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

#### Import the database tables required by the seata-server db schema
Initializing [global_table、branch_table、lock_table、distributed_lock](https://github.com/seata/seata/blob/1.8.0/script/server/db/mysql.sql) in the database

```sql
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

#### Create the database tables required by the business in the application sample

```sql
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

### 2. Configure Nacos

> Spring Cloud Alibaba is adapted with Nacos 2.2.3. In this example, Nacos 2.2.3 is used as the configuration center component of Seata.

Create Nacos configuration for Seata: data-id: `seata.properties`, Group: `SEATA_GROUP` (default grouping for seata 1.8.0), import

Add the following configuration items required in the application example to the `seata.properties` configuration file: [事务群组配置](https://seata.io/zh-cn/docs/user/configurations.html)

```properties
   service.vgroupMapping.order-service-tx-group=default
   service.vgroupMapping.account-service-tx-group=default
   service.vgroupMapping.business-service-tx-group=default
   service.vgroupMapping.storage-service-tx-group=default
```

### 3. Start Seata-server

> Seata 1.5.1 supports console local access. Console address: http://127.0.0.1:7091, you can view the information about the transaction being executed and the global lock information through the built-in console of Seata. When the transaction is finished, the relevant information will be deleted.

#### 1. Download

Click Download [Seata 1.8.0](https://github.com/seata/seata/releases/download/v1.8.0/seata-server-1.8.0.zip) Version.

#### 2. Configure Seata-server

Modify `seata-server-1.8.0\conf\application.yml` the following configuration items in the configuration file:

- Comment `group: SEATA_GROUP`
- Add Nacos username and password

```yml
seata:
  # nacos configuration
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace:
      # group: SEATA_GROUP
      username: nacos
      password: nacos
      context-path:
      data-id: seataServer.properties
      ##if use MSE Nacos with auth, mutex with username/password attribute
      #access-key:
      #secret-key:
  registry:
    # nacos configuration
    type: nacos
    nacos:
      application: seata-server
      server-addr: 127.0.0.1:8848
      # group: SEATA_GROUP
      namespace:
      cluster: default
      username: nacos
      password: nacos
```

> **Notice**
> Nacos 2.2.3 enables authentication. Configuration `username` and `password` properties are required, otherwise login fails. For more Nacos 2.2.3 related configurations, refer to `nacos-example`.
> **The Nacos service registration group when seata-server is started must be consistent with the group in the sample application, otherwise an error that seata-server cannot be found will occur!**
> For more information about the configuration of Seata-server with Nacos as the configuration center, please refer to https://seata.io/zh-cn/docs/ops/deploy-by-docker-compose/#nacos-db.

### 3. Start Seata-server

Windows:

```cmd
./seata-server.bat
```

Linux/Mac

```shell
sh seata-server.sh
```

For more configuration startup parameters, please refer to https://seata.io/zh-cn/docs/user/quickstart/#%E6%AD%A5%E9%AA%A4-4-%E5%90%AF%E5%8A%A8%E6%9C%8D%E5%8A%A1.

**Notice** If you change the endpoint and the registry uses the default file type, remember that in the `file.conf` file in each sample project, Modify the value of grouplist (when the registry. Type or config. Type in the registry. Conf is file, the file name in the internal file node will be read. If the type is not file, the data will be directly read from the registration configuration center of the corresponding metadata of the configuration type. It is recommended to use nacos as the configuration registration center.

## Run the sample

Start the sample by running `account-server` the Main functions of the, `order-service`, `storage-service`, and `business-service` applications separately.

After starting the sample, access the following URL through the GET method of HTTP to verify `business-service` the scenarios of calling other services through RestTemplate and FeignClient in respectively.

```shell
http://127.0.0.1:18081/seata/feign

http://127.0.0.1:18081/seata/rest
```

When a service interface is invoked, two types of returns are possible

1. SUCCESS: calling interface service succeeded;
2. 500 exception, business-service mock exception.

## How do I verify that a distributed transaction is successful?

### Xid information passed successfully

In `account-server` the Controllers of, `order-service`, and `storage-service` services, the first logic to be executed is to output the Xid information in the RootContext. If the correct Xid information is output, that is, it changes every time. And that Xid of all the services in the same invocation are the same. Then it indicates that the passing and restoring of Seata's Xid is normal.

### Whether the data in the database is consistent

In this example, we simulate a scenario in which a user purchases goods. The Storage Service is responsible for deducting the inventory quantity, the Order Service is responsible for saving the order, and the Account service is responsible for deducting the balance of the user's account.

To demonstrate the sample, we use Random. NextBoolean () to randomly throw exceptions in Order Service and AccountService, simulating a scenario where exceptions randomly occur during service invocation.

If a distributed transaction is in effect, then the following equation should hold

- User's original amount (1000) = user's existing amount + unit price of goods (2) *Number of orders* quantity of goods per order (2)

- Initial quantity of goods (100) = Quantity on hand of goods + Order quantity * Quantity of goods per order (2)

## Support points for Spring Cloud

- Service providers that provide services through Spring MVC can automatically restore the Seata context when they receive an HTTP request with Seata information in the header.

- Support the automatic passing of the Seata context when the service caller invokes through the RestTemplate.

- Support the automatic passing of the Seata context when the service caller calls through FeignClient.

- Scenarios where SeataClient and Hystrix are used together are supported.

- Scenarios where SeataClient and Sentinel are used together are supported.
