# Routing Example

## Project description

This project demonstrates how to use the Spring Cloud Alibaba Governance Routing module to complete the label routing function.

## Module structure

This module includes a consumer instance and a provider cluster that contains two instances.

## Component support description
Currently, the routing module supports only a few components:

Remote call component: Spring Cloud OpenFeign, RestTemplate, WebClient;

Load balancing component: Ribbon;

Gateway components: Spring Netflix Zuul, Spring Cloud Gateway;

More components, such as Spring Cloud LoadBalabcer, will be supported in the future.

## Examples

### How to access

**Note that this section is only for your convenience to understand the access method. The access work has been completed in this sample code, and you do not need to modify it.**

1. First, modify the `pom.xml` file that needs routing service and introduce `spring-cloud-alibaba-routing-service-adapter` dependencies. Different adapter modules can be introduced as required.


   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-alibaba-routing-service-adapter</artifactId>
   </dependency>
   ```

2. Configure the load balancing algorithm when there is no routing rule (take the random load balancing algorithm as an example). If there is no configuration, use the default load balancing algorithm Zone AvoidanceRule.


   ```properties
   spring.cloud.governance.routing.rule=RandomRule
   ```

### The application starts

Enter the `spring-cloud-alibaba-examples/governance-example/label-routing-example/routing-service-provider-example` folder, start four service instances, namely A1Provider Application, and inject them into the Nacos registry at A4Provider Application.

### Client consumer effect demonstration (take feign as an example)

> Note: This chapter demo provides a quick start version of Docker-Compose. Click here to view (docker-compose QuickStart) [./docker-composition-example-quickstart/label-routing-quickstart/README.md]

1. Import the script to postman in `spring-cloud-alibaba-examples/governance-example/label-routing-example/web-client-consumer-example/resources` the and `spring-cloud-alibaba-examples/governance-example/label-routing-example/gateway-consumer-example/resources` folders respectively;
2. Enter the `spring-cloud-alibaba-examples/governance-example/label-routing-example/wen-client-consumer-example` folder to start the startup classes of the three modules respectively, which are ConsumerFeign Application, ConsumerReactive Application and ConsumerRestApplication;

3. Click the v1 and v2 version requests one by one to see if the four service instances can be consumed **(Do not set task routing rules)** normally.

#### Rule description
The routing rules set in the service consumer instance are as follows:


```java
@GetMapping("/add")
public void getDataFromControlPlaneTest() {
    List<RoutingRule> routingRules = new ArrayList<>();
    List<MatchService> matchServices = new ArrayList<>();
    UnifiedRouteDataStructure unifiedRouteDataStructure = new UntiedRouteDataStructure();
    unifiedRouteDataStructure.setTargetService(WebClientConsumerConstants.SERVICE_PROVIDER_NAME);
    LabelRouteRule labelRouteData = new LabelRouteRule();
    labelRouteData.setDefaultRouteVersion("v1");
    RoutingRule routingRule = new HeaderRule();
    routingRule.setType("header");
    routingRule.setCondition("=");
    routingRule.setKey("tag");
    routingRule.setValue("v2");
    RoutingRule routingRule1 = new UrlRule.Parameter();
    routingRule1.setType("parameter");
    routingRule1.setCondition(">");
    routingRule1.setKey("id");
    routingRule1.setValue("10");
    RoutingRule routingRule2 = new UrlRule.Path();
    routingRule2.setType("path");
    routingRule2.setCondition("=");
    routingRule2.setValue("/router-test");
    routingRules.add(routingRule);
    routingRules.add(routingRule1);
    routingRules.add(routingRule2);
    MatchService matchService = new MatchService();
    matchService.setVersion("v2");
    matchService.setWeight(100);
    matchService.setRuleList(routingRules);
    matchServices.add(matchService);
    labelRouteData.setMatchRouteList(matchServices);
    unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);
    List<UntiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
    unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
    controlPlaneConnection.pushRouteData(unifiedRouteDataStructureList);
}
```

The routing rule corresponding to the code is as follows:

> If the request parameters are satisfied at the same time `tag=v2`, the request header contains ID and the value is less than 10, and the URI is `/router-test`, all traffic is routed to the v2 version, and if one is not satisfied, the traffic is routed to the v1 version.

The rules also support dynamic modification, and the rules for testing dynamic modification are as follows:

```java
@GetMapping("/add")
public void getDataFromControlPlaneTest() {
	List<RoutingRule> routingRules = new ArrayList<>();
	List<MatchService> matchServices = new ArrayList<>();
	UntiedRouteDataStructure unifiedRouteDataStructure = new UntiedRouteDataStructure();
	unifiedRouteDataStructure.setTargetService(WebClientConsumerConstants.SERVICE_PROVIDER_NAME);
	LabelRouteRule labelRouteData = new LabelRouteRule();
	labelRouteData.setDefaultRouteVersion("v1");
	
	RoutingRule routingRule = new HeaderRule();
	routingRule.setType("header");
	routingRule.setCondition("=");
	routingRule.setKey("tag");
	routingRule.setValue("v2");
	RoutingRule routingRule1 = new UrlRule.Parameter();
	routingRule1.setType("parameter");
	routingRule1.setCondition(">");
	routingRule1.setKey("id");
	routingRule1.setValue("10");
	
	RoutingRule routingRule2 = new UrlRule.Path();
	routingRule2.setType("path");
	routingRule2.setCondition("=");
	routingRule2.setValue("/router-test");
	routingRules.add(routingRule);
	routingRules.add(routingRule1);
	routingRules.add(routingRule2);
	
	MatchService matchService = new MatchService();
	matchService.setVersion("v2");
	matchService.setWeight(50);
	matchService.setRuleList(routingRules);
	matchServices.add(matchService);
	labelRouteData.setMatchRouteList(matchServices);
	unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);
	List<UntiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
	unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
	controlPlaneConnection.pushRouteData(unifiedRouteDataStructureList);
}
```
The rules corresponding to the codes are as follows:

> If the request parameters are satisfied at the same time `tag=v2`, the request header contains ID and the value is less than 10, and the URI is `/router-test`, then 50% of the traffic is routed to the v2 version, and the remaining traffic is routed to the v1 version. If one of the traffic does not meet the requirement, then the traffic is routing to the v1 version.

The area affinity routing rules are as follows:

```yml
    # label routing configuration
    governance:
      routing:
      # region: dev
      # zone: zone1
      # rule: RandomRule
