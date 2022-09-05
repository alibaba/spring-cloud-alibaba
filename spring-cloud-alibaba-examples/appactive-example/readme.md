# AppActive Example

## Introduction

[![vTlxsA.jpg](https://s1.ax1x.com/2022/09/04/vTlxsA.jpg)](https://imgse.com/i/vTlxsA)

The overall structure of this demo is shown above.

Note: 

- The registry Nacos and database MySQL that the application depends on are not shown in the figure.
- The demo uses Nacos as a command channel

### Core Concept

concept

Based on AppActive, the application multi-activity scheme can be divided into three categories: global application, core application and shared application according to application attributes, which can be classified into two types of units: central unit and common unit.

3 types applications:
- Center application: business applications (such as inventory, amount, etc.) with extremely high latency requirements and strong consistency cannot be split between multiple activities in different places, and they need to read and write services in the central unit.
- Unit application: a business application that is divided into units, an application that reads and writes in a specific unit according to the preset multi-active rules and request information.
- Normal application: It belongs to the business on the non-core link of the system, has low requirements on data consistency, and is not divided into units.

2 types units:

- Center: hosts the global application
- Unit: application of other non-central units.

There are three applications in demo, according to the distance (call link) of the end user from near and far:

- frontend: frontend application, accept user requests, and return after requesting actual data.
- product: product application, providing three services:
    - product List: General Service
    - product Details: Unit Service
    - product order: central service, relying on inventory application
- storage: storage application, it provide create orders service for users.

The applications are deployed in each of the center and unit.

The green grid in the figure represents the call link of the request.

## Instructions for use

### Quick start
Before starting the example for demonstration, let's take a look at how Spring Cloud applications use the remote multi-active capabilities provided by AppActive.
**Note, this chapter is only for your understanding of the access method. The access work has been completed in this examples, and you do not need to modify it.**

1. First, modify the pom.xml file to add the following maven dependencies based on the latest `spring-cloud-alibaba-dependencies` added to the provider and consumer.

       <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-appactive</artifactId>
        </dependency>

2. Assign traffic policies to specific interface configurations in the `application.properties` configuration file of the Provider application.

        spring.cloud.appactive.filter.unit-path=/detailHidden/*,/detail/*
        spring.cloud.appactive.filter.center-path=/buy/*
        spring.cloud.appactive.filter.normal-path=/*

3. In the `application.properties` of the Consumer application, configure the client load balancing as the load balancing algorithm provided by AppActive. The configuration method is as follows. Note that `[service-name]` needs to be replaced with the specific service name to be consumed.

        [service-name].ribbon.NFLoadBalancerRuleClassName =com.alibaba.cloud.appactive.consumer.AppactiveRule


### Presentation preparation

1. Start Nacos, MySQL, and push multi-active rules to Nacos:
   
   - In the `appactive-example` directory, execute: `docker-compose -f component-quickstart.yml up -d` to start Nacos, MySQL.
   - Execute the following command: `curl -X POST 'http://127.0.0.1:8848/nacos/v1/console/namespaces' -d 'customNamespaceId=appactiveDemoNamespaceId&namespaceName=appactiveDemoNamespaceName&namespaceDesc=appactiveDemoNamespaceDesc'` Create a demo named in Nacos configuration center Space appactiveDemoNamespaceId.
   - Execute the following command: `sh baseline.sh 2 NACOS appactiveDemoNamespaceId` to push the multi-active rule to the namespace. The multi-live rules are described as follows:
      - `appactive.dataId.idSourceRulePath`: Describes how to extract routing tokens from http traffic
      - `appactive.dataId.transformerRulePath`: Describes how to parse routing tokens
      - `appactive.dataId.trafficRouteRulePath`: Describes the mapping between routing labels and units
      - `appactive.dataId.dataScopeRuleDirectoryPath_mysql-product`: Properties that describe the database

   
2. Start five sets of applications, the startup parameters are:

- frontend
    ```
    -Dappactive.channelTypeEnum=NACOS
    -Dappactive.namespaceId=appactiveDemoNamespaceId
    -Dappactive.unit=unit
    -Dappactive.app=frontend
    -Dio.appactive.demo.unitlist=center,unit
    -Dio.appactive.demo.applist=frontend,product,storage
    -Dserver.port=8875
    ```
- product
    ```
    -Dappactive.channelTypeEnum=NACOS
    -Dappactive.namespaceId=appactiveDemoNamespaceId
    -Dappactive.unit=center
    -Dappactive.app=product
    -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/product?characterEncoding=utf8&useSSL=false&serverTimezone=GMT&activeInstanceId=mysql&activeDbName=product
    -Dserver.port=8883
    ```
    ```
    -Dappactive.channelTypeEnum=NACOS
    -Dappactive.namespaceId=appactiveDemoNamespaceId
    -Dappactive.unit=unit
    -Dappactive.app=product
    -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/product?characterEncoding=utf8&useSSL=false&serverTimezone=GMT&activeInstanceId=mysql&activeDbName=product
    -Dserver.port=8873
    ```
- storage
    ```
    -Dappactive.channelTypeEnum=NACOS
    -Dappactive.namespaceId=appactiveDemoNamespaceId
    -Dappactive.unit=center
    -Dappactive.app=storage
    -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/product?characterEncoding=utf8&useSSL=false&serverTimezone=GMT
    -Dserver.port=8881
    ```
    ```
    -Dappactive.channelTypeEnum=NACOS
    -Dappactive.namespaceId=appactiveDemoNamespaceId
    -Dappactive.unit=unit
    -Dappactive.app=storage
    -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/product?characterEncoding=utf8&useSSL=false&serverTimezone=GMT
    -Dserver.port=8871
    ```

## Demonstration process

1. Demonstration of normal application service calls belonging to the unit. Typing: `http://127.0.0.1:8079/listProduct` address in the browser, it can be seen that the request is sent to the product through the frontend application.

    [![vTlxsA.jpg](https://s1.ax1x.com/2022/09/04/vTlxsA.jpg)](https://imgse.com/i/vTlxsA)

    Since `/listProduct` in the above path matches the `/*` path rule in the product application, which corresponds to the normal unit, frontend does not have a tendency in the product address list obtained from the registry, and will randomly select an address for request sending. So requesting the above path multiple times will see the request switch back and forth between the unit and center units of the product.

2. Demonstration of unit application service calls belonging to the unit. Typing: `http://127.0.0.1:8079/detailProduct` in the browser, because the `/detailProduct` in the above path matches the `/detail/*` path rule in the product application, corresponding to the unit unit, it will be based on the request The specific value of the variable in the Header, Cookie or request parameter is used to determine the downstream unit type of the request, because the following flow switching rules are configured in advance (for details, see the content of the idUnitMapping.json file in the rule directory):
    ```
    {
      "itemType": "UnitRuleItem",
      "items": [
        {
          "name": "unit",
          "conditions": [
            {
              "@userIdBetween": [
                "0~1999"
              ]
            }
          ]
        },
        {
          "name": "center",
          "conditions": [
            {
              "@userIdBetween": [
                "2000~9999"
              ]
            }
          ]
        }
      ]
    }
    ```
    The above rules mean that requests with user IDs of 0~1999 will be sent to the unit in the downstream provider, and requests with user IDs of 2000~9999 will be sent to the center of the downstream provider.
    As shown in the figure below, a request with a user ID of 1999 is simulated. It can be seen that the request is sent to the unit node of product in the downstream through the frontend.

    [![1xnI7.jpg](https://s1.328888.xyz/2022/09/05/1xnI7.jpg)](https://imgloc.com/i/1xnI7)

    As shown in the figure below, a request with a user ID of 2000 is simulated. It can be seen that the request is sent to the center unit node of the product in the downstream through the frontend.

    [![1xAHk.jpg](https://s1.328888.xyz/2022/09/05/1xAHk.jpg)](https://imgloc.com/i/1xAHk)

3. Demonstration of center application service invocation belonging to the center. Typing: `http://127.0.0.1:8079/buyProduct` path in the browser, because the /buyProduct in the above path matches the /buy/* path rule in the product and storage applications, corresponding to the center unit, it will directly Send the request to the downstream center cell node.

    [![1s4Oi.jpg](https://s1.328888.xyz/2022/09/04/1s4Oi.jpg)](https://imgloc.com/i/1s4Oi)

4. Cut flow demo. The main things to do when cutting flow are as follows:
    - Build new mapping relationship rules and write prohibition rules (manually).
    - Push the write prohibition rules to the application.
    - Push the new mapping relationship rules to the application after waiting for the data to equalize.
   The streaming rule demonstrated next will send requests with user IDs 0~2999 to the unit in the downstream provider, and requests with user IDs 3000~9999 will be sent to the center in the downstream provider. For specific rules, see idUnitMappingNext.json:
        ```
        {
          "itemType": "UnitRuleItem",
          "items": [
            {
              "name": "unit",
              "conditions": [
                {
                  "@userIdBetween": [
                    "0~2999"
                  ]
                }
              ]
            },
            {
              "name": "center",
              "conditions": [
                {
                  "@userIdBetween": [
                    "3000~9999"
                  ]
                }
              ]
            }
          ]
        }
        ```
        As shown in the figure below, a request with a user ID of 2999 is simulated. It can be seen that the request is sent to the unit node of the product in the downstream through the frontend, and the flow switching rule takes effect.
        [![1xUnd.jpg](https://s1.328888.xyz/2022/09/05/1xUnd.jpg)](https://imgloc.com/i/1xUnd)

        As shown in the figure below, a request with a user ID of 3000 is simulated. It can be seen that the request is sent to the center unit node of the product in the downstream through the frontend, and the cut flow rule takes effect.
        [![1xpgr.jpg](https://s1.328888.xyz/2022/09/05/1xpgr.jpg)](https://imgloc.com/i/1xpgr)




