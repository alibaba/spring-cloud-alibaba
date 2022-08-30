# 样例

## 整体架构

![appactive_landscape](http://appactive.oss-cn-beijing.aliyuncs.com/images/SCA-DEMO.png)

本 demo 整体架构如图。 

注：

- 应用共同依赖的注册中心 nacos 和 数据库 mysql 未在图中展示出来。
- 本 demo 的命令通道依赖于 nacos


共有 2 个单元：

- center: 中心单元 
- unit: 普通单元

共有 2 个应用，按照距离（调用链路）终端用户由近及远分别为：

- frontend: 前端应用，接受用户请求，请求到实际数据后返回
- product: 产品应用，提供三个服务：
	- 产品列表: 普通服务
	- 产品详情: 单元服务
	- 产品下单: 中心服务

应用在 center 和 unit 各部署一套（对等部署）。

图中绿色格子代表了本次请求的调用链路。

## 快速启动

### 步骤

1. 往 nacos 中推送多活规则，详见 https://doc.appactive.io/docs/cn/details/demo_nacos.html#%E6%AD%A5%E9%AA%A4
2. 启动 4 套应用，启动参数分别为

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

若想直接体验更多，请见[AppActive官方demo站点](http://demo.appactive.io/)

## 分模块体验

### Filter

#### 步骤

1. 构建相关 jar 包
2. 运行

    ```
    java -Dappactive.channelTypeEnum=NACOS \
           -Dappactive.namespaceId=appactiveDemoNamespaceId \
           -Dappactive.unit=unit \
           -Dappactive.app=frontend \
           -Dio.appactive.demo.unitlist=center,unit \
           -Dio.appactive.demo.applist=frontend,product \
           -Dserver.port=8886 \
    -jar xxxxxx.jar
    ```

3. 测试

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

构建 SpringCloud 的 demo 过于复杂，建议使用 quick start 中启用的demo，直接进行体验，特别地，单元保护功能测试步骤如下：

1. 发起测试

    ```
    curl 127.0.0.1:8884/detail -H "Host:demo.appactive.io" -H "appactive-router-id:2499"
    # 注意到报错会有这样一段
    403 FORBIDDEN "routerId 2499 does not belong in unit:unit"
    ```

因为我们直接将 路由id为 2499 的 请求路由到了单元，但实际上，这个请求应该路由到中心，所以被单元的provider拒绝请求了。

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



