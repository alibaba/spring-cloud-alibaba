# Routing Example

## Project description

This project demonstrates how to use the Spring Cloud Alibaba Governance Routing module to complete the label routing function.

## Module structure

This module includes a consumer instance and a provider cluster, which contains four instances.

- The sample modules mentioned in this article are in the `spring-cloud-alibaba/spring-cloud-alibaba-examples/governance-example/label-routing-example/` directory;
- The starter dependent module is in the `spring-cloud-alibaba/spring-cloud-alibaba-starters/spring-cloud-starter-alibaba-governance-routing` directory.
- Unless otherwise specified, all paths mentioned below are based on the parent path above.

## Component support description
Currently, the routing module supports only a few components:

Remote call component: Spring Cloud OpenFeign, RestTemplate, WebClient;

Load balancing component: Ribbon;

Gateway components: Spring Netflix Zuul, Spring Cloud Gateway;

More components, such as Spring Cloud LoadBalancer, will be supported in the future.

## Examples

### How to access

**Note that this section is only for your convenience to understand the access method. The access work has been completed in this sample code, and you do not need to modify it.**

1. First, modify the `pom.xml` file that needs routing service and introduce `spring-cloud-starter-alibaba-governance-routing` dependencies.


   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-governance-routing</artifactId>
   </dependency>
   ```

2. Configure the load balancing algorithm when there is no routing rule (take the random load balancing algorithm as an example). If there is no configuration, use the default load balancing algorithm Zone AvoidanceRule.


   ```properties
   spring.cloud.governance.routing.rule=RandomRule
   ```

### The application starts

Enter the `routing-service-provider-example` folder, start four service instances, namely A1Provider Application, and inject them into the Nacos registry at A4Provider Application.

### Client consumer effect demonstration (take feign as an example)

> Note: This chapter demo provides a quick start version of Docker-Compose. Click here to view (docker-compose QuickStart) [./docker-composition-example-quickstart/label-routing-quickstart/README-zh. MD]

1. Enter `web-client-consumer-example/resources` the folder to import the script required by the request client into postman, and send the request for use during verification;
   1. The location of the request script for the `feign` consumer client in postman is in the `客户端消费者/feign` directory; (RestTemplate is the same as WebClient)
2. Enter the `web-client-consumer-example` folder to start the startup classes of the three modules respectively, which are ConsumerFeign Application, ConsumerReactive Application and ConsumerRestApplication;
3. Click the v1 and v2 version requests one by one to see if the four service instances can be consumed **(No routing rules are set)** normally.

#### Description of expected results of service provider

Enter `routing-service-provider-example` the path, view the startup class file, and you can find the following code:

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

It can be seen that the data returned by the service provider is as follows:

- Returns the host, IP address of the service instance;
- Returns the port of the service instance;
- The region and zone labels set in the service during the region affinity routing;
- The nacos metadata tag version set in the service

Service Provider Return Sample:


```shell
Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
```

#### Rule description

The routing rules set in the service consumer (openfeign-consumer-example, restTemplate-consumer-example, reactive-consumer-example) instance are as follows:


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

The routing rule corresponding to the code is as follows:

> If the request parameters are satisfied at the same time `tag=v2`, the request header contains ID and the value is less than 10, and the URI is `/router-test`, all traffic is routed to the v2 version, and if one is not satisfied, the traffic is routed to the v1 version.

The rules also support dynamic modification, and the rules for testing dynamic modification are as follows:


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
The rules corresponding to the codes are as follows:

> If the request parameters are satisfied at the same time `tag=v2`, the request header contains ID and the value is less than 10, and the URI is `/router-test`, then 50% of the traffic is routed to the v2 version, and the remaining traffic is routed to the v1 version. If one of the traffic does not meet the requirement, then the traffic is routing to the v1 version.

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
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # v2: Observing the metadata of service-provider, it is found that there is no service instance with region=dev, zone=zone1, and version=v2. Therefore, regional affinity routing will degenerate into label routing effect. The following results are expected:
    # Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
    # Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   
    # The test results match the expected results!
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.

   ```shell
    # expected outcome:
    # v1: The routing rules are not satisfied, and the route is routed to the v1 version, and the regional affinity routing rules are region=dev, zone=zone1. The example print returns the following results:
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # v2: Because the regional affinity routing rules are set, even if the v1 and v2 versions each have a weight of 50%, the service instance will still be selected based on the regional affinity routing rules. The expected result is:
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
    # The test results match the expected results!
   ```

#### When an area affinity route does not exist

Enter the `web-client-consumer-example/openfeign-consumer-example/src/main/resources/application.yml` file, comment the following configuration, and start ConsumerFeignApplication again;

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
    # Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
    # v2: Since there are no regional affinity routing restrictions, load balancing will be performed between v2 instances according to ribbon rules.
    # Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   
    # The test found that it met the expected results
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.


   ```shell
    # expected outcome
    # v1: Because there are no regional affinity routing restrictions, the routing results select service instances based on label routing, so load balancing is performed between the two v1 instances according to ribbon rules.
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
     
    # v2: The weights of v1 and v2 each account for 50, so the calling results of the four service instances will appear.
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
    # Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
   
    # The test found that it met the expected results 
   ```

### Demonstration of gateway consumer effect (taking gateway as an example)

1. Enter the `gateway-consumer-example` folder to start the startup classes of the two gateway modules respectively, which are ConsumerZuulApplication and ConsumerGateway Application
2. Enter the `gateway-consumer-example/resources` folder to import the request script required by the gateway sample into postman;
3. Click the v1 and v2 version requests one by one to see if the four service instances can be consumed **(No routing rules are set)** normally.

#### Rule description

The tag routing rules in the gateway consumer are as follows:


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

The routing rule corresponding to the code is as follows:

> If the request parameters are satisfied at the same time `tag=v2`, the request header contains ID and the value is less than 10, and the URI is `/test-a1`, all traffic is routed to the v2 version, and if one is not satisfied, the traffic is routed to the v1 version.

Update routing rules:


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
    # v1: The routing rules are not satisfied, and the route is routed to the v1 version, and the regional affinity routing rules are region=dev, zone=zone1. The expected result is:
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # v2: Observing the metadata of service-provider, it is found that there is no service instance with region=dev, zone=zone1, and version=v2. Therefore, regional affinity routing will degenerate into label routing effect. The following results are expected:
    # Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
    # Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
   
    # The test results match the expected results!
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.


   ```shell
    # expected outcome:
    # v1: The label routing rules are not satisfied, and the route is routed to the v1 version. The service instance is selected from the two v1 version instances based on the regional affinity label. The instance printing returns the following results:
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # v2: Because the regional affinity routing rules are set, even if the v1 and v2 versions each have a weight of 50%, the service instance will still be selected based on the regional affinity routing rules. The expected result is:
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
    # The test results match the expected results!
   ```

#### When an area affinity route does not exist

Enter the `gateway-consumer-example/gateway-consumer-example/src/main/resources/application.yml` file, comment the following configuration, and start the GatewayConsumerApplication again;


```yml
    # Regional affinity routing configuration
    governance:
       routing:
