# label route example

## Project Description

This project demonstrates how to use the spring cloud ailbaba governance labelrouting module to complete the label routing function.

## module structure

This module includes a consumer instance and a provider cluster, which contains two instances.

## Example

### How to access

**Note that this section is only for your convenience in understanding the access method. The access work has been completed in this sample code, and you do not need to modify it.**
1. First, modify the pom XML file, which introduces the spring cloud ailbaba governance labelrouting dependency.
```xml
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-alibaba-governance-labelrouting</artifactId>
</dependency>
```

### Application Start

Start a startup class of three modules, ConsumerApplication and two ProviderApplications, and inject them into the Nacos registry.

### Effect demonstration

#### Rule Description
The rules set in the instance are as follows:
```java
@GetMapping("/add")
public void getDataFromControlPlaneTest() {
    List<RouteRule> routeRules = new ArrayList<>();
    List<MatchService> matchServices = new ArrayList<>();
    UnifiedRouteDataStructure unifiedRouteDataStructure = new UntiedRouteDataStructure();
    unifiedRouteDataStructure.setTargetService("service-provider");
    LabelRouteRule labelRouteData = new LabelRouteRule();
    labelRouteData.setDefaultRouteVersion("v1");
    RouteRule routeRule = new HeaderRule();
    routeRule.setType("header");
    routeRule.setCondition("=");
    routeRule.setKey("tag");
    routeRule.setValue("gray");
    RouteRule routeRule1 = new UrlRule.Parameter();
    routeRule1.setType("parameter");
    routeRule1.setCondition(">");
    routeRule1.setKey("id");
    routeRule1.setValue("10");
    RouteRule routeRule2 = new UrlRule.Path();
    routeRule2.setType("path");
    routeRule2.setCondition("=");
    routeRule2.setValue("/router-test");
    routeRules.add(routeRule);
    routeRules.add(routeRule1);
    routeRules.add(routeRule2);
    MatchService matchService = new MatchService();
    matchService.setVersion("v2");
    matchService.setWeight(100);
    matchService.setRuleList(routeRules);
    matchServices.add(matchService);
    labelRouteData.setMatchRouteList(matchServices);
    unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);
    List<UntiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
    unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
    controlPlaneConnection.pushRouteData(unifiedRouteDataStructureList);
}
```
The rules corresponding to the code are as follows:
If the request parameter contains tag=gray and the request header contains id and the value is greater than 10, uri is /router-test at the same time, the traffic is routed to the v2 version. If one of the request parameters does not meet the requirement, the traffic is routed to the v1 version.

Rules also support dynamic modification. The rules for testing dynamic modification are as follows:
```java
@GetMapping("/add")
public void getDataFromControlPlaneTest() {
	List<RouteRule> routeRules = new ArrayList<>();
	List<MatchService> matchServices = new ArrayList<>();
	UntiedRouteDataStructure unifiedRouteDataStructure = new UntiedRouteDataStructure();
	unifiedRouteDataStructure.setTargetService("service-provider");
	LabelRouteRule labelRouteData = new LabelRouteRule();
	labelRouteData.setDefaultRouteVersion("v1");
	
	RouteRule routeRule = new HeaderRule();
	routeRule.setType("header");
	routeRule.setCondition("=");
	routeRule.setKey("tag");
	routeRule.setValue("gray");
	RouteRule routeRule1 = new UrlRule.Parameter();
	routeRule1.setType("parameter");
	routeRule1.setCondition(">");
	routeRule1.setKey("id");
	routeRule1.setValue("10");
	
	RouteRule routeRule2 = new UrlRule.Path();
	routeRule2.setType("path");
	routeRule2.setCondition("=");
	routeRule2.setValue("/router-test");
	routeRules.add(routeRule);
	routeRules.add(routeRule1);
	routeRules.add(routeRule2);
	
	MatchService matchService = new MatchService();
	matchService.setVersion("v2");
	matchService.setWeight(50);
	matchService.setRuleList(routeRules);
	matchServices.add(matchService);
	labelRouteData.setMatchRouteList(matchServices);
	unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);
	List<UntiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
	unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
	controlPlaneConnection.pushRouteData(unifiedRouteDataStructureList);
}
```
The rules corresponding to the code are as follows:
If the request parameter contains tag=gray, and the request header contains id and the value is greater than 10,uri is /router-test, 50% of the traffic is routed to the v2 version, and the rest is routed to the v1 version. If one of the traffic does not meet the requirements, the traffic is routed to the v1 version.

#####  demonstrate Steps
1. visit http://localhost:18083/add Push the routing rules from the control surface interface to the routing rule warehouse
   visit http://localhost:18083/router -The test does not meet the routing rules. When the test is routed to the v1 version, the v1 version instance prints and returns the following results:
   ```
   Route in 30.221.132.228: 18081,version is v1.
   ```
   visit http://localhost:18083/router-test?id=11 and the test value set in the request header is gray, which meets the routing rules. The route is to the v2 version. The v2 version instance prints and returns the following results:
   ```
   Route in 30.221.132.228: 18082,version is v2.
   ```

2. visit http://localhost:18083/update Simulate dynamic modification of routing rules.
   visit http://localhost:18083/router  The test does not meet the routing rules. When the test is routed to the v1 version, the v1 version instance prints and returns the following results:
   ```
   Route in 30.221.132.228: 18081,version is v1.
   ```
   visit http://localhost:18083/router-test?id=11 and the test value set in the request header is gray, which meets the routing rules. 50% of the routes are routed to the v2 version. The v2 version instance prints the following results:
   ```
   Route in 30.221.132.228: 18082,version is v2.
   ```
   50% of them are routed to the v1 version, and the following results are returned when the v1 version instance is printed:
   ```
   Route in 30.221.132.228: 18081,version is v1.
   ```

