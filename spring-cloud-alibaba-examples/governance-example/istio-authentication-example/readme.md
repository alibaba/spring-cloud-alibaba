# Istio Authentication Example

## Project Instruction

This project demonstrates how to use Istio to publish authentication config to Spring Cloud Alibaba application and use the config to do authentication

## Preparation
### Install K8s
Please refer to [tools](https://kubernetes.io/zh-cn/docs/tasks/tools/)chapter of K8s document
### Enable Istio on K8s
Please refer to [install](https://istio.io/latest/zh/docs/setup/install/)chapter of Istio ducoment

## Introduction to Istio authentication rules
[overview](https://istio.io/latest/zh/docs/concepts/security/#authorization)
[detail](https://istio.io/latest/zh/docs/reference/config/security/)

## Demo
### Connect to Istio
Before launching the example for demonstration, let's look at how a Spring Cloud application accesses Istio and provides authentication. This section is only for you to understand how to use it. The config has been filled in this example and you may not need to modify it.
1. Modify pom.xml to introduce Istio resource transform and Spring Cloud Alibaba authentication module

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>auth-core</artifactId>
	<version>2.2.9-SNAPSHOT</version>
</dependency>

<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>istio-resource-transform</artifactId>
	<version>2.2.9-SNAPSHOT</version>
</dependency>
```
2. Configure Istio related metadata in the src/main/resources/application yml configuration file

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
        secure: ${ISTIOD_SECURE:false}
        istiod-token: ${ISTIOD_TOKEN:}        
```
下面解释一下各字段的含义
|Configuration Item|key|Description
|--|--|--|
|Whether to enable authentication| spring.cloud.governance.auth.enabled||
|Whether to connect to Istio to obtain authentication configuration| spring.cloud.istio.config.enabled||
|Host of Istiod| spring.cloud.istio.config.host||
|Port of Istiod| spring.cloud.istio.config.port|注：15010 port does not need TLS，but 15012 does|
|Thread pool size for SCA to pull the config| spring.cloud.istio.config.polling-pool-size||
|Time interval for SCA to pull the config| spring.cloud.istio.config.polling-time||
|JWT token for SCA to connect to 15012 port| spring.cloud.istio.config.istiod-token||
### Run the application
You need to run the application in the K8s environment and inject some meta information about K8s into the following environment variables for the running application
|Environment variable name|K8s pod metadata name|
|--|--|
|POD_NAME|metadata.name|
|NAMESPACE_NAME|metadata.namespace|

**注：The POD in which your deployed application does not need to be automatically injected by Istio because the various governance modules of Spring Cloud Alibaba will be used to replace the functions of the Envoy Proxy**
### 效果演示
In this demo, if we carry the correct authentication information, the SCA authentication module will approve the request, and the demo will return some metadata information about the request
```
received request from 127.0.0.1, local addr is 127.0.0.1, local host is localhost, request path is/test
```
The status code of the response is 200<br>
If we carry incorrect authentication information, the SCA authentication module will reject the request and print
```
Auth failed, please check the request and auth rule
```
The status code of the response is 401<br>
The following are some simple examples of authentication rule configurations
#### IP黑白名单
The following command is used to deliver an authentication rule to the demo application through Istio. This rule restricts the source IP addresses that can access the application
```
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
  action: ALLOW
  rules:
  - from:
    - source:
        ipBlocks: ["127.0.0.1"]
EOF
```
You can validate the rules by sending request to the auth interface of this demo
```
curl --location --request GET '${demo_ip}/auth'
```
#### 请求头
We use the following command to deliver an authentication rule to the demo application through Istio. This rule restricts the request header for accessing the application
```
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
Then send an Http request with the user-agent header and an Http request without the user-agent header to verify whether the rule is valid
```
curl --location --request GET '${demo_ip}/auth' \
--header 'User-Agent: PostmanRuntime/7.29.2'
```
```
curl --location --request GET '${demo_ip}/auth'
```
#### JWT
We use the following command to deliver an authentication rule to the demo application through Istio. This rule restricts the JWT token value that must be carried to access the application
```
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
An Http request with the correct JWT token and an incorrect JWT token are then sent to verify that the rule is valid
```
curl --location --request GET '${demo_ip}/auth' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg'
```
```
curl --location --request GET '${demo_ip}/auth' \
--header 'Authorization: Bearer invalid token'
```
