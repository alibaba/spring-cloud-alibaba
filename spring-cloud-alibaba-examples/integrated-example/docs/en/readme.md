# Integrated Example

## Project Description

This project is a demo of Spring Cloud Alibaba (hereinafter referred to as SCA) containerized deployment best practices, and is an example project integrating SCA components (Nacos, Sentinel, Seata, RocketMQ).

The main components used and their usage features are as follows.

- Spring Cloud Gateway:gateway
- Nacos:configuration centre and service registry
- Sentinel:fusion flow limiting
- Seata:Distributed Transactions
- RocketMQ:message queues for peak and valley reduction
- Docker:Microservices Containerized Deployment
- Kubernetes Helm Chart

![Overall Overview](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220816004541921.png)

## Application Scenario Description

In this demo, SCA community provide two business scenarios.

1) A scenario where a user places an order for goods and after placing the order.

- First request the inventory module to deduct the inventory

- Deduct the account balance

- Generate order information and return a response

2) The user likes the goods (simulating the producer-consumer application scenario of MQ) and returns the details (number of likes, etc.) after the goods have been liked.

### Detailed description of the component

1) In which the scenario where the user places an order for the goods mainly uses Seata to perform distributed transactions to represent the capabilities.

2) The scenario where the user likes a product simulates a high traffic environment with Sentinel for flow limiting or RocketMQ for peak shaving. In this scenario, SCA community provide two ways to deal with high traffic.

- Sentinel binds a specified gateway route on the gateway side for service fusion degradation.
- RocketMQ performs peak-shaving, where producers send messages to RocketMQ and consumers pull and consume at configurable consumption rates, reducing the pressure of high traffic direct requests to the database and increasing the number of likes.

#### SpringCloud Gateway

A gateway to the microservices module.

Spring Cloud GateWay integrates with Nacos, enabling dynamic routing configuration.

By listening for changes to the Nacos configuration, the service gateway routing configuration is dynamically refreshed so that each time the routing information changes, there is no need to modify the configuration file and then restart the service.

#### Nacos

The configuration centre for each microservice, the service registry.

- Configuration Centre
  - Shared configuration: MySQL data source related information configuration.

- Registration Centre
  - All microservice modules are registered to Nacos for service registration and discovery.
  - Integration with SpringCloud Gateway gateway.

#### Seata

Seata-based AT model for distributed transaction processing for the Inventory, Accounts and Orders modules.

Roll back transactions whenever stock is low/account balance is low.

#### Sentinel

Service fusion flow limiting for point and click scenarios.

Integrates Nacos Configuration Center with Spring Cloud Gateway to enable dynamic configuration of fused flow limiting rules for specified routing rules.

#### RocketMQ

Used for peaks and valleys reduction of like service traffic.

By sending high volume like requests from the producer to the mq, the consumer module pulls from the mq and consumes them with a certain frequency, rather than simply fusing and limiting the degradation of the service directly, enabling RocketMQ's ability to shave peaks and valleys for high volume traffic.

## Release Notes

This project provides a [local-deployment](local-deployment.md), [docker-compose version](docker-compose-deployment.md) and a [Kubernetes Helm-Chart version](kubernetes-deployment.md).

- To learn how to configure the components and build the complete environment, we recommend learning the [local-deployment](local-deployment.md).

- If you only want to run the sample code, avoid the tedious local environment construction process, and do not want to use the K8S cluster. You can try using [docker-compose version] (docker-compose-deployment.md).

- If you want to quickly experience the components on a K8S cluster and skip the process of deploying each component, please check out the [Kubernetes Helm-Chart version](kubernetes-deployment.md).