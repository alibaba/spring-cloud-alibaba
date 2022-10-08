# Spring Cloud Alibaba Containerized Deployment Best Practices | Kubernetes Helm-Chart Edition

## Preparation

This is the Spring Cloud Alibaba Best Practices Kubernetes deployment version, which requires you to prepare the following environment

- Kubernetes (we recommend using Docker Desktop's built-in integrated Kubernetes environment for this experience)
- Helm

If you don't have the above environment, please go to the official documentation to build the environment

- [Helm Installation](https://helm.sh/zh/docs/intro/install/)
- [Kubernetes Docker Desktop Quick Install](https://docs.docker.com/desktop/kubernetes/)

## Start testing

Go to the `spring-cloud-alibaba-examples/integrated-example` directory and execute the following command to complete the Helm installation
```shell
helm package helm-chart

helm install integrated-example integrated-example-1.0.0.tgz
```
With the above command we were able to deploy the best practice project via Helm with one click based on the Helm Chart documentation provided by the project

You can check the deployment status of each container resource through the `kubectl` command provided by Kubernetes, and wait patiently for **all containers to finish starting** to experience the usage scenarios and capabilities of each component on the corresponding page

If you want to stop the experience, enter the following command
```shell
helm uninstall integrated-example
```

### Distributed transaction capabilities

#### Scenario Description

For the distributed transaction capability, we provide a scenario **where a user places an order to purchase goods** and after placing the order.

- First request the inventory module and deduct the inventory
- Deduct the account balance
- Generate order information to return a response

##### start test


Visit `http://127.0.0.1:30080/order` to experience the corresponding scenario.

By clicking the order button directly to submit the form, we simulate the client sending a request to the gateway to create an order.

- The user's userId is admin
- The item number of the user's order is 1
- The number of items purchased in this order is 1

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221008112011327.png)

In this demo example, the unit price of each item is 2 for demonstration purposes.

And in the previous preparation, **initialize business database table** we created a new user userId = admin with a balance of $3, and a new item numbered 1 with 100 units in stock.

So by doing the above, we will create an order, deduct the number of items in stock corresponding to item number 1 (100-1=99), and deduct the balance of the admin user (3-2=1).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221008111903019.png)

If the same interface is requested again, again the inventory is deducted first (99-1=98), but an exception is thrown because the admin user's balance is insufficient and is caught by Seata, which performs a two-stage commit of the distributed transaction and rolls back the transaction.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221008111924467.png)

You can see that the database still has 99 records in stock because of the rollback.

### Fused flow limiting, peak shaving capability

#### Scenario Description

For service fusion limiting and peak and valley cutting in the context of high traffic, we provide a scenario** where users make likes for products**. In this scenario, we provide two ways to deal with high traffic.

- Sentinel binds specified gateway routes on the gateway side for fusion degradation of services.
- RocketMQ performs traffic clipping, where the producer sends messages to RocketMQ under high traffic requests, while the consumer pulls and consumes through a configurable consumption rate, reducing the pressure of high traffic direct requests to the database to increase the number of likes requests.

#### startup test


- Sentinel service meltdown degradation

Visit `http://127.0.0.1:30080/sentinel` to experience the corresponding scenario.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221008112154213.png)

The Gateway routing point service has a flow limit rule of 5, while 10 concurrent requests are simulated on the front end by asynchronous processing.

Therefore, we can see that Sentinel performs a service fusion on the Gateway side to return the fallback to the client for the extra traffic, while the number of likes in the database is updated (+5).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221008112036924.png)

- RocketMQ is performing peak and valley reduction

Visit `http://127.0.0.1:30080/rocketmq` to experience the corresponding scenario.

Since we previously configured the consumption rate and interval of the `integrated-consumer` consumer module in Nacos, we simulate 1000 requests for likes at the click of a button, and the `integrated_provider`
will deliver 1000 requests to the Broker, and the consumer module will consume them according to the configured consumption rate, and update the database with the product data of the likes, simulating the characteristics of RocketMQ to cut the peaks and fill the valleys under high traffic.

You can see that the number of likes in the database is being dynamically updated.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221008112225839.png)

## Other

This example **is just a selection of typical features for each component to serve the application scenario**.

If you are interested or want to go deeper, you are welcome to study the separate example documentation for each component.

- Nacos Examples
  - [nacos-config-example](../../nacos-example/nacos-config-example/readme-zh.md)
  - [nacos-discovery-example](../../nacos-example/nacos-discovery-example/readme-zh.md)
- [Sentinel-Core-Example](../../sentinel-example/sentinel-core-example/readme-zh.md)
- [Seata Examples](../../seata-example/readme-zh.md)
- [RocketMQ Example](../../rocketmq-example/readme-zh.md)