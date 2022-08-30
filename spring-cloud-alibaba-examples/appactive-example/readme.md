# Demo

## Overall architecture

![appactive_landscape](http://appactive.oss-cn-beijing.aliyuncs.com/images/SCA-DEMO.png)

The overall structure of this demo is shown in the figure.

Note: 

- The registry nacos and database mysql that the application depends on are not shown in the figure.
- The demo uses nacos as a command channel


There are 2 units:

- center: center unit
- unit: ordinary unit

There are 2 applications in total, according to the distance (call link) of the end user from near and far:

- frontend: frontend application, accept user requests, and return after requesting actual data
- product: product application, providing three services:
    - product List: General Service
    - product Details: Unit Service
    - product order: central service, relying on inventory application

The applications are deployed in each of the center and unit.

The green grid in the figure represents the call link of this request.

## Quick Start

### Step

1. Push baseline rules to nacos, visit [this](https://doc.appactive.io/docs/cn/details/demo_nacos.html#%E6%AD%A5%E9%AA%A4) for more
2. Start all 4 applications, the params are:

frontend
```
-Dappactive.channelTypeEnum=NACOS
-Dappactive.namespaceId=appactiveDemoNamespaceId
-Dappactive.unit=center
-Dappactive.app=frontend
-Dio.appactive.demo.unitlist=center,unit
-Dio.appactive.demo.applist=frontend,product
-Dserver.port=8885
```
product
```
-Dappactive.channelTypeEnum=NACOS
-Dappactive.namespaceId=appactiveDemoNamespaceId
-Dappactive.unit=center
-Dappactive.app=product
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/product?characterEncoding=utf8&useSSL=false&serverTimezone=GMT&activeInstanceId=mysql&activeDbName=product
-Dserver.port=8883
```

If you want to experience demo directly， please visit [appactive demo site](http://demo.appactive.io/)

## Modules Experience 

### Filter

#### steps

1. build all jar needed
2. run java application

    ```
    java -Dappactive.channelTypeEnum=NACOS \
           -Dappactive.namespaceId=appactiveDemoNamespaceId \
           -Dappactive.unit=unit \
           -Dappactive.app=frontend \
           -Dio.appactive.demo.unitlist=center,unit \
           -Dio.appactive.demo.applist=frontend,product,storage \
           -Dserver.port=8886 \
    -jar xxxxxx.jar
    ```

3. test

    ```
    curl 127.0.0.1:8886/show?r_id=1 -H "r_id:2" -b "r_id=3"
    routerId: 1
    curl 127.0.0.1:8886/show -H "r_id:2" -b "r_id=3"
    routerId: 2
    curl 127.0.0.1:8886/show  -b "r_id=3"
    routerId: 3
    curl 127.0.0.1:8886/show  
    routerId: null
    ```

### SpringCloud

the building process of demo of Dubbo is far too complicated，we suggest using demo in  "quick start". 
Specially，unit service protection testing are as follows:

1. run test

    ```
    curl 127.0.0.1:8884/detail -H "Host:demo.appactive.io" -H "appactive-router-id:2499"
    # you will notice an error
    403 FORBIDDEN "routerId 2499 does not belong in unit:unit"
    ```
A request of 2499 was routed to unit, however, request of 2499 should be routed to center,thus the provider of unit rejected this request

## Rule description

Before running all applications, we pushed baseline rules to nacos, which include:

- appactive.dataId.idSourceRulePath: Describes how to extract routing labels from http traffic
- appactive.dataId.transformerRulePath: Describe how to parse the routing mark
- appactive.dataId.trafficRouteRulePath: Describe the mapping relationship between the routing mark and the unit
- appactive.dataId.dataScopeRuleDirectoryPath_mysql-product: describes the attribution unit of the current machine

### Switch flow

Mainly do the following things when switching flow:

- Build new mapping rules and banning rules (manually)
- Push new mapping rules to gateway
- Push banning rules to other apps
- Wait for the data to tie and push the new mapping relationship rules to other applications

Note that the new mapping relationship is the target state you want to achieve, and the prohibition rule is the difference calculated based on the target state and the status quo. Currently, both of these need to be manually set and updated to the corresponding json file under `appactive-portal/rule`, and then run `sh cut.sh NACOS appactiveDemoNamespaceId`
