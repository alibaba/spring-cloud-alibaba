# AppActive Example

## 项目说明

[![vTlxsA.jpg](https://s1.ax1x.com/2022/09/04/vTlxsA.jpg)](https://imgse.com/i/vTlxsA)

本 demo 整体架构如图。 

注：

- 应用共同依赖的注册中心 Nacos 和 数据库 MySQL 未在图中展示出来。
- 本 demo 的命令通道依赖于 Nacos。


共有 3 类单元：

- center: 中心单元，无法做多活改造的应用
- unit: 普通单元，做了多活改造应用
- normal：非单元，未做多活改造的应用

共有 3 个应用，按照距离（调用链路）终端用户由近及远分别为：

- frontend: 前端应用，接受用户请求，请求到实际数据后返回
- product: 产品应用，提供三个服务：
	- 产品列表: 普通服务
	- 产品详情: 单元服务
	- 产品下单: 中心服务
- storage: 库存应用，供下单服务扣减库存

应用在 center 和 unit 各部署一套（对等部署）。

图中绿色格子代表了本次请求的调用链路。

## 示例

### 快速接入
在启动示例进行演示之前，我们先了解一下 Spring Cloud 应用如何使用 AppActive所提供的异地多活能力。
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改 pom.xml 文件，引入 Nacos Config Starter。

       <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-appactive</artifactId>
        </dependency>

2. 在应用的 /src/main/resources/application.properties 配置文件中给特定接口配置其所调用的实例类型。

        spring.cloud.appactive.filter.unit-path=/detailHidden/*,/detail/*
        spring.cloud.appactive.filter.center-path=/buy/*
        spring.cloud.appactive.filter.normal-path=/*

3. 在应用的 /src/main/resources/application.properties 配置客户端负载均衡为 AppActive 所提供的负载均衡算法，配置方式如下，注意需要将[service-name]替换成具体的待消费服务名。

        [service-name].ribbon.NFLoadBalancerRuleClassName =com.alibaba.cloud.appactive.consumer.AppactiveRule


### 快速启动

1. 启动 Nacos, MySQL, 并往 Nacos 中推送多活规则：
   
   - 在 `appactive-example` 目录下，执行：`docker-compose -f component-quickstart.yml up -d` 启动 Nacos, MySQL。 
   - 执行以下命令：`curl -X POST 'http://127.0.0.1:8848/nacos/v1/console/namespaces' -d 'customNamespaceId=appactiveDemoNamespaceId&namespaceName=appactiveDemoNamespaceName&namespaceDesc=appactiveDemoNamespaceDesc'` 在 Nacos 配置中心中创建一个演示用命名空间 appactiveDemoNamespaceId。 
   - 执行以下命令：`sh baseline.sh 2 NACOS appactiveDemoNamespaceId`，往命名空间中推送多活规则。
   
2. 启动 5 套应用，启动参数分别为：

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

## 效果演示

### 普通服务

#### 步骤

1. 在浏览器中输入：http://127.0.0.1:8079/listProduct 地址，可见请求通过 frontend 应用被发送给了 product。

    [![vTlxsA.jpg](https://s1.ax1x.com/2022/09/04/vTlxsA.jpg)](https://imgse.com/i/vTlxsA)

    由于上述路径中的 /listProduct 在 product 应用中匹配到的是 /* 路径规则，对应 normal 非单元，所以frontend 在从注册中心获取的 product 地址列表中不存在倾向性，会随机选择地址进行请求发送。因此多次请求上述路径，会看到请求在 product 的 unit 和 center 单元中来回切换。

2. 在浏览器中输入：http://127.0.0.1:8079/detailProduct 路径，由于上述路径中的 /detailProduct 在 product 应用中匹配到的是 /detail/* 路径规则，对应 unit 单元，其会根据请求中 Header, Cookie 或请求参数中的变量具体的值去判断该请求的下游单元类型，由于事先配置如下切流规则（具体可见 rule 目录下的 idUnitMapping.json 文件内容）：
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
    上述规则表示，用户Id为 0~1999 的请求将发送给下游提供者中的 unit 单元，用户Id为 2000~9999 的请求将发送给下游提供者中的 center 单元。
    如下图，模拟一个用户Id为 1999 的请求，可见请求通过 frontend 发送到了下游中 product 的 unit 单元。

    [![1sOhE.jpg](https://s1.328888.xyz/2022/09/04/1sOhE.jpg)](https://imgloc.com/i/1sOhE)

    如下图，模拟一个用户Id为 2000 的请求，可见请求通过 frontend 发送到了下游中 product 的 center 单元。

    [![1sjsJ.jpg](https://s1.328888.xyz/2022/09/04/1sjsJ.jpg)](https://imgloc.com/i/1sjsJ)

3. 在浏览器中输入：http://127.0.0.1:8079/buyProduct 路径，由于上述路径中的 /buyProduct 在 product 和 storage 应用中匹配到的是 /buy/* 路径规则，对应 center 单元，其会直接将请求发送到下游的 center 单元中节点。

    [![1s4Oi.jpg](https://s1.328888.xyz/2022/09/04/1s4Oi.jpg)](https://imgloc.com/i/1s4Oi)


## 规则说明

### 基线

在运行所有应用前，我们将规则推送到了 nacos，规则包括

- appactive.dataId.idSourceRulePath: 描述如何从 http 流量中提取路由标
- appactive.dataId.transformerRulePath: 描述如何解析路由标
- appactive.dataId.trafficRouteRulePath: 描述路由标和单元的映射关系
- appactive.dataId.dataScopeRuleDirectoryPath_mysql-product: 描述数据库的属性

### 切流

切流时主要做了如下几件事：

- 构建新的映射关系规则和禁写规则（手动）
- 将禁写规则推送给应用
- 等待数据追平后将新的映射关系规则推送给应用

注意，新的映射关系是你想达到的目标状态，而禁写规则是根据目标状态和现状计算出来的差值。
当前，这两者都需要你手动设置并更新。



