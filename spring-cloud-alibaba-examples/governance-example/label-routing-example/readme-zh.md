# Routing Example

## 项目说明

本项目演示如何使用 Spring Cloud Alibaba Governance Routing 模块完成标签路由功能。

## 模块结构

本模块包括一个消费者实例和一个提供者集群，该集群包含着4个实例。

- 本文中提到的示例模块都在 `spring-cloud-alibaba/spring-cloud-alibaba-examples/governance-example/label-routing-example/` 目录下；
- starter 起步依赖模块都在 `spring-cloud-alibaba/spring-cloud-alibaba-starters/spring-cloud-starter-alibaba-governance-routing` 目录下。
- 如无特殊说明，下文中提到的所有路径都以其上为父路径。

## 组件支持说明
目前，路由模块只支持了部分组件：

远程调用组件：Spring Cloud OpenFeign, RestTemplate, WebClient；

负载均衡组件：Ribbon；

网关组件：Spring Netflix Zuul，Spring Cloud Gateway；

未来会支持更多组件，例如 Spring Cloud LoadBalancer 等。

## 示例

### 如何接入

**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**

1. 首先，修改需要进行路由服务的 `pom.xml` 文件，引入 `spring-cloud-alibaba-routing-service-adapter` 依赖，可以根据需要引入不同的适配器模块（提供 gateway 和 zuul 网关的适配）。

   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-alibaba-routing-service-adapter</artifactId>
   </dependency>
   ```

2. 配置当没有路由规则时的负载均衡算法（以随机负载均衡算法为例），如果没有配置，使用 ribbon 默认的负载均衡算法 ZoneAvoidanceRule 。

   ```properties
   spring.cloud.governance.routing.rule=RandomRule
   ```

### 应用启动

进入 `routing-service-provider-example` 文件夹，启动四个服务实例，分别为 A1ProviderApplication，到 A4ProviderApplication 将其注入到 Nacos 注册中心中。

### 客户端消费者效果演示（以 feign 为例）

> Note: 本章节演示提供了 Docker-Compose 快速启动版本，点击此处查看 (Docker-Compose QuickStart)[./docker-compose-example-quickstart/label-routing-quickstart/README-zh.md]

1. 进入 `web-client-consumer-example/resources` 文件夹中将请求客户端需要的脚本导入到 postman 中，在验证时发送请求使用；
   1. `feign` 消费者客户端在 postman 中的请求脚本位置在 `客户端消费者/feign` 目录下；（RestTemplate 和 WebClient 相同）
2. 进入 `web-client-consumer-example` 文件夹分别启动三个模块的启动类，分别为 ConsumerFeignApplication，ConsumerReactiveApplication 和 ConsumerRestApplication；
3. 逐个点击 v1 和 v2 版本请求，查看四个服务实例是否可以被正常消费**（不设置任何路由规则）**。

#### 服务提供者预期结果说明

进入 `routing-service-provider-example` 路径下，查看启动类文件，可以发现如下代码：

```java
	@RestController
class A1Controller {

