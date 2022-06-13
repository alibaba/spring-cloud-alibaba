# Seata Example



##Project Description




This project demonstrates how to use Seata Starter to complete distributed transaction access for Spring Cloud applications.



[Seata] ( https://github.com/seata/seata ) is Alibaba's open source distributed transaction middleware that addresses the distributed transaction issues facing the micro-service scenario in an efficient and intrusive way.





##Preparations



Before running this example, you need to complete the following steps:



1. Configure the database



1. Create UNDO_ LOG table



1. Create the database tables needed by the business in the example



1. Import Nacos Configuration

[ https://github.com/seata/seata/blob/1.5.0/script/config-center/config.txt ]



1. Start Seata Server




###Configuration database



First, you need a MySQL database that supports the InnoDB engine.



**NOTE**: In fact, Seata supports different applications that use totally unrelated databases, but here we chose to use only one database for a simple demonstration of one principle.



Will `application'in the resources directory of the `account-server', `order-service', `storage-service` three applications. The following configuration in the yml`file is modified to the actual configuration in your running environment.



```

Mysql. Server. Ip=your MySQL server IP address

Mysql. Server. Port=your MySQL server listening port

Mysql. Db. Name=your database name for test



Mysql. User. Name=your MySQL server username

Mysql. User. Password=your MySQL server password



```



###Create undo_ Log table



[Seata AT mode]() requires undo_ Log table.



````$sql

--Notice here that 0.3.0 + increases the unique index ux_ Undo_ Log

CREATE TABLE `undo_ Log` (

`id`bigint(20) NOT NULL AUTO_ INCREMENT,

`branch_ Id` bigint(20) NOT NULL,

`xid`varchar(100) NOT NULL,

`context`varchar(128) NOT NULL,

`rollback_ Info` longblob NOT NULL,

`log_ Status` int(11) NOT NULL,

`log_ Create`datetime NOT NULL,

`log_ Modified`datetime NOT NULL,

`ext`varchar(100) DEFAULT NULL,

PRIMARY KEY (`id`),

UNIQUE KEY `ux_ Undo_ Log` (`xid`, `branch_id`)

) ENGINE=InnoDB AUTO_ INCREMENT=1 DEFAULT CHARSET=utf8;

```

Database tables needed to import seata-server DB schema

Initialize [global_table, branch_table, lock_table, distributed_lock] in the database

Click to view: https://github.com/seata/seata/blob/1.5.0/script/server/db/mysql.sql

````$sql

-------------------------------------- The script used when storeMode is'db'----------------------------------------------------------------------------------------------------------------------------------

-- the table to store GlobalSession data

CREATE TABLE IF NOT EXISTS `global_ Table`

(

`xid` VARCHAR(128) NOT NULL,

`transaction_ Id` BIGINT,

`status` TINYINT NOT NULL,

`application_ Id` VARCHAR (32),

`transaction_ Service_ Group` VARCHAR (32),

`transaction_ Name` VARCHAR (128),

`timeout` INT,

`begin_ Time` BIGINT,

`application_ Data` VARCHAR (2000),

`gmt_ Create` DATETIME,

`gmt_ Modified` DATETIME,

PRIMARY KEY (`xid`),

KEY `idx_ Status_ Gmt_ Modified` (`status`, `gmt_modified`),

KEY `idx_ Transaction_ Id` (`transaction_id`)

) ENGINE = InnoDB

DEFAULT CHARSET = utf8mb4;



-- the table to store BranchSession data

CREATE TABLE IF NOT EXISTS `branch_ Table`

(

`branch_ Id` BIGINT NOT NULL,

`xid` VARCHAR(128) NOT NULL,

`transaction_ Id` BIGINT,

`resource_ Group_ Id` VARCHAR (32),

`resource_ Id` VARCHAR (256),

`branch_ Type` VARCHAR(8),

`status` TINYINT,

`client_ Id` VARCHAR (64),

`application_ Data` VARCHAR (2000),

`gmt_ Create` DATETIME(6),

`gmt_ Modified` DATETIME(6),

PRIMARY KEY (`branch_id`),

KEY `idx_ Xid` (`xid`)

) ENGINE = InnoDB

DEFAULT CHARSET = utf8mb4;



-- the table to store lock data

CREATE TABLE IF NOT EXISTS `lock_ Table`

(

`row_ Key` VARCHAR(128) NOT NULL,

`xid` VARCHAR (128),

`transaction_ Id` BIGINT,

`branch_ Id` BIGINT NOT NULL,

`resource_ Id` VARCHAR (256),

`table_ Name` VARCHAR (32),

`pk` VARCHAR(36),

`status` TINYINT NOT NULL DEFAULT'0'COMMENT'0:locked, 1:rollbacking',

`gmt_ Create` DATETIME,

`gmt_ Modified` DATETIME,

PRIMARY KEY (`row_key`),

KEY `idx_ Status` (`status`),

KEY `idx_ Branch_ Id` (`branch_id`),

KEY `idx_ Xid_ And_ Branch_ Id` (`xid`, `branch_id`)

) ENGINE = InnoDB

DEFAULT CHARSET = utf8mb4;



CREATE TABLE IF NOT EXISTS `distributed_ Lock`

(

`lock_ Key` CHAR(20) NOT NULL,

`lock_ Value` VARCHAR(20) NOT NULL,

`expire` BIGINT,

Primary key (`lock_key`)

) ENGINE = InnoDB

DEFAULT CHARSET = utf8mb4;



INSERT INTO `distributed_ Lock` (lock_key, lock_value, expire) VALUES ('AsyncCommitting',', 0);

INSERT INTO `distributed_ Lock` (lock_key, lock_value, expire) VALUES ('RetryCommitting',', 0);

INSERT INTO `distributed_ Lock` (lock_key, lock_value, expire) VALUES ('RetryRollbacking',', 0);

INSERT INTO `distributed_ Lock` (lock_key, lock_value, expire) VALUES ('TxTimeoutCheck',', 0);

```

###Create the database tables needed by the business in the sample



````$sql

DROP TABLE IF EXISTS `storage_ Tbl`;

CREATE TABLE `storage_ Tbl` (

`id` int(11) NOT NULL AUTO_ INCREMENT,

`commodity_ Code` varchar(255) DEFAULT NULL,

`count` int(11) DEFAULT 0,

PRIMARY KEY (`id`),

UNIQUE KEY (`commodity_code`)

ENGINE=InnoDB DEFAULT CHARSET=utf8;
