# Seata Example

## Project Instruction


This project demonstrates how to use Seata Starter to complete distributed transaction access for Spring Cloud applications.

[Seata](https://github.com/seata/seata) is Alibaba's open source distributed transaction middleware that solves distributed transaction problems in microservice scenarios in an efficient and 0-intrusive way.



## Preparation

Before running this example, you need to complete the following preparation steps.

1. Configure the database

2. Create the `UNDO_LOG` table

3. Create the database tables required by the business in the example

4. Start Seata Server


### Configure the database

First, you need to have a MySQL database that supports the `InnoDB` engine.

**Note**: Seata actually supports different applications using completely unrelated databases, but here we have chosen to use only one database in order to simply demonstrate a principle.

Change the following configuration in the `application.properties` file in the `resources` directory of the `account-server`, `order-service`, and `storage-service` applications to the actual configuration in your runtime environment.

```
mysql.server.ip=your mysql server ip address
mysql.server.port=your mysql server listening port
mysql.db.name=your database name for test

mysql.user.name=your mysql server username
mysql.user.password=your mysql server password

```

### Create `undo_log` table

The [Seata AT schema]() needs to use the undo_log table.

``` $sql
-- Note that here 0.3.0+ adds unique index `ux_undo_log`
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

### Create the database tables required by the business in the example

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

### Start Seata Server

Click on this page [https://github.com/seata/seata/releases](https://github.com/seata/seata/releases) to download the latest version of Seata Server.


Go to the bin directory after unpacking and execute the following command to start it, all startup parameters are optional.

```$shell
sh seata-server.sh -p $LISTEN_PORT -m $MODE(file or db) -h $HOST -e $ENV
```
`-p` seata-server listen to the service port number   
`-m` storage mode, optional values: file, db. file is used for single point mode, db is used for ha mode, when using db storage mode, you need to modify the database configuration of the store configuration node in the configuration, and also initialize [global_table, branch_table and
lock_table](https://github.com/seata/seata/blob/develop/server/src/main/resources/db_store.sql)   
`-h` is used to solve the `seata-server` and business side of the cross-network problem, its configuration of the host value directly to the service available in the registration center host, when the cross-network here need to be configured as a public IP or NATIP, if all in the same LAN is not required to configure   
`-e` is used to solve the multi-environment configuration center isolation problem   

In this example, the following command is used to start Seata Server

```$shell
sh seata-server.sh -p 8091 -m file
```

**Note** If you have modified the endpoint and the registry uses the default file type, remember to change the value of grouplist in the `file.conf` file in each example project (the registry.type or config.type in registry.conf will be read when it is file). If the type is not file, the data will be read directly from the registration configuration center of the corresponding metadata of the configuration type), it is recommended to use nacos as the configuration registration center.


## Run example

Run the Main function of `account-server`, `order-service`, `storage-service` and `business-service` respectively to start the example.

After starting the example, access the following two URLs via the `GET` method of HTTP to verify the scenario of calling other services in `business-service` via RestTemplate and FeignClient, respectively.

```$xslt
http://127.0.0.1:18081/seata/feign

http://127.0.0.1:18081/seata/rest
```

## How to verify the success of a distributed transaction?

### Check if the Xid information was passed successfully

In the Controller of `account-server`, `order-service` and `storage-service` services, the first logic executed is to output the Xid information in the RootContext, and if we see that the correct Xid information is output, i.e., it changes every time and the Xid of all services in the same. If you see that the correct Xid information is output, i.e., it changes every time and the Xid of all services in the same call is the same. If you see that the Xid information in the RootContext is correct, i.e., it changes every time, and the Xid of all the services in the same call is the same, then the Xid of Seata is passed and restored properly.

### Check if the data in the database is consistent

In this example, we simulate a scenario where a user purchases goods, the StorageService is responsible for deducting the inventory quantity, the OrderService is responsible for saving the order, and the AccountService is responsible for deducting the user's account balance.

To demonstrate the sample, we use Random.nextBoolean() in OrderService and AccountService to throw a random exception, simulating a random exception occurring during a service call.

If the distributed transaction is in effect, then the following equation should hold

- `Original amount of the user (1000)` = `existing amount of the user` + `unit price of the goods (2)` * `number of orders` * `number of goods per order (2)`

- `Initial quantity of goods (100)` = `existing quantity of goods` + `number of orders` * `number of goods per order (2)`

## Compatible with the Spring Cloud ecosystem

- Service providers that provide services through `Spring MVC` can automatically restore `Seata` context when they receive an HTTP request with `Seata` information in the header.

- Support for service callers to automatically pass `Seata` contexts when called via RestTemplate.

- Supports automatic passing of `Seata` context when called by a service caller via FeignClient.

- Supports scenarios where `SeataClient` and `Sentinel` are used together.