   @GetMapping("/test-a1")
   public String testA1() {
	   
      String host = nacosRegistration.getHost();
      int port = nacosRegistration.getPort();
      String zone = nacosRegistration.getMetadata().get("zone");
      String region = nacosRegistration.getMetadata().get("region");
      String version = nacosRegistration.getMetadata().get("version");
      return "Route in " + host + ":" + port + ", region: " + region + ", zone: "
              + zone + ", version: " + version;
   }

}
```

从中可以看出服务提供者返回的数据如下：

- 返回服务实例的 host，ip 地址；
- 返回服务实例的 port；
- 在进行区域亲和性路由时，在服务中设置的 region 和 zone 标签；
- 在服务中设置的 nacos 元数据标签 version

服务提供者返回样例：

```shell
Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
```

#### 规则说明

服务消费者（openfeign-consumer-example，restTemplate-consumer-example，webClient-consumer-example）实例中设置的路由规则如下：

```json
[
  {
    "targetService": "routing-service-provider",
    "labelRouteRule": {
      "matchRouteList": [
        {
          "ruleList": [
            {
              "type": "header",
              "condition": "=",
              "key": "tag",
              "value": "v2"
            },
            {
              "type": "parameter",
              "condition": ">",
              "key": "id",
              "value": "10"
            },
            {
              "type": "path",
              "condition": "=",
              "value": "/router-test",
              "key": null
            }
          ],
          "version": "v2",
          "weight": 100,
          "fallback": null
        }
      ],
      "defaultRouteVersion": "v1"
    }
  }
]
```

代码对应的路由规则如下：

> 若同时满足请求参数中含有`tag=v2`，请求头中含有 id 且值小于10，uri 为`/router-test`则流量全部路由到 v2 版本中，若有一条不满足，则流量路由到 v1 版本中。

规则也支持动态修改，测试动态修改的规则如下：

```json 
[
   {
      "targetService": "routing-service-provider",
      "labelRouteRule": {
         "matchRouteList": [
            {
               "ruleList": [
                  {
                     "type": "header",
                     "condition": "=",
                     "key": "tag",
                     "value": "v2"
                  },
                  {
                     "type": "parameter",
                     "condition": ">",
                     "key": "id",
                     "value": "10"
                  },
                  {
                     "type": "path",
                     "condition": "=",
                     "value": "/router-test",
                     "key": null
                  }
               ],
               "version": "v2",
               "weight": 50,
               "fallback": null
            }
         ],
         "defaultRouteVersion": "v1"
      }
   }
]
```
代码对应的规则如下：

> 若同时满足请求参数中含有`tag=v2`，请 求头中含有 id 且值小于10，uri 为`/router-test`，则 50% 流量路由到 v2 版本中，剩下的流量路由到 v1 版本中，若有一条不满足，则流量路由到 v1 版本中。

区域亲和性路由规则如下：

```yml
    # label routing configuration
    governance:
      routing:
        region: dev
        zone: zone1
      # rule: RandomRule
```

> 当服务实例满足所设置的 `region=dev`, `zone=zone1` 规则时，路由到指定服务实例。

#### 当区域亲和性路由存在时

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：不满足路由规则，路由到v1版本中，且区域亲和性路由规则为 regnion=dev，zone=zone1，预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   # v2：观察 service-provider 的元数据发现，没有 region=dev，zone=zone1，version=v2 的服务实例，因此区域亲和性路由会退化为标签路由效果，预期为以下结果：
   # 	 Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   #	 Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   
   # 测试发现和预期结果匹配！
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果：
   # v1：不满足路由规则，路由到 v1 版本中，且区域亲和性路由规则为 region=dev，zone=zone1，实例打印返回如下结果：
   #	 Route in 172.18.0.3:19093, region: dev, zone: zone1, version: v1
   # v2：因为设置了区域亲和性路由规则，所以即使 v1 和 v2 版本各自 50% 的权重，但是还是会根据区域亲和性路由规则选取服务实例, 预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
   # 测试发现和预期结果匹配！
   ```

#### 当区域亲和性路由不存在时

进入 `web-client-consumer-example/openfeign-consumer-example/src/main/resources/application.yml` 文件中，注释以下配置，再次启动 ConsumerFeignApplication；

