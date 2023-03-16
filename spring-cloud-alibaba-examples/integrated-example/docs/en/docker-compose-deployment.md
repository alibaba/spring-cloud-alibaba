# Spring Cloud Alibaba Containerized Deployment Best Practices | Docker-Compose Edition

## Preparation

If you have not installed Docker or Docker-Compose, please follow the official documentation to build the environment

> Note: When using Docker-Compose to experience the demo, please make sure that the local machine memory resource is >= 24G!

- Docker：https://docs.docker.com/desktop/install/linux-install/
- Docker-Compose：https://docs.docker.com/compose/install/

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

### Preparing jar packages

Go to the `spring-cloud-alibaba-examples` directory and run the `mvn package` command to compile the project and generate the jar package, so as to prepare for the subsequent construction of the docker service image.

## Quickly start 

### Component start

Enter `spring-cloud-alibaba-examples/integration-example` directory, run the following command in the terminal to quickly deploy the components required to run example: `docker-compose -f ./docker-compose/docker-compose-env.yml up -d`.

### Add configuration

After docker-compose-env.yml is run successfully, add the Nacos configuration:

- Enter `spring-cloud-alibaba-examples/integration-example` directory;
- Execute the `config-init/scripts/nacos-config-quick.sh` script file in the terminal.

The one-click import of all micro-service configurations is complete.

> Note: windows operating systems can use `git bash` to execute shell script files to complete the configuration import.

### Service start

Enter `spring-cloud-alibaba-examples/integration-example` directory, Run the following command in the terminal to quickly deploy the services required for running example: `docker-compose -f ./docker-compose/docker-compose-service.yml up -d`.

## Stop all containers

### Stops the service container

Enter `spring-cloud-alibaba-examples/integration-examplee` directory, Run the following command in the terminal to `docker-compose -f ./docker-compose/docker-compose-service.yml down` to stop the running example service container.

### Stops the component container

Enter `spring-cloud-alibaba-examples/integration-example` directory, Run the following command in the terminal to `docker-compose -f ./docker-compose/docker-compose-env.yml down` to stop the running example component container.

> When the container starts, you can observe the startup process of the container through `docker-compose- f docker-compose-*.yml up`!

## Experience Demo

After the preparation work is done, you can run the demo, mainly according to different usage scenarios, you can experience the user order (distributed transaction capability) and simulate the high traffic point (meltdown and limit the flow as well as the ability to cut the peak and fill the valley) respectively.

First, you need to start the `integrated-frontend` and `integrated-gateway` projects separately.

- The gateway module is the gateway for the entire best practice example.
- frontend is the simple front-end page for the best practice.

### Distributed Transaction Capabilities

#### Scenario Description

For the distributed transaction capability, we provide the scenario **where a user places an order for goods** and after placing the order.

- First request the inventory module and deduct the inventory
- Deduct the account balance
- Generate order information to return a response

##### Start test

Start `integrated-storage`,`integrated-account`,`integrated-order` microservices respectively.

Visit `http://integrated-frontend:8080/order` to experience the corresponding scenario.

By clicking the order button directly to submit the form, we simulate the client sending a request to the gateway to create an order.

- The user's userId is admin
- The item number of the user's order is 1
- The number of items purchased in this order is 1

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155416524.png)

In this demo example, the unit price of each item is 2 for demonstration purposes.

And in the previous preparation, **initialize business database table** we created a new user userId = admin with a balance of $3, and a new item numbered 1 with 100 units in stock.

So by doing the above, we will create an order, deduct the number of items in stock corresponding to item number 1 (100-1=99), and deduct the balance of the admin user (3-2=1).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155429801.png)

If the same interface is requested again, again the inventory is deducted first (99-1=98), but an exception is thrown because the admin user's balance is insufficient and is caught by Seata, which performs a two-stage commit of the distributed transaction and rolls back the transaction.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016155436112.png)

You can see that the database still has 99 records in stock because of the rollback.

### Fused flow limiting, peak shaving capability

#### Scenario Description

For service fusion limiting and peak and valley cutting in the context of high traffic, we provide a scenario **where users make likes for products**. In this scenario, we provide two ways to deal with high traffic.

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

Since we previously configured the consumption rate and interval of the `integrated-praise-consumer` consumer module in Nacos, we simulate 1000 requests for likes at the click of a button, and the `integrated-praise-provider`
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
