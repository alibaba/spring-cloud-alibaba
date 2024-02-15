## Spring Cloud Alibaba Sidecar Example

## Project Description

This project demonstrates how to use Nacos + Spring Cloud Alibaba Sidecar to accomplish heterogeneous microservice access.

[Spring Cloud Alibaba Sidecar](https://sca.aliyun.com/zh-cn/docs/2022.0.0.0/user-guide/sidecar/overview) is a framework for quickly and seamlessly integrating Spring Cloud applications with heterogeneous microservices. services.

## Preparation

### Download and start Nacos

**Before you can access Sidecar, you need to start Nacos Server.**

1. Download [Nacos Server](https://github.com/alibaba/nacos/releases/download/2.1.0/nacos-server-2.1.0.zip) and unzip it. 2.

2. Start Nacos Server

   After downloading and unpacking, you need to go to the bin directory to start the nacos server, make sure you don't double-click to start it, double-clicking will start it as a cluster by default. Here, we will start it as a standalone server:

   ```bash
   startup.cmd -m standalone
   ```

3. Log in to Nacos

   Type localhost:8848/nacos in your browser to see the Nacos console, and enter your username and password to log in to Nacos (username and password both are `nacos`);

## Simple Example

In this article, we use Nacos as a registry as an example, and Sidecar to access a non-Java language service.

> By default, we use the golang language service as an example, and we can start the `node` heterogeneous service in `application.yml`.

### Step1: Introducing dependencies

Modify the `pom.xml` file to introduce Spring Cloud Alibaba Sidecar Starter.

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

### Step2: Configure Sidecar related information

Then specify the following configuration in the project's `application.yml` configuration file:

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

Note: localhost:8050 is the address of the heterogeneous service. In practice it can be any REST service, it just needs to return the correct JSON formatted health test data.

```json
{
  "status": "UP"
}
```

### Step3: Starting the application

After that, start the Sidecar service and local heterogeneous service respectively.

IDE direct start: find the main class `com.alibaba.cloud.sidecar.DemoApplication` and execute the main method to start the application.

Note: In this article, we take the `spring-cloud-alibaba-sidecar-nacos-example` project as an example, so we start the `DemoApplication` startup class under it.

![idea.png](https://cdn.nlark.com/yuque/0/2022/png/1752280/1662550869316-98d574af-d1ba-4c00-a0af-5e33e13075fd.png)

### Step4: Check the service registrations.

![nacos.png](https://cdn.nlark.com/yuque/0/2022/png/1752280/1662548324337-566cc824-4d08-4041-ac83-1968c7347a9e.png)

### Step4: Accessing Heterogeneous Services

After completing the above 4 steps, we find that the corresponding service `sidecar-service` has been successfully registered to the registry. At this point, this service has been successfully integrated into Spring Cloud microservices. For Spring Cloud microservices, accessing it is no different from accessing other Java microservices.
This is the beauty of Spring Cloud Alibaba Sidecar. Next, we will continue to demonstrate how to access this service.

Browser Access: `http://127.0.0.1:8070/sidecar-service/test`

The integration is successful if it works.

![img](https://cdn.nlark.com/yuque/0/2022/png/1752280/1662549893322-1b7a761a-ecd7-44ae-88b6-872eca43a866.png)

## More

If you have any suggestions or ideas about spring cloud starter alibaba sidecar, please feel free to submit them to us in an Issue or through other community channels.