```

> Route to the specified service instance when the service instance satisfies the set `region=dev` `zone=zone1` rule.

#### When area affinity routes exist

1. Adds a routing rule and pushes the routing rule from the control plane interface into the routing rule repository.


   ```shell
    # expected outcome:
    # v1: The routing rules are not satisfied, and the route is routed to the v1 version, and the regional affinity routing rules are regnion=dev, zone=zone1. The expected result is:
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
    # v2: Observing the metadata of service-provider, it is found that there is no service instance with region=dev, zone=zone1, and version=v2. Therefore, regional affinity routing will degenerate into label routing effect. The following results are expected:
    # Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
    # Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
   
    # The test results match the expected results!
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.


   ```shell
    # expected outcome:
    # v1: The routing rules are not satisfied, and the route is routed to the v1 version, and the regional affinity routing rules are regnion=dev, zone=zone1. The instance print returns the following results:
    # Route in 172.18.0.3:18083, region: dev, zone: zone1, version: v1
    # v2: Because the regional affinity routing rules are set, even if the v1 and v2 versions each have a weight of 50%, the service instance will still be selected based on the regional affinity routing rules. The expected result is:
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   
    # The test results match the expected results!
   ```

#### When an area affinity route does not exist

Enter the `spring-cloud-alibaba-examples/governance-example/label-routing-example/web-client-consumer-example/openfeign-consumer-example/src/main/resources/application.yml` file, comment the following configuration, and start ConsumerFeignApplication again;


```yml
    # label routing configuration
    governance:
      routing:
        # region: dev
        # zone: zone1
      # rule: RandomRule
```