```yml
    # label routing configuration
    governance:
      routing:
        # region: dev
        # zone: zone1
      # rule: RandomRule
```

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：因为没有区域亲和性路由限制，所以会在 v1 实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
   #	Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
   # v2：因为没有区域亲和性路由限制，所以会在 v2 实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   
   # 测试发现，符合预期结果
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果
   # v1：因为没有区域亲和性路由限制，路由结果按照标签路由选择服务实例，所以会在两个 v1 实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   # 	Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
     
   # v2：v1 和 v2 权重各占 50，所以四种服务实例的调用结果都会出现
   #	Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   #	Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   #	Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
   
   # 测试发现，符合预期结果
   ```

### 网关消费者效果演示 （以 gateway 为例）

1. 进入 `gateway-consumer-example` 文件夹分别启动两个网关模块的启动类，分别为 ConsumerZuulApplication，和ConsumerGatewayApplication
2. 进入 `gateway-consumer-example/resources` 文件夹将网关示例需要的请求脚本导入到 postman 中；
3. 逐个点击 v1 和 v2 版本请求，查看四个服务实例是否可以被正常消费**（不设置任何路由规则）**。

#### 规则说明

网关消费者中的标签路由规则如下：

```json
[
   {
      "targetService": "routing-service-provider",
      "labelRouteRule": {
         "matchRouteList": [
            {
               "ruleList": [
                  {
                     "type": "header",
                     "condition": "=",
                     "key": "tag",
                     "value": "v2"
                  },
                  {
                     "type": "parameter",
                     "condition": ">",
                     "key": "id",
                     "value": "10"
                  },
                  {
                     "type": "path",
                     "condition": "=",
                     "value": "/test-a1",
                     "key": null
                  }
               ],
               "version": "v2",
               "weight": 100,
               "fallback": null
            }
         ],
         "defaultRouteVersion": "v1"
      }
   }
]
```

代码对应的路由规则如下：

> 若同时满足请求参数中含有`tag=v2`，请求头中含有 id 且值小于10，uri 为`/test-a1`则流量全部路由到 v2 版本中，若有一条不满足，则流量路由到 v1 版本中。

更新路由规则：

```json
[
   {
      "targetService": "routing-service-provider",
      "labelRouteRule": {
         "matchRouteList": [
            {
               "ruleList": [
                  {
                     "type": "header",
                     "condition": "=",
                     "key": "tag",
                     "value": "v2"
                  },
                  {
                     "type": "parameter",
                     "condition": ">",
                     "key": "id",
                     "value": "10"
                  },
                  {
                     "type": "path",
                     "condition": "=",
                     "value": "/test-a1",
                     "key": null
                  }
               ],
               "version": "v2",
               "weight": 50,
               "fallback": null
            }
         ],
         "defaultRouteVersion": "v1"
      }
   }
]

```

代码对应的规则如下：

> 若同时满足请求参数中含有`tag=v2`，请 求头中含有 id 且值小于10，uri 为`/test-a1`，则 50% 流量路由到 v2 版本中，剩下的流量路由到 v1 版本中，若有一条不满足，则流量路由到 v1 版本中。

区域亲和性路由规则如下：

```yml
    # label routing configuration
    governance:
      routing:
        region: dev
        zone: zone1
      # rule: RandomRule
```

> 当服务实例满足所设置的 `region=dev`, `zone=zone1` 规则时，路由到指定服务实例。

#### 当区域亲和性路由存在时

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：不满足路由规则，路由到v1版本中，且区域亲和性路由规则为 region=dev，zone=zone1，预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   # v2：观察 service-provider 的元数据发现，没有 region=dev，zone=zone1，version=v2 的服务实例，因此区域亲和性路由会退化为标签路由效果，预期为以下结果：
   # 	 Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   #	 Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   
   # 测试发现和预期结果匹配！
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果：
   # v1：不满足标签路由规则，路由到v1版本中，从两个 v1 版本实例中根据区域亲和性标签选择服务实例，实例打印返回如下结果：
   #	 Route in 172.18.0.3:19093, region: dev, zone: zone1, version: v1
   # v2：因为设置了区域亲和性路由规则，所以即使 v1 和 v2 版本各自 50% 的权重，但是还是会根据区域亲和性路由规则选取服务实例, 预期结果为：
   # 	 Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
   # 测试发现和预期结果匹配！
   ```

#### 当区域亲和性路由不存在时

