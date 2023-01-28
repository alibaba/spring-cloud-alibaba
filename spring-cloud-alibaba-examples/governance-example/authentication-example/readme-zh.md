# Istio Authentication Example

## 项目说明

本项目演示如何使用 Istio 下发鉴权配置到Spring Cloud Alibaba(下文简称：SCA)并对应用做鉴权。SCA鉴权模块同时支持对Spring MVC以及Spring WebFlux应用做鉴权。

## 准备
### 安装K8s环境
请参考K8s的[安装工具](https://kubernetes.io/zh-cn/docs/tasks/tools/)小节。
### 在K8s上安装并启用Istio
请参考Istio官方文档的[安装](https://istio.io/latest/zh/docs/setup/install/)小节。

## Istio鉴权规则介绍
- [授权概述](https://istio.io/latest/zh/docs/concepts/security/#authorization)
- [具体配置方法](https://istio.io/latest/zh/docs/reference/config/security/)

## 示例
### 如何接入
在启动示例进行演示之前，先了解一下应用如何接入Istio并提供鉴权功能。 注意 本章节只是为了便于理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。
1. 修改`pom.xml`文件，引入Istio规则Adapter以及SCA鉴权模块:

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
2. 在应用的 `src/main/resources/application.yml` 配置文件中配置Istio相关元数据:

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
下面解释一下各字段的含义:
|配置项|key|默认值|说明
|--|--|--|--|
|是否开启鉴权| spring.cloud.governance.auth.enabled|true|
|是否连接Istio获取鉴权配置| spring.cloud.istio.config.enabled|true|
|Istiod的地址| spring.cloud.istio.config.host|127.0.0.1|
|Istiod的端口| spring.cloud.istio.config.port|15012|注：连接15010端口无需TLS，连接15012端口需TLS认证|
|SCA去Istio拉取配置的线程池大小| spring.cloud.istio.config.polling-pool-size|10|
|SCA去Istio拉取配置的间隔时间| spring.cloud.istio.config.polling-time|30|单位为秒
|连接Istio<br>15012端口时使用的JWT token| spring.cloud.istio.config.istiod-token|应用所在pod的`/var/run/secrets/tokens/istio-token`文件的内容|
|是否打印xDS相关日志| spring.cloud.istio.config.log-xds|true|
### 运行应用
注意，应用运行在K8s环境中，在非默认命名空间下的应用，需要接收Istiod下发的规则，需要将运行的应用K8s的元信息注入以下环境变量中，具体操作方式可参考 [Kubernetes文档](https://kubernetes.io/zh-cn/docs/tasks/inject-data-application/environment-variable-expose-pod-information)
|环境变量名|K8s pod metadata name|
|--|--|
|POD_NAME|metadata.name|
|NAMESPACE_NAME|metadata.namespace|

**注：您部署的应用所在的pod不需要被Istio执行自动注入，因为SCA的各个治理模块将会被用来替代Envoy Proxy的各种功能。**
### 效果演示
下面给出几个简单的鉴权规则配置的示例:
#### IP黑白名单
使用如下命令通过Istio下发一条鉴权规则至demo应用，这条规则限制了访问该应用的来源IP:
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
可以通过请求本demo的auth接口来验证规则是否生效:
```
curl --location --request GET '${demo_ip}/auth'
```
在本例中，若请求的来源IP为`127.0.0.1`，则本应用返回:
```
Auth failed, please check the request and auth rule
```
说明此请求被拒绝。<br>
若请求的来源IP不为`127.0.0.1`，则本应用返回:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```
说明通过了SCA的鉴权，将会返回此请求的一些元信息。

在此之后，删除这条IP黑白名单的鉴权规则:
```shell
kubectl delete AuthorizationPolicy from-ip-allow -n ${namespace_name}
```
之后再次请求本demo的auth接口，可以发现，因为鉴权规则已被删除，所以本应用将会返回:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```

#### 请求头认证
使用如下命令通过Istio下发一条鉴权规则至demo应用，这条规则的限制了访问该应用的请求header:
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
之后发送一个带User-Agent头部的HTTP请求来验证规则是否生效:
```
curl --location --request GET '${demo_ip}/auth' \
--header 'User-Agent: PostmanRuntime/7.29.2'
```
由于此请求由于携带了正确的HTTP Header信息，将会返回:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```
之后发送一个不带User-Agent头部的HTTP请求来验证规则是否生效:
```
curl --location --request GET '${demo_ip}/auth'
```
由于此请求没有携带正确的HTTP Header信息，将会返回:
```
Auth failed, please check the request and auth rule
```
在此之后，删除这条请求头认证的规则:
```shell
kubectl delete AuthorizationPolicy http-headers-allow -n ${namespace_name}
```
之后再次请求本demo的auth接口，可以发现，因为鉴权规则已被删除，所以本应用将会返回:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```

#### JWT认证
使用如下命令通过Istio下发一条鉴权规则至demo应用，这条规则限制了访问该应用需要携带的JWT token value:
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
之后发送一个带正确JWT token的HTTP请求来验证规则是否生效:
```
curl --location --request GET '${demo_ip}/auth' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjQ2ODU5ODk3MDAsImZvbyI6ImJhciIsImlhdCI6MTUzMjM4OTcwMCwiaXNzIjoidGVzdGluZ0BzZWN1cmUuaXN0aW8uaW8iLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.CfNnxWP2tcnR9q0vxyxweaF3ovQYHYZl82hAUsn21bwQd9zP7c-LS9qd_vpdLG4Tn1A15NxfCjp5f7QNBUo-KC9PJqYpgGbaXhaGx7bEdFWjcwv3nZzvc7M__ZpaCERdwU7igUmJqYGBYQ51vr2njU9ZimyKkfDe3axcyiBZde7G6dabliUosJvvKOPcKIWPccCgefSj_GNfwIip3-SsFdlR7BtbVUcqR-yv-XOxJ3Uc1MI0tz3uMiiZcyPV7sNCU4KRnemRIMHVOfuvHsU60_GhGbiSFzgPTAa9WTltbnarTbxudb_YEOx12JiwYToeX0DCPb43W1tzIBxgm8NxUg'
```
由于此请求由于携带了正确的JWT token信息，将会返回:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```
之后再发送一个带错误JWT token的HTTP请求:
```
curl --location --request GET '${demo_ip}/auth' \
--header 'Authorization: Bearer invalid token'
```
由于此请求没有携带正确的JWT token信息，将会返回:
```
Auth failed, please check the request and auth rule
```
在此之后，删除这条JWT认证的规则:
```shell
kubectl delete RequestAuthentication jwt-jwks-uri -n ${namespace_name}
```
之后再次请求本demo的auth接口，可以发现，因为鉴权规则已被删除，所以本应用将会返回:
```
received request from ${from_ip}, local addr is ${local_ip}, local host is ${local_host}, request path is/auth
```