1. Adds a routing rule and pushes the routing rule from the control plane interface into the routing rule repository.


   ```shell
    # expected outcome:
    # v1: Because there are no regional affinity routing restrictions, load balancing will be performed among v1 instances according to ribbon rules.
    # Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   
    # v2: Since there are no regional affinity routing restrictions, load balancing will be performed between v2 instances according to ribbon rules.
    # Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
   
    # The test found that it met the expected results
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.


   ```shell
    # expected outcome
    # v1: Because there are no regional affinity routing restrictions, the routing results select service instances based on label routing, so load balancing is performed between the two v1 instances according to ribbon rules.
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
    # Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
     
    # v2: The weights of v1 and v2 each account for 50, so the calling results of the four service instances will appear.
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
    # Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
    # Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
   
    # The test found that it met the expected results
   ```

### Demonstration of gateway consumer effect (taking gateway as an example)

1. Enter the `spring-cloud-alibaba-examples/governance-example/label-routing-example/gateway-consumer-example` folder to start the startup classes of the two gateway modules respectively, which are ConsumerZuulApplication and ConsumerGateway Application

3. Click the v1 and v2 version requests one by one to see if the four service instances can be consumed **(Do not set task routing rules)** normally.

#### Rule description

The label routing rules in the gateway consumer are as follows


```java
@Override
public void getDataFromControlPlaneTest() {

    log.info("请求 /add 接口，发布路由规则");

    List<Rule> routeRules = new ArrayList<>();
    List<MatchService> matchServices = new ArrayList<>();

    UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();

    // set target service
    unifiedRouteDataStructure.setTargetService(GatewayConstants.SERVICE_PROVIDER_NAME);

    RoutingRule labelRouteData = new RoutingRule();

    // set default service version
    labelRouteData.setDefaultRouteVersion("v1");

    // set request header routing rule
    Rule routeRule = new HeaderRoutingRule();
    routeRule.setCondition("=");
    routeRule.setKey("tag");
    routeRule.setValue("v2");

    // set request url routing rule
    Rule routeRule1 = new UrlRoutingRule.ParameterRoutingRule();
    routeRule1.setCondition(">");
    routeRule1.setKey("id");
    routeRule1.setValue("10");

    // set request url routing rule
    Rule routeRule2 = new UrlRoutingRule.PathRoutingRule();
    routeRule2.setCondition("=");
    routeRule2.setValue("/test-a1");

    // add routing rule to routeRules#List<Rule>
    routeRules.add(routeRule);
    routeRules.add(routeRule1);
    routeRules.add(routeRule2);

    // If the preceding conditions are met, the route is routed to the v2 instance and
    // the weight is set to 100
    MatchService matchService = new MatchService();
    matchService.setVersion("v2");
    matchService.setWeight(100);
    matchService.setRuleList(routeRules);
    matchServices.add(matchService);

    labelRouteData.setMatchRouteList(matchServices);

    unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);

    List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
    unifiedRouteDataStructureList.add(unifiedRouteDataStructure);

    RoutingDataChangedEvent routingDataChangedEvent = new RoutingDataChangedEvent(
        this, unifiedRouteDataStructureList);

    // Publish routing rules
    applicationContext.publishEvent(routingDataChangedEvent);

    log.info("请求 /add 接口，发布路由规则完成！");

}
```

The routing rule corresponding to the code is as follows:

> If the request parameters are satisfied at the same time `tag=v2`, the request header contains ID and the value is less than 10, and the URI is `/test-a1`, all traffic is routed to the v2 version, and if one is not satisfied, the traffic is routed to the v1 version.

Update routing rules:


```java
public void updateDataFromControlPlaneTest() {

    log.info("请求 /update 接口，更新路由规则");

    List<Rule> routeRules = new ArrayList<>();
    List<MatchService> matchServices = new ArrayList<>();

    UnifiedRoutingDataStructure unifiedRouteDataStructure = new UnifiedRoutingDataStructure();
    unifiedRouteDataStructure.setTargetService(GatewayConstants.SERVICE_PROVIDER_NAME);

    RoutingRule labelRouteData = new RoutingRule();
    labelRouteData.setDefaultRouteVersion("v1");

    Rule routeRule = new HeaderRoutingRule();
    routeRule.setCondition("=");
    routeRule.setKey("tag");
    routeRule.setValue("v2");
    Rule routeRule1 = new UrlRoutingRule.ParameterRoutingRule();
    routeRule1.setCondition(">");
    routeRule1.setKey("id");
    routeRule1.setValue("10");
    Rule routeRule2 = new UrlRoutingRule.PathRoutingRule();
    routeRule2.setCondition("=");
    routeRule2.setValue("/test-a1");
    routeRules.add(routeRule);
    routeRules.add(routeRule1);
    routeRules.add(routeRule2);

    // set weight 50
    MatchService matchService = new MatchService();
    matchService.setVersion("v2");
    matchService.setWeight(50);
    matchService.setRuleList(routeRules);
    matchServices.add(matchService);

    labelRouteData.setMatchRouteList(matchServices);

    unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);

    List<UnifiedRoutingDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
    unifiedRouteDataStructureList.add(unifiedRouteDataStructure);

    applicationContext.publishEvent(
        new RoutingDataChangedEvent(this, unifiedRouteDataStructureList));

    log.info("请求 /update 接口，更新路由规则完成！");

}
```

The rules corresponding to the codes are as follows:

> If the request parameters are satisfied at the same time `tag=v2`, the request header contains ID and the value is less than 10, and the URI is `/test-a1`, then 50% of the traffic is routed to the v2 version, and the remaining traffic is routed to the v1 version. If one of the traffic does not meet the requirement, then the traffic is routing to the v1 version.

The area affinity routing rules are as follows:


```yml
    # label routing configuration
    governance:
      routing:
        region: dev
        zone: zone1
      # rule: RandomRule