进入 `gateway-consumer-example/gateway-consumer-example/src/main/resources/application.yml` 文件中，注释以下配置，再次启动 GatewayConsumerApplication ；

```yml
    # Regional affinity routing configuration
    governance:
       routing:
#        region: dev
#        zone: zone1
       # rule: RandomRule
```

1. 添加路由规则，将路由规则由控制面接口推入路由规则仓库中。

   ```shell
   # 预期结果：
   # v1：因为没有区域亲和性路由限制，所以会在实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
   #	Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
   # v2：因为没有区域亲和性路由限制，所以会在实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   
   # 测试发现，符合预期结果
   ```

2. 更新路由规则，模拟动态修改路由规则。

   ```shell
   # 预期结果
   # v1：因为没有区域亲和性路由限制，路由结果按照标签路由选择服务实例，所以会在两个实例之间按照 ribbon 的规则进行负载均衡
   #	Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   # 	Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
     
   # v2：v1 和 v2 权重各占 50，所以四种服务实例的调用结果都会出现
   #	Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   #	Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   #	Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   #	Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
   
   # 测试发现，符合预期结果
   ```

## 集成Istio

**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**
### 安装K8s环境
请参考K8s的[安装工具](https://kubernetes.io/zh-cn/docs/tasks/tools/)小节。
### 在K8s上安装并启用Istio
请参考Istio官方文档的[安装](https://istio.io/latest/zh/docs/setup/install/)小节。
### Istio流量治理规则介绍
- [VirtualService](https://istio.io/latest/zh/docs/reference/config/networking/virtual-service/)
- [DestinationRule](https://istio.io/latest/zh/docs/reference/config/networking/destination-rule/)
### 配置
1. 首先，修改pom.xml 文件，引入`spring-cloud-starter-alibaba-governance-routing-starter`依赖。同时引入Spring Cloud Alibaba的`spring-cloud-starter-xds-adapter`模块

```xml
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-alibaba-governance-routing-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-xds-adapter</artifactId>
</dependency>
```

2. 在`src/main/resources/application.yml`配置文件中配置Istio控制面的相关信息:

```YAML
server:
  port: 18084
spring:
  main:
    allow-bean-definition-overriding: true
  application:
    name: service-consumer
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        fail-fast: true
        username: nacos
        password: nacos
    governance:
      auth:
        # 是否开启鉴权
        enabled: ${ISTIO_AUTH_ENABLE:false}
    istio:
      config:
        # 是否开启Istio配置转换
        enabled: ${ISTIO_CONFIG_ENABLE:true}
        # Istiod ip
        host: ${ISTIOD_ADDR:127.0.0.1}
        # Istiod 端口
        port: ${ISTIOD_PORT:15010}
        # 轮询Istio线程池大小
        polling-pool-size: ${POLLING_POOL_SIZE:10}
        # 轮询Istio时间间隔
        polling-time: ${POLLING_TIME:10}
        # Istiod鉴权token(访问Istiod 15012端口时可用)
        istiod-token: ${ISTIOD_TOKEN:}
        # 是否打印xds相关日志
        log-xds: ${LOG_XDS:true}
```

### 应用启动

启动三个模块的启动类，分别为IstioConsumerApplication，两个ProviderApplication，将其注入到Nacos注册中心中。

### 下发配置

通过Istio控制面下发标签路由规则，首先下发DestinationRule规则:

```YAML
kubectl apply -f - << EOF
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: my-destination-rule
spec:
  host: sca-virtual-service
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
EOF
```

此规则将后端服务拆分为两个版本，label为v1的pod被分到v1版本，label为v2的pod被分到v2版本之后，下发 VirtualService 规则:

```YAML
kubectl apply -f - << EOF
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: sca-virtual-service
spec:
  hosts:
    - service-provider
  http:
  - match:
    - headers:
        tag:
          exact: v2
      uri:
        exact: /istio-label-routing
    route:
    - destination:
        host: service-provider
        subset: v2
  - route:
    - destination:
        host: service-provider
        subset: v1
EOF
```

这条 VirtualService 指定了一条最简单的标签路由规则，将请求头tag为v2，请求路径为`/istio-label-routing`的HTTP请求路由到v2版本，其余的流量都路由到v1版本:
### 效果演示

发送一条不带请求头的HTTP请求至IstioConsumerApplication:

```
curl --location --request GET '127.0.0.1:18084/istio-label-routing'
```
因为请求头不为v2，所以请求将会被路由到v1版本，返回如下:
```
Route in 30.221.132.228: 18081,version is v1.
```
之后发送一条请求头tag为v2，且请求路径为`/istio-label-routing`的HTTP请求:
```
curl --location --request GET '127.0.0.1:18084/istio-label-routing' --header 'tag: v2'
```
因为满足路由规则，所以请求会被路由至v2版本:
```
Route in 30.221.132.228: 18082,version is v2.
```
最后删除这条标签路由规则:
```shell
kubectl delete VirtualService sca-virtual-service
kubectl delete DestinationRule my-destination-rule
```
删除规则后，可以看到路由的策略将不由请求头的携带与否来决定，而是完全遵从于负载均衡器的实现。

## 集成OpenSergo
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**
1. 首先，修改pom.xml 文件，引入`spring-cloud-starter-alibaba-governance-routing-starter`依赖。同时引入Spring Cloud Alibaba的`spring-cloud-starter-opensergo-adapter`模块
```XML
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-alibaba-governance-routing-starter</artifactId>
</dependency>
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-opensergo-adapter</artifactId>
</dependency>
```
2. 在application.properties配置文件中配置OpenSergo控制面的相关信息
```
# OpenSergo 控制面 endpoint
spring.cloud.opensergo.endpoint=127.0.0.1:10246
```
### 应用启动
启动三个模块的启动类，分别为OpenSergoConsumerApplication，两个ProviderApplication，将其注入到Nacos注册中心中。

### 下发配置

[启动 OpenSergo 控制面](https://opensergo.io/zh-cn/docs/quick-start/opensergo-control-plane/) ，并通过 OpenSergo 控制面下发流量路由规则

```YAML
kubectl apply -f - << EOF
apiVersion: traffic.opensergo.io/v1alpha1
kind: TrafficRouter
metadata:
  name: service-provider
  namespace: default
  labels:
    app: service-provider
spec:
  hosts:
    - service-provider
  http:
    - match:
        - headers:
            tag:
              exact: v2
      route:
        - destination:
            host: service-provider
            subset: v2
            fallback:
              host: service-provider
              subset: v1
    - route:
        - destination:
            host: service-provider
            subset: v1
EOF
```
这条[TrafficRouter](https://github.com/opensergo/opensergo-specification/blob/main/specification/zh-Hans/traffic-routing.md) 指定了一条最简单的流量路由规则，将请求头tag为v2的HTTP请求路由到v2版本，其余的流量都路由到v1版本。
如果v2版本没有对应的节点，则将流量fallback至v1版本。
### 效果演示
发送一条不带请求头的HTTP请求至OpenSergoConsumerApplication
```
curl --location --request GET '127.0.0.1:18083/router-test'
```
因为请求头不为v2，所以请求将会被路由到v1版本，返回如下
```
Route in 30.221.132.228: 18081,version is v1.
```
之后发送一条请求头tag为v2的HTTP请求
```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: v2'
```
因为满足路由规则，所以请求会被路由至v2版本
```
Route in 30.221.132.228: 18082,version is v2.
```
停止v2版本的ProviderApplication后，继续发送一条请求头tag为v2的HTTP请求
```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: v2'
```
因为v2版本没有服务提供者，因此流量被fallback至v1版本。
```
Route in 30.221.132.228: 18081,version is v1.
```
