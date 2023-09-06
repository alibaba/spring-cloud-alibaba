# Istio Authentication Example

## Project Instruction

This project demonstrates how to use Istio to publish authentication config to Spring Cloud Alibaba (SCA) application and use the config to do authentication. The SCA authentication module supports authentication of Spring MVC and Spring WebFlux applications.

## Preparation
### Install K8s
Please refer to [tools](https://kubernetes.io/zh-cn/docs/tasks/tools/) chapter of K8s document.
### Enable Istio on K8s
Please refer to [install](https://istio.io/latest/zh/docs/setup/install/) chapter of Istio document.

## Introduction to Istio authentication rules
- [Istio Authorization Overview](https://istio.io/latest/zh/docs/concepts/security/#authorization)
- [Istio Security Detail](https://istio.io/latest/zh/docs/reference/config/security/)

## Demo
### Connect to Istio
Before launching the example for demonstration, let's look at how a Spring Cloud application accesses Istio and provides authentication. This section is only for you to understand how to use it. The config has been filled in this example and you may not need to modify it.
1. Modify `pom.xml` to introduce Istio resource transform and SCA authentication module:

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-governance-auth</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-xds-adapter</artifactId>
</dependency>
```
2. Configure Istio related metadata in the `src/main/resources/application` yml configuration file:

```yml
server:
  port: ${SERVER_PORT:80}
spring:
  cloud:
    governance:
      auth:
        enabled: ${ISTIO_AUTH_ENABLE:true}
    istio:
      config:
        enabled: ${ISTIO_CONFIG_ENABLE:true}
        host: ${ISTIOD_ADDR:127.0.0.1}
        port: ${ISTIOD_PORT:15010}
        polling-pool-size: ${POLLING_POOL_SIZE:10}
        polling-time: ${POLLING_TIMEOUT:10}
        istiod-token: ${ISTIOD_TOKEN:}
        log-xds: ${LOG_XDS:true}
```
Here's an explanation of each field:
|Configuration Item|key|Default Value|Description
|--|--|--|--|
|Whether to enable authentication| spring.cloud.governance.auth.enabled|true|
|Whether to connect to Istio to obtain authentication configuration| spring.cloud.istio.config.enabled|true|
|Host of Istiod| spring.cloud.istio.config.host|127.0.0.1|
|Port of Istiod| spring.cloud.istio.config.port|15012|15010 port does not need TLS，but 15012 does
|Thread pool size for application to pull the config| spring.cloud.istio.config.polling-pool-size|10|
|Time interval for application to pull the config| spring.cloud.istio.config.polling-time|30|The unit is second|
|JWT token for application to connect to 15012 port| spring.cloud.istio.config.istiod-token|Content of file `/var/run/secrets/tokens/istio-token` in the pod of application|
|Whether to print logs about xDS| spring.cloud.istio.config.log-xds|true|

### Run the application
Note that the application runs in the K8s environment, and the application in the non-default namespace needs to receive the rules issued by Istiod, and needs to inject the meta information of the running application Kubernetes into the following environment variables. For the specific operation method, please refer to [Kubernetes documentation](https://kubernetes.io/zh-cn/docs/tasks/inject-data-application/environment-variable-expose-pod-information):
|Environment variable name|K8s pod metadata name|
|--|--|
|POD_NAME|metadata.name|
|NAMESPACE_NAME|metadata.namespace|

**HINT：The POD in which your deployed application does not need to be automatically injected by Istio because the various governance modules of SCA will be used to replace the functions of the Envoy Proxy.**
### Demostration
The following are some simple examples of authentication rule configurations:
#### IP Blocks
The following command is used to deliver an authentication rule to the demo application through Istio. This rule restricts the source IP addresses that can access the application:
```YAML
kubectl apply -f - << EOF
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: from-ip-allow
  namespace: ${namespace_name}
spec:
  selector:
    matchLabels:
      app: ${app_name}
  action: DENY
  rules:
  - from:
    - source:
        ipBlocks: ["127.0.0.1"]
EOF
```
You can validate the rules by sending request to the auth interface of this demo:
```
curl --location --request GET '${demo_ip}/auth'
```
In this example, if the source IP of the request is 127.0.0.1, then the application returns:
```
Auth failed, please check the request and auth rule
```
This indicates that the request is denied.<br>
If the source IP of the request is not '127.0.0.1', then the application returns:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```
It indicates that the request has been authenticated by application and some meta data of the request will be returned.

After that, we delete the authentication rule for the IP Blocks:
```shell
kubectl delete AuthorizationPolicy from-ip-allow -n ${namespace_name}
```
Then request the auth interface of this demo again, we can find that the application will return the following message because the authentication rule has been deleted:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```

#### Request Header Authentication
We use the following command to deliver an authentication rule to the demo application through Istio. This rule restricts the request header for accessing the application:
```YAML
kubectl apply -f - << EOF
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: http-headers-allow
  namespace: ${namespace_name}
spec:
  selector:
    matchLabels:
      app: ${app_name}
  action: ALLOW
  rules:
  - when:
    - key: request.headers[User-Agent]
      values: ["PostmanRuntime/*"]
EOF
```
Then send a HTTP request with a user-agent header to verify whether the rule is valid:
```
curl --location --request GET '${demo_ip}/auth' \
--header 'User-Agent: PostmanRuntime/7.29.2'
```
Since this request carries a correct HTTP Header, it will return:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```
Then send a HTTP request without a user-agent header to verify whether the rule is valid:
```
curl --location --request GET '${demo_ip}/auth'
```
Since this request don't carry a correct HTTP Header, it will return:
```
Auth failed, please check the request and auth rule
```

After that, we remove the rule for requests header authentication:
```shell
kubectl delete AuthorizationPolicy http-headers-allow -n ${namespace_name}
```
Then request the auth interface of this demo again, we can find that the application will return the following message because the authentication rule has been deleted:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```

#### JWT Authentication
We use the following command to deliver an authentication rule to the demo application through Istio. This rule restricts the JWT token value that must be carried to access the application:
```YAML
kubectl apply -f - <<EOF
apiVersion: security.istio.io/v1beta1
kind: RequestAuthentication
metadata:
  name: jwt-jwks-uri
  namespace: ${namespace_name}
spec:
  selector:
    matchLabels:
      app: ${app_name}
  jwtRules:
  - issuer: testing@secure.istio.io
    jwksUri: https://raw.githubusercontent.com/istio/istio/release-1.5/security/tools/jwt/samples/jwks.json
EOF
```
A Http request with a correct JWT token is then sent to verify that the rule is valid:
```
curl --location --request GET '${demo_ip}/auth' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg'
```
Since this request carries a correct JWT token, it will return:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```
A Http request with an invalid JWT token is then sent to verify that the rule is valid:
```
curl --location --request GET '${demo_ip}/auth' \
--header 'Authorization: Bearer invalid token'
```
Since this request carries a invalid JWT token, it will return:
```
Auth failed, please check the request and auth rule
```
After that, we remove the rule for JWT authentication:
```shell
kubectl delete RequestAuthentication jwt-jwks-uri -n ${namespace_name}
```
Then request the auth interface of this demo again, we can find that the application will return the following message because the authentication rule has been deleted:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```