#        region: dev
#        zone: zone1
       # rule: RandomRule
```

1. Adds a routing rule and pushes the routing rule from the control plane interface into the routing rule repository.


   ```shell
    # expected outcome:
    # v1: Because there are no regional affinity routing restrictions, load balancing will be performed between instances according to ribbon rules.
    # Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
   
    # v2: Because there are no regional affinity routing restrictions, load balancing will be performed between instances according to ribbon rules.
    # Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
   
    # The test found that it met the expected results
   ```

2. Updating routing rules and simulating dynamic modification of routing rules.


   ```shell
    # expected outcome
    # v1: Because there are no regional affinity routing restrictions, the routing results select service instances based on label routing, so load balancing is performed between the two instances according to ribbon rules.
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
     
    # v2: The weights of v1 and v2 each account for 50, so the calling results of the four service instances will appear.
    # Route in 192.168.2.9:19093, region: dev, zone: zone1, version: v1
    # Route in 192.168.2.9:19092, region: qa, zone: zone2, version: v2
    # Route in 192.168.2.9:19094, region: dev, zone: zone2, version: v2
    # Route in 192.168.2.9:19091, region: qa, zone: zone1, version: v1
   
    # The test found that it met the expected results
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
- [Istio Authorization Overview](https://istio.io/latest/zh/docs/concepts/security/#authorization)
- [Istio Security Detail](https://istio.io/latest/zh/docs/reference/config/security/)
1. First, modify the pom.xml file to introduce the `spring-cloud-starter-alibaba-governance-routing` and `spring-cloud-starter-xds-adapter` dependency
```xml
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-alibaba-governance-routing</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-xds-adapter</artifactId>
</dependency>
```
2. Configure application.yml for Istio control plane:
```YAML
server:
  port: ${SERVER_PORT:80}
spring:
  cloud:
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
1. First, modify the POM. XML file to introduce `spring-cloud-starter-alibaba-governance-routing` dependencies. At the same time, the module of Spring Cloud Alibaba `spring-cloud-starter-opensergo-adapter` is introduced.

## Integrating OpenSergo
**Note that this section is only for your convenience in understanding the access method. The access work has been completed in this sample code, and you do not need to modify it.**
### Configuration
1. First, modify the `pom.xml` file to introduce the `spring-cloud-starter-alibaba-governance-routing` and `spring-cloud-starter-opensergo-adapter` dependency
```xml
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-alibaba-governance-routing</artifactId>
</dependency>
<dependency>
   <groupId>com.alibaba.cloud</groupId>
   <artifactId>spring-cloud-starter-opensergo-adapter</artifactId>
</dependency>
```
2. Configure `application.yml` for OpenSergo control plane
```
# OpenSergo 控制面 endpoint
spring.cloud.opensergo.endpoint=127.0.0.1:10246
```
### Startup Application
Start OpenSergoConsumerApplication and two ProviderApplications, and inject it into the Nacos registry center.
### Publish Configuration
[First start OpenSergo control plan](https://opensergo.io/docs/quick-start/opensergo-control-plane/) , Then we publish the label routing rules through the OpenSergo control plane. We publish a TrafficRouter rule.
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
This [TrafficRouter](https://github.com/opensergo/opensergo-specification/blob/main/specification/en/traffic-routing.md)  specifies the simplest label routing rule. HTTP requests with a v2 header are routed to v2, and the rest of the traffic is routed to v1.
If the version v2 does not have a corresponding instance, the HTTP request will fall back to the version v1.
### Demonstrate effect
We send an HTTP request without a request header to OpenSergoConsumerApplication
```
curl --location --request GET '127.0.0.1:18083/router-test'
```
Since the request header is not v2, the request will be routed to version v1 with the following result
```
Route in 30.221.132.228: 18081,version is v1.
```
Then send an HTTP request with a request header tag of v2

```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: v2'
```
The request is routed to version v2 because the routing rule is matched by the request.
```
Route in 30.221.132.228: 18082,version is v2.
```
After we stop the ProviderApplication of the version v2, we send an HTTP request with the request header tag v2.
```
curl --location --request GET '127.0.0.1:18083/router-test' --header 'tag: v2'
```
because the version v2 does not have a corresponding instance, so the Http requesr is fallback to the version v1.
```
Route in 30.221.132.228: 18081,version is v1.
```