```

> Route to the specified service instance when the service instance satisfies the set `region=dev` `zone=zone1` rule.

#### When area affinity routes exist

1. Adds a routing rule and pushes the routing rule from the control plane interface into the routing rule repository.


   ```shell
    # expected outcome:
    # v1: The routing rules are not satisfied, and the route is routed to the v1 version, and the regional affinity routing rules are regnion=dev, zone=zone1. The expected result is:
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
    # v2: Observing the metadata of service-provider, it is found that there is no service instance with region=dev, zone=zone1, and version=v2. Therefore, regional affinity routing will degenerate into label routing effect. The following results are expected:
    # Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
    # Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
   
    # The test results match the expected results!
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.


   ```shell
    # expected outcome:
    # v1: The label routing rules are not satisfied, and the route is routed to the v1 version. The service instance is selected from the two v1 version instances based on the regional affinity label. The instance printing returns the following results:
    # Route in 172.18.0.3:18083, region: dev, zone: zone1, version: v1
    # v2: Because the regional affinity routing rules are set, even if the v1 and v2 versions each have a weight of 50%, the service instance will still be selected based on the regional affinity routing rules. The expected result is:
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   
    # The test results match the expected results!
   ```

#### When an area affinity route does not exist

Enter the `spring-cloud-alibaba-examples/governance-example/label-routing-example/web-client-consumer-example/routing-feign-consumer-example/src/main/resources/application.yml` file, comment the following configuration, and start ConsumerFeignApplication again;


```yml
    # label routing configuration
    governance:
      routing:
        # region: dev
        # zone: zone1
      # rule: RandomRule
```

