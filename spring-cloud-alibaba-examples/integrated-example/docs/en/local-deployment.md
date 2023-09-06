# Spring Cloud Alibaba Containerized Deployment Best Practices | Local Deployment Edition

## Preparation

### Environment Declaration

Before running the local example, you need to ensure that the local machine has the following basic environment. If you do not have the current local environment, the following steps to demonstrate the construction process.
You can also quickly launch the component through the docker-compose file provided by the Spring Cloud Alibaba (SCA) community.

- Nacos server
- Seata server
- RocketMQ server
- MySQL server

### Component Service Versions

For each component version of this project, please go to the release page of each community to download and decompression run.

- [Nacos: version 2.1.0](https://github.com/alibaba/nacos/releases)
- [Seata: version 1.5.1](https://github.com/seata/seata/releases)
- [RocketMQ: version 4.9.4](https://github.com/apache/rocketmq/releases)
- MySQL: version 5.7

### Hosts configuration

To ensure that the code can start properly, please configure the local host mapping first, add the following mapping to the configuration file.

```shell
# for integrated-example
127.0.0.1 integrated-mysql
127.0.0.1 nacos-server
127.0.0.1 seata-server
127.0.0.1 rocketmq
127.0.0.1 gateway-service
127.0.0.1 integrated-frontend
```

### Database configuration

Before you start the database configuration, please make sure the MySQL server is on.

#### Initialize business tables

For the first scenario, the order, account, and inventory microservices all need their own databases, while the second scenario simulates a database for storing like information as well.

Run the sql script `spring-cloud-alibaba-examples/integrated-example/config-init/sql/init.sql` to create the environment required for the business and the Seata-related tables in one click.

### Nacos Configuration

At this point, the database services are configured and you need to configure the Nacos configuration center for all the microservice configuration files.

#### Nacos startup

For the sake of example, here we use the ``standalone`` mode of Nacos, go to the unpacked directory of Nacos and execute the following command.

```shell
#Linux/Mac environment
sh bin/startup.sh -m standalone
#If you are in Ubuntu and the above command gives you an error [[symbol not found, you can run the following command
bash bin/startup.sh -m standalone
#Win environment
. \bin\startup.cmd -m standalone
````

#### Adding configuration files

Before bulk importing the configuration, please modify the datasource configuration (username and password) in `spring-cloud-alibaba-examples/integrated-example/config-init/config/datasource-config.yaml`.

After that, run `spring-cloud-alibaba-examples/integrated-example/config/scripts/nacos-config-quick.sh` to complete the one-click import of all microservice configurations.

```shell
# linux
sh nacos-config-quick.sh
# windows can use git bash to import the configuration, run the command as above
```

### Seata Configuration

After the Nacos service registry and configuration center are deployed, here is the configuration of the Seata server.

Seata's db mode requires additional configuration of database information and modification of the Seata Server configuration file, and the configuration file has been merged in the new version compared to the old version, so for demonstration purposes, Seata Server is started in `file` mode on Seata standalone.

#### Start Seata Server

Go to the seata directory after the release and execute the following command.

```shell
#Linux/Mac environment
sh . /bin/seata-server.sh
#Win environment
bin\seata-server.bat
```

### RocketMQ configuration

After the Seata service starts, you can start the RocketMQ NameServer and Broker services.

Go to the unpacked rocketmq directory after the release and execute the following command.

#### Start the NameServer

```shell
#Linux/Mac environment
sh bin/mqnamesrv
#Win environment
. \bin\mqnamesrv.cmd
```

#### Start Broker

```shell
#Linux/Mac environment
sh bin/mqbroker
#Win environment
. \bin\mqbroker.cmd
```

## Run the demo example

After the preparation work is done, you can run the demo, mainly according to different usage scenarios, you can experience the user order (distributed transaction capability) and simulate the high traffic point (meltdown and limit the flow as well as the ability to cut the peak and fill the valley) respectively.

First, you need to start the `integrated-frontend` and `integrated-gateway` projects separately.

- `integrated-frontend` module is front page for best practice examples.
- `integral-gateway` module is the gateway for the entire best practice example.

### Distributed Transaction Capabilities

#### Scenario Description

For the distributed transaction capability, we provide the scenario **where a user places an order for goods** and after placing the order.

- First request the inventory module and deduct the inventory
- Deduct the account balance
- Generate order information to return a response

##### Start test

Start `integrated-storage`,`integrated-account`,`integrated-order` microservices respectively.

Visit `http://integrated-frontend:8080/order` to experience the corresponding scenario.

By clicking the order button directly to submit the form, application simulate the client sending a request to the gateway to create an order.

- The user's userId is admin
- The item number of the user's order is 1
- The number of items purchased in this order is 1

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155416524.png)

In this demo example, the unit price of each item is 2 for demonstration purposes.

And in the previous preparation, **initialize business database table** application created a new user, the user's userId is admin with a balance of $3, and a new item numbered 1 with 100 units in stock.

So by doing the above, we will create an order, deduct the number of items in stock corresponding to item number 1 (100-1=99), and deduct the balance of the admin user (3-2=1).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155429801.png)

If the same interface is requested again, again the inventory is deducted first (99-1=98), but an exception is thrown because the admin user's balance is insufficient and is caught by Seata, which performs a two-stage commit of the distributed transaction and rolls back the transaction.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155436112.png)

You can see that the database still has 99 records in stock because of the rollback.

### Fused flow limiting, peak shaving capability

#### Scenario Description

For service fusion limiting and peak and valley cutting in the context of high traffic, SCA community provide a scenario **where users make likes for products**. In this scenario, we provide two ways to deal with high traffic.

- Sentinel binds specified gateway routes on the gateway side for fusion degradation of services.
- RocketMQ performs traffic clipping, where the producer sends messages to RocketMQ under high traffic requests, while the consumer pulls and consumes through a configurable consumption rate, reducing the pressure of high traffic direct requests to the database to increase the number of likes requests.

#### Startup test

Start the `integrated-praise-provider` and `integrated-praise-consumer` modules separately.

- Sentinel service meltdown degradation

Visit `http://integrated-frontend:8080/sentinel` to experience the corresponding scenario.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155501290.png)

The Gateway routing point service has a flow limit rule of 5, while 10 concurrent requests are simulated on the front end through asynchronous processing.

Therefore, we can see that Sentinel performs a service fusion on the Gateway side to return the fallback to the client for the extra traffic, while the number of likes in the database is updated (+5).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914155755103.png)

- RocketMQ is performing peak and valley reduction

Visit `http://integrated-frontend:8080/rocketmq` to experience the corresponding scenario.

Since previously configured the consumption rate and interval of the `integrated-praise-consumer` consumer module in Nacos, simulate 1000 requests for likes at the click of a button, and the `integrated-praise-provider`
will deliver 1000 requests to the Broker, and the consumer module will consume them according to the configured consumption rate, and update the database with the product data of the likes, simulating the characteristics of RocketMQ to cut the peaks and fill the valleys under high traffic.

You can see that the number of likes in the database is being dynamically updated.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016173604059.png)

## Other

This example **is just a selection of typical features for each component to serve the application scenario**.

If you are interested or want to go deeper, you are welcome to study the individual example documentation for each component.

- Nacos examples
  - [Nacos config example](../../../nacos-example/nacos-config-example/readme.md)
  - [Nacos discovery example](../../../nacos-example/nacos-discovery-example/readme.md)
- [Sentinel core example](../../../sentinel-example/sentinel-core-example/readme.md)
- [Seata example](../../../seata-example/readme.md)
- [RocketMQ example](../../rocketmq-example/readme.md)
