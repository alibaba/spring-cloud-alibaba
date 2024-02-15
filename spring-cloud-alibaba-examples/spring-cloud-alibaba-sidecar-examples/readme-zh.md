# Spring Cloud Alibaba Sidecar Example

## 项目说明

本项目演示如何使用 Nacos + Spring Cloud Alibaba Sidecar 完成异构微服务的接入。

[Spring Cloud Alibaba Sidecar](https://sca.aliyun.com/zh-cn/docs/2022.0.0.0/user-guide/sidecar/overview)  是一个用来快速完美整合 Spring Cloud 应用与异构微服务的框架。

## 准备工作

### 下载并启动 Nacos

**在接入 Sidecar 之前，首先需要启动 Nacos Server。**

1. 下载 [Nacos Server](https://github.com/alibaba/nacos/releases/) 并解压

2. 启动 Nacos Server
   
   下载解压，需要进入到 bin 目录启动 nacos server， 一定不要双击启动，双击默认会以集群方式启动。这里以单机方式启动：
   
   ```bash
   startup.cmd -m standalone
   ```

3. 登录 Nacos
   
   浏览器输入 localhost:8848/nacos 可以看到 Nacos 的 控制台界面，输入用户名和密码以登录 Nacos（用户名和密码都是 `nacos`）；

## 简单示例

本文以 Nacos 作为注册中心为例，Sidecar 接入一个非 Java 语言的服务。

> 默认以 golang 语言服务为示例，可以在 `application.yml` 中启动 `node` 异构服务。

### Step1: 引入依赖

修改 `pom.xml` 文件，引入 Spring Cloud Alibaba Sidecar Starter。

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sidecar</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

### Step2: 配置 Sidecar 相关信息

然后在项目的 `application.yml` 配置文件中指定以下配置：

```yaml
server:
   port: 8070

spring:
   profiles:
      active: node

   cloud:
      nacos:
         discovery:
            username: 'nacos'
            password: 'nacos'
            server-addr: 127.0.0.1:8848
      gateway:
         discovery:
            locator:
               enabled: true

   application:
      name: sidecar-service

sidecar:
   # heterogeneous microservices IP
   ip: 127.0.0.1
   # heterogeneous microservices Port
   port: 8050

   # heterogeneous microservices health url
   # health-check-url: http://localhost:8050/api/v1/health-check

management:
   endpoint:
      health:
         show-details: always
```

注意：这里的 localhost:8050，是异构服务地址。在实际使用过程中可以是任意的 REST 服务，只需要返回正确的 JSON 格式的健康检测数据即可。

```json
{
  "status": "UP"
}
```

### Step3: 启动应用

之后分别启动 Sidecar 服务、本地异构服务。

IDE 直接启动：找到主类 `com.alibaba.cloud.sidecar.DemoApplication`，执行 main 方法启动应用。

注意：本文是以 `spring-cloud-alibaba-sidecar-nacos-example` 项目为例，所以启动的是它下面的 `DemoApplication` 启动类。

![idea.png](https://cdn.nlark.com/yuque/0/2022/png/1752280/1662550869316-98d574af-d1ba-4c00-a0af-5e33e13075fd.png)

### Step4: 查看服务注册情况

![nacos.png](https://cdn.nlark.com/yuque/0/2022/png/1752280/1662548324337-566cc824-4d08-4041-ac83-1968c7347a9e.png)

### Step4: 访问异构服务

完成上面4步，我们发现对应的服务 `sidecar-service` 已经成功注册到了注册中心。此时，这个服务已经成功的融入到了 Spring Cloud 微服务的怀抱。对于 Spring Cloud 微服务而言，访问它跟访问其它的 Java 微服务没有任何区别。
而这，也正是 Spring Cloud Alibaba Sidecar 的魅力所在。接下来，将继续演示怎样访问这个服务。

浏览器访问：`http://127.0.0.1:8070/sidecar-service/test`

能调通则说明整合成功。

![img](https://cdn.nlark.com/yuque/0/2022/png/1752280/1662549893322-1b7a761a-ecd7-44ae-88b6-872eca43a866.png)

## More

如果您对 spring cloud starter alibaba sidecar 有任何建议或想法，欢迎在 Issue 中或者通过其他社区渠道向我们提出。
