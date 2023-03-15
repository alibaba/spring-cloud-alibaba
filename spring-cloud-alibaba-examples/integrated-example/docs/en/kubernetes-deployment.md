# Spring Cloud Alibaba Containerized Deployment Best Practices | Kubernetes Helm-Chart Edition

## Preparation

This is the Spring Cloud Alibaba (hereinafter referred to as SCA) Best Practices Kubernetes deployment version, which requires you to prepare the following environment.

- Kubernetes (We recommend using Docker Desktop's built-in integrated Kubernetes environment for this experience.)
- Helm

If the test machine does not already have the above environment, please go to the official documentation to build the environment.

- [Helm Installation](https://helm.sh/zh/docs/intro/install/)
- [Kubernetes Docker Desktop Quick Installation](https://docs.docker.com/desktop/kubernetes/)

Here expose the services of the Pod in Kubernetes to the outside world by means of NodePort, and configure the ip mapping of the Kubernetes cluster node before starting the test.
```shell
# Please adjust with the public ip of your K8S node
120.24.xxx.xxx integrated-frontend
120.24.xxx.xxx gateway-service
120.24.xxx.xxx integrated-mysql-web
120.24.xxx.xxx nacos-mysql-web
120.24.xxx.xxx nacos-svc
```

## Start the test

Go to the ``spring-cloud-alibaba-examples/integrated-example`` directory and execute the following command to deploy the application using Helm.
```shell
helm package helm-chart

helm install integrated-example integrated-example-1.0.0.tgz
```
By running the above command, quickly deploy the best practice example through Helm according to the Helm Chart documentation provided by the SCA community.

You can check the deployment status of each container resource through the `kubectl` command provided by Kubernetes, and wait patiently for **all containers to finish starting** to experience the usage scenarios and capabilities of each component on the corresponding page.

If you want to stop the experience, enter the following command.
```shell
helm uninstall integrated-example
```

### Distributed Transaction Capabilities

#### Scenario Description

For the distributed transaction capability, SCA community provide a scenario **where a user places an order to purchase goods** and after placing the order.

- First request the inventory module and deduct the inventory
- Deduct the account balance
- Generate order information to return a response

##### Start test

Visit `http://integrated-frontend:30080/order` to experience the corresponding scenario.

By clicking directly on the order button to submit the form, simulate the client sending a request to the gateway to create an order.

- The user's userId is admin
- The user places an order with item number 1
- The number of items purchased in this order is 1

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143033445.png)

In this demo example, the unit price of each item is 2 for demonstration purposes.

While initializing the `integrated-mysql` container, **initializing the business database table** creates a new user, the user's userId is admin, with a balance of $3; and a new item numbered 1 with 100 units in stock.
So by doing the above, application will create an order, deduct the number of items in stock corresponding to item number 1 (100-1=99), and deduct the balance of the admin user (3-2=1).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143057730.png)

If the same interface is requested again, again the inventory is deducted first (99-1=98), but an exception is thrown because the admin user's balance is insufficient and is caught by Seata, which performs a two-stage commit of the distributed transaction and rolls back the transaction.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143104810.png)

You can see that the database still has 99 records in stock because of the rollback.

### Fused flow limiting, peak shaving capability

#### Scenario Description

For service fusion limiting and peak and valley cutting in the context of high traffic, SCA community provide a scenario** where users make likes for products**. In this scenario, we provide two ways to deal with high traffic.

- Sentinel binds specified gateway routes on the gateway side for fusion degradation of services.
- RocketMQ performs traffic clipping, where the producer sends messages to RocketMQ under high traffic requests, while the consumer pulls and consumes through a configurable consumption rate, reducing the pressure of high traffic direct requests to the database to increase the number of likes requests.

#### Startup test

- Sentinel Service Meltdown Degradation

Visit `http://integrated-frontend:30080/sentinel` to experience the corresponding scenario.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143120697.png)

The Gateway routing point service has a flow limit rule of 5, while 10 concurrent requests are simulated on the front end through asynchronous processing.

Therefore, we can see that Sentinel performs a service fusion on the Gateway side to return the fallback to the client for the extra traffic, while the number of likes in the database is updated (+5).

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143203773.png)

- RocketMQ is performing peak and valley reduction

Visit `http://integrated-frontend:30080/rocketmq` to experience the corresponding scenario.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143342664.png)

Since the consumption rate and interval of the `integrated-praise-consumer` consumer module is configured in Nacos before, the application will simulate 1000 "like" requests when clicking the button, `integrated-praise-provider`
will deliver 1000 requests to the Broker, and the consumer module will consume them according to the configured consumption rate, and update the database with the product data of the likes, simulating the characteristics of RocketMQ to cut the peaks and fill the valleys under high traffic.

You can see that the number of likes in the database is being dynamically updated.

![](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20221016143352619.png)

## Other

This example **is just a selection of typical features for each component to serve the application scenario**.

Of course, there is more to each component than just what is demonstrated in the best practices, so if you are interested or want to go deeper, feel free to read the individual example documentation for each component.

- Nacos examples
  - [Nacos config example](../../../nacos-example/nacos-config-example/readme.md)
  - [Nacos discovery example](../../../nacos-example/nacos-discovery-example/readme.md)
- [Sentinel core example](../../../sentinel-example/sentinel-core-example/readme.md)
- [Seata example](../../../seata-example/readme.md)
- [RocketMQ example](../../rocketmq-example/readme.md)