1. Adds a routing rule and pushes the routing rule from the control plane interface into the routing rule repository.


   ```shell
    # expected outcome:
    # v1: Because there are no regional affinity routing restrictions, load balancing will be performed between instances according to ribbon rules.
    # Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   
    # v2: Because there are no regional affinity routing restrictions, load balancing will be performed between instances according to ribbon rules.
    # Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
   
    # The test found that it met the expected results
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.


   ```shell
    # expected outcome:
    # v1: Because there are no regional affinity routing restrictions, load balancing will be performed between instances according to ribbon rules.
    # Route in 192.168.2.9:18081, region: qa, zone: zone1, version: v1
    # Route in 192.168.2.9:18083, region: dev, zone: zone1, version: v1
   
    # v2: Because there are no regional affinity routing restrictions, load balancing will be performed between instances according to ribbon rules.
    # Route in 192.168.2.9:18084, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:18082, region: qa, zone: zone2, version: v2
   
    # The test found that it met the expected results
   ```

## Integrate Istio

**Note that this section is only for your convenience to understand the access method. The access work has been completed in this sample code, and you do not need to modify it.**
### Install the K8s environment
Refer to [Install Tools](https://kubernetes.io/zh-cn/docs/tasks/tools/) section K8s.
### Install and enable Istio on K8s
Please refer to the section of the official Istio documentation [Install](https://istio.io/latest/zh/docs/setup/install/).
### Introduction to Istio Traffic Governance Rules
- [VirtualService](https://istio.io/latest/zh/docs/reference/config/networking/virtual-service/)
- [DestinationRule](https://istio.io/latest/zh/docs/reference/config/networking/destination-rule/)
### Configuration
1. First, modify the POM. XML file to introduce `spring-cloud-starter-alibaba-governance-routing-starter` dependencies. At the same time, the module of Spring Cloud Alibaba `spring-cloud-starter-xds-adapter` is introduced.

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
2. To configure information about the Istio control plane in the `src/main/resources/application.yml` configuration file:

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
### The application starts
Start the startup classes of the three modules, IstioConsumerApplication and the two ProviderApplications, and inject them into the Nacos registry.

### Distribute configuration
Issue the label routing rule through the Istio control plane. Issue the DestinationRule rule first:

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
This rule splits the backend service into two versions. After the pod with label v1 is assigned to version v1, and the pod with label v2 is assigned to version v2, the Virtual Service rule is issued:

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
This Virtual Service specifies the simplest tag routing rule, which routes HTTP requests with a request header tag of v2 and a request path `/istio-label-routing` of to the v2 version, and the rest of the traffic to the v1 version:
### Effect demonstration
Send an HTTP request to IstioConsumerApplication without a request header:

```
curl --location --request GET '127.0.0.1:18084/istio-label-routing'
```
Because the request header is not v2, the request will be routed to the v1 version, returning the following:

```
Route in 30.221.132.228: 18081,version is v1.
```
Then send an HTTP request with a request header tag of v2 and a request path `/istio-label-routing` of:

```
curl --location --request GET '127.0.0.1:18084/istio-label-routing' --header 'tag: v2'
```
Because the routing rules are met, the request is routed to v2:

```
Route in 30.221.132.228: 18082,version is v2.
```
Finally, delete the label routing rule:

```shell
kubectl delete VirtualService sca-virtual-service
kubectl delete DestinationRule my-destination-rule
```
After deleting the rule, it can be seen that the routing strategy will not be determined by whether the request header is carried or not, but will fully comply with the implementation of the load balancer.

## Integrate OpenSergo
**Note that this section is only for your convenience to understand the access method. The access work has been completed in this sample code, and you do not need to modify it.**
1. First, modify the POM. XML file to introduce `spring-cloud-starter-alibaba-governance-routing-starter` dependencies. At the same time, the module of Spring Cloud Alibaba `spring-cloud-starter-opensergo-adapter` is introduced.

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
2. Configure information about the OpenSergo control plane in the application. Properties configuration films

```
# OpenSergo 控制面 endpoint
spring.cloud.opensergo.endpoint=127.0.0.1:10246
```
### The application starts
Start the startup classes of the three modules, OpenSergoConsumer Application and two ProviderApplications, and inject them into the Nacos registry.

### Distribute configuration

[Start the OpenSergo control surface](https://opensergo.io/zh-cn/docs/quick-start/opensergo-control-plane/) And send the traffic routing rules through the OpenSergo control plane


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
This [TrafficRouter](https://github.com/opensergo/opensergo-specification/blob/main/specification/zh-Hans/traffic-routing.md) specifies a simple traffic routing rule that routes HTTP requests with a request header tag of v2 to version v2, and the rest of the traffic to version v1. If the v2 version does not have a corresponding node, the traffic is fallback to the v1 version.
### Effect demonstration
Send an HTTP request to OpenSergoConsumer Application without a request header

```
curl --location --request GET '127.0.0.1:18083/router-test'
```
Because the request header is not v2, the request will be routed to the v1 version and returned as follows

```
Route in 30.221.132.228: 18081,version is v1.
```
Then send an HTTP request with a request header tag of v2

```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: v2'
```
Because the routing rules are met, the request is routed to v2

```
Route in 30.221.132.228: 18082,version is v2.
```
After stopping the v2 Provider Application, continue to send an HTTP request with a request header tag of v2

```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: v2'
```
Because the v2 version does not have a service provider, traffic is fallback to the v1 version.

```
Route in 30.221.132.228: 18081,version is v1.
```