3. If you don't push rule,it will load balance by common rule you set.
## Integrating Istio
**Note that this section is only for your convenience in understanding the access method. The access work has been completed in this sample code, and you do not need to modify it.**
## Preparation
### Install K8s
Please refer to [tools](https://kubernetes.io/zh-cn/docs/tasks/tools/) chapter of K8s document.
### Enable Istio on K8s
Please refer to [install](https://istio.io/latest/zh/docs/setup/install/) chapter of Istio document.
### Introduction to Istio traffic control rules
- [overview](https://istio.io/latest/zh/docs/concepts/security/#authorization)
- [detail](https://istio.io/latest/zh/docs/reference/config/security/)
1. First, modify the pom.xml file to introduce the `spring-cloud-starter-alibaba-governance-labelrouting` and `spring-cloud-starter-alibaba-istio` dependency
```xml
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-alibaba-governance-labelrouting</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-istio</artifactId>
</dependency>
```
2. Configure application.yml for Istio control plane:
```
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
        # Is authentication enabled
        enabled: ${ISTIO_AUTH_ENABLE:false}
    istio:
      config:
        # Is Istio resource transform enabled
        enabled: ${ISTIO_CONFIG_ENABLE:true}
        # Istiod ip
        host: ${ISTIOD_ADDR:127.0.0.1}
        # Istiod port
        port: ${ISTIOD_PORT:15010}
        # Istiod thread-pool size
        polling-pool-size: ${POLLING_POOL_SIZE:10}
        # Istiod polling gap
        polling-time: ${POLLING_TIME:10}
        # Istiod token(For Istio 15012 port)
        istiod-token: ${ISTIOD_TOKEN:}
        # Whether to print xds log
        log-xds: ${LOG_XDS:true}
```
### Startup Application
Start IstioConsumerApplication and two ProviderApplications, and inject it into the Nacos registry center.

### Publish Configuration
We publish the label routing rules through the Istio control plane. We publish a DestinationRule rule first:
```
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
This rule splits the back-end service into two versions. Pod with label v1 is assigned to v1, and pod with label v2 is assigned to v2
After that, we publish the VirtualService rule:
```
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
          exact: gray
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
This VirtualService specifies the simplest label routing rule. HTTP requests with a gray header and /istio-label-routing path are routed to v2, and the rest of the traffic is routed to v1:
### Demonstrate effect
We send an HTTP request without a request header to IstioConsumerApplication:
```
curl --location --request GET '127.0.0.1:18084/istio-label-routing'
```
Since the request header is not gray, the request will be routed to version v1 with the following result:
```
Route in 30.221.132.228: 18081,version is v1.
```
We then send an HTTP request with a gray tag in its header and the request path is /istio-label-routing:
```
curl --location --request GET '127.0.0.1:18084/istio-label-routing' --header 'tag: gray'
```
The request is routed to version v2 because the routing rule is matched by the request:
```
Route in 30.221.132.228: 18082,version is v2.
```
Finally, we delete this label routing rule::
```shell
kubectl delete VirtualService sca-virtual-service
kubectl delete DestinationRule my-destination-rule
```
After the rule is deleted, the routing policy is not determined by whether the request header is carried or not, but completely depends on the implementation of the loadbalancer。
## Integrating OpenSergo
**Note that this section is only for your convenience in understanding the access method. The access work has been completed in this sample code, and you do not need to modify it.**
### Configure
1. First, modify the pom.xml file to introduce the `spring-cloud-starter-alibaba-governance-labelrouting` and `spring-cloud-starter-alibaba-opensergo` dependency
```
   <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-governance-labelrouting</artifactId>
   </dependency>
   <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-opensergo</artifactId>
   </dependency>
```
2. Configure application.yml for OpenSergo control plane
```
# The endpoint of OpenSergo ControlPlane
spring.cloud.opensergo.endpoint=127.0.0.1:10246
```
### Startup Application
Start OpenSergoConsumerApplication and two ProviderApplications, and inject it into the Nacos registry center.
### Publish Configuration
[First start OpenSergo control plan](https://opensergo.io/docs/quick-start/opensergo-control-plane/) , Then we publish the label routing rules through the OpenSergo control plane. We publish a TrafficRouter rule.
```
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
This TrafficRouter specifies the simplest label routing rule. HTTP requests with a gray header are routed to v2, and the rest of the traffic is routed to v1.
If the version v2 does not have a corresponding instance, the HTTP request will fall back to the version v1.
### Demonstrate effect
We send an HTTP request without a request header to IstioConsumerApplication
```
curl --location --request GET '127.0.0.1:18083/router-test'
```
Since the request header is not gray, the request will be routed to version v1 with the following result
```
Route in 30.221.132.228: 18081,version is v1.
```
We then send an HTTP request with a gray tag in its header and the request path is /istio-label-routing
```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: gray'
```
The request is routed to version v2 because the routing rule is matched by the request.
```
Route in 30.221.132.228: 18082,version is v2.
```
After we stop the ProviderApplication of the version v2, we send an HTTP request with the request header tag gray.
```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: v2'
```
because the version v2 does not have a corresponding instance, so the Http requesr is fallback to the version v1.
```
Route in 30.221.132.228: 18081,version is v1.
```