# Integrated Example

## Project Description

This project is a demo project of Spring CLoud Alibaba containerized deployment best practices, it is an Example project integrating Spring Cloud Alibaba related components (Nacos, Sentinel, Seata, RocketMQ).

The main components used, and their usage features are as follows.

- Spring Cloud Gateway gateway
- Nacos configuration center and service registry
- Sentinel fusion flow limitation
- Seata Distributed Transactions
- RocketMQ message queue peak-shaving
- Docker Microservice Containerized Deployment
- Docker-Compose Microservice Container Management

Kubernetes cluster orchestration management! [Overall Overview](https://my-img-1.oss-cn-hangzhou.aliyuncs.com/image-20220816004541921.png)

## Application Scenario Description

In this demo example, we provide two business scenarios

1) The scenario where a user places an order to buy goods, and after placing the order

- first request the inventory module and deduct the inventory

- Deduct the account balance

- Generate order information and return the response

2) User likes the goods (simulating the producer-consumer application scenario of MQ) to return the details of the goods after the likes (number of likes, etc.)

### Component details

1) In which the user places an order to purchase goods scenario mainly using Seata to reflect the ability of distributed transactions

2) In the scenario where the user likes the product, we simulate a high traffic environment with Sentinel to limit the flow or RocketMQ to cut the peaks and fill the valleys. In this scenario, we provide two ways to deal with high traffic

- Sentinel binds specified gateway routes on the gateway side for service fusion degradation
- RocketMQ for peak-shaving, where producers send messages to RocketMQ while consumers pull and consume at configurable consumption rates to reduce the pressure of high traffic direct requests to the database and increase the number of likes

> SpringCloud Gateway

Gateway for microservice modules

Spring Cloud GateWay integration with Nacos for dynamic routing configuration

By listening to the changes in Nacos configuration, the service gateway routing configuration is dynamically refreshed so that each time the routing information changes, there is no need to modify the configuration file and restart the service.

> Nacos

Configuration center for each microservice, service registration center

- Configuration Center
    - Shared configuration: mysql data source related information configuration

- Registration information
- All microservice modules are registered to Nacos for service registration and discovery
- Integration with SpringCloud Gateway 

> Seata

Seata based AT schema for distributed transactions for Inventory, Account, Order modules

Rollback transactions whenever inventory is low/account balance is low

> Sentinel

Service fusion flow limiting for point-and-click scenarios

Integration of Nacos Configuration Center and Spring Cloud Gateway for dynamic configuration of fusion flow restriction rules for specified routing rules

> RocketMQ

Peaks and valleys reduction for like service traffic

By sending high volume of like requests from the producer to mq, the consumer module pulls from mq to consume at a certain frequency, instead of simply downgrading the direct service fuse limit, realizing the ability of rocketmq to cut peaks and fill valleys for high volume of traffic.

## Release Notes

This project provides for [local-deployment version](local-deployment.md) and [docker-compose container version](container-deployment.md)

- If you want to learn how to configure the components and write the complete business process, we recommend learning [local-deployment version](local-deployment.md)

- If you want to quickly experience the effect of components and skip the process of environment deployment, please check out [docker-compose container version](container-deployment.md)