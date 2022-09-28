## Spring Cloud Alibaba Containerized Deployment Best Practices | Local Deployment Version

## Preparation

### Environment declaration

Before running the local example, you need to ensure that the following base environment is available locally. If you do not have a current environment locally, the following step-by-step build will demonstrate the build process.

- Nacos server
- Seata server
- RocketMQ server
- MySQL server

### Component Service Versions

To download and unpack the component versions of this project, please go to the release pages of the respective communities.

- [Nacos: version 2.1.0](https://github.com/alibaba/nacos/releases)
- [Seata: version 1.5.1](https://github.com/seata/seata/releases)
- [RocketMQ: version 4.9.4](https://github.com/apache/rocketmq/releases)
- MySQL: version 5.7

### Database configuration

The following starts the local environment build preparation, before the database configuration starts, please make sure that the MySQL server is turned on.

#### Initializing Business Tables

For the first scenario, the orders, accounts and inventory microservices all need their own databases, while the second scenario simulating likes also needs a database to store the likes information.

Run the sql script `spring-cloud-alibaba-examples/integrated-example/sql/init.sql` to create the environment required for the business and the Seata-related tables in one click.

### Nacos configuration

Now that the database services are configured, you need to configure the Nacos configuration centre with all the microservice configuration files.

#### Nacos startup

For the purpose of this example, Nacos is started in ``standalone'' mode. Go to the Nacos unpacked directory and execute the following command.

```sh
#Linux/Mac environment
sh bin/startup.sh -m standalone
#If you are in Ubuntu and the above command gives you an error saying [[symbol not found, you can run the following command
bash bin/startup.sh -m standalone
#Win environment
. \bin\startup.cmd -m standalone
```

#### Adding configuration files

Before bulk importing the configuration, change the datasource configuration (username and password) in `integrated-example/config/datasource-config.yaml`.

Afterwards, run `spring-cloud-alibaba-examples/integrated-example/scripts/nacos-config-quick.sh` to complete the one-click import of all microservice configurations.

### Seata configuration

Once the Nacos service registry and configuration centre have been deployed, here is the configuration of the Seata server.

Seata's db mode requires additional configuration of the database information and modification of the Seata Server configuration file, which has been merged in the new version compared to the old one.

#### Start Seata Server

Go to the seata directory after the release and execute the following command.

```sh
#Linux/Mac environment
sh . /bin/seata-server.sh
#Win environment
bin\seata-server.bat
```

### RocketMQ configuration

Once the Seata service is started, you can start the RocketMQ NameServer and Broker services.

Go to the unpacked rocketmq directory after the release and execute the following command.

#### Start the NameServer

```sh
#Linux/Mac environment
sh bin/mqnamesrv
#Win environment
. \bin\mqnamesrv.cmd
```

#### Start Broker

```sh
#Linux/Mac environment
sh bin/mqbroker
#Win environment
. \bin\mqbroker.cmd
```

## Run the demo

After the preparation work is done, you can run the demo to experience the user order (distributed transaction capability) and simulate the high traffic volume (meltdown limit and peak shaving capability) depending on different usage scenarios.

The first step is to start the `integrated_frontend` and `integrated_gateway` projects respectively.

- The gateway module is the gateway to the entire best practice instance.
- frontend is the simple front-end page for the best practice.

### Distributed transaction capability

#### scenario description

For the distributed transaction capability, we provide a scenario **where a user places an order for goods** and after placing the order.

- First request the inventory module and deduct the inventory
- Deducts the account balance
- Generate order information to return a response

##### start test

Start the `integrated_storage`, `integrated_account`, `integrated_order` microservices separately.

Visit `http://127.0.0.1:8080/order` to experience the corresponding scenario.

By clicking directly on the order button to submit the form, we simulate the client sending a request to the gateway to create an order.

- The user's userId is admin
- The user places an order for item number 1
- The number of items purchased in this order is 1

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914153234414.png)

In this demo, for demonstration purposes, the unit price of each item is 2.

In the previous preparation, when **initialising the business database table** we created a new user userId = admin with a balance of $3 and a new item numbered 1 with 100 items in stock.

So by doing the above, we create an order, deducting the number of units in stock for item number 1 (100-1=99) and deducting the balance of the admin user (3-2=1).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914153126925.png)

If the same interface is requested again, again the stock is deducted first (99-1=98), but an exception is thrown because the admin user's balance is insufficient and is caught by Seata, which performs a two-stage commit of the distributed transaction and rolls back the transaction.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914153313127.png)

You can see that the database still has 99 records in stock because of the rollback.

### Melt limit, peak shaving capability

#### Scenario description

For service fusion limiting in the context of high traffic and peak and valley reduction, we provide a scenario where **users like an item**. In this scenario, we provide two ways to deal with high traffic.

- Sentinel binds a specified gateway route on the gateway side for fusion degradation of the service.
- RocketMQ performs traffic clipping, where the producer sends messages to RocketMQ during high traffic requests, while the consumer pulls and consumes at a configurable consumption rate, reducing the pressure of high traffic direct requests to the database to increase the number of likes requested.

#### startup tests

Start the `integrated_provider` and `integrated_consumer` modules separately.

- Sentinel service meltdown downgrade

Visit `http://127.0.0.1:8080/sentinel` to experience the corresponding scenario.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914155720469.png)

The Gateway routing point service has a flow limit rule of 5, while 10 concurrent requests are simulated on the front-end through asynchronous processing.

So you can see that Sentinel is fusing the service on the Gateway side to return a fallback to the client for the extra traffic, while the number of likes in the database is updated (+5).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914155755103.png)

- RocketMQ does peak and valley reduction

Visit `http://127.0.0.1:8080/rocketmq` to experience the corresponding scenario.

As we have previously configured the consumption rate and interval of the `integrated-consumer` consumer module in Nacos, we simulate 1000 requests for likes at the click of a button, and for 1000 requests for likes, the `integrated_provider`
In the consumer module, we consume at the configured consumption rate and update the database with the product data, simulating the RocketMQ peak-shaving feature in heavy traffic.

You can see that the number of likes in the database is being dynamically updated.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220914155815191.png)

## Other

This example **is just a selection of the typical features of each component to serve the application scenario**.

Of course, there is more to each component than what is demonstrated in the best practices, so if you are interested or want to go deeper, you are welcome to study the individual example documentation for each component.

- Nacos Examples
    - [nacos-config-example](../../nacos-example/nacos-config-example/readme-zh.md)
    - [nacos-discovery-example](../../nacos-example/nacos-discovery-example/readme-zh.md)
- [Sentinel-Core-Example](../../sentinel-example/sentinel-core-example/readme-zh.md)
- [Seata Examples](../../seata-example/readme-zh.md)
- [RocketMQ Example](../../rocketmq-example/readme-zh.md)