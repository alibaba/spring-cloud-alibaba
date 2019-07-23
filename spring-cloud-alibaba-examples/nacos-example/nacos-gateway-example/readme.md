#  Spring Cloud Gateway、 Nacos Discovery Example

## Project Instruction

This example illustrates how to use Nacos Discovery Starter、 Spring Cloud Gateway Starter implement Service route for Spring Cloud applications.

[Nacos](https://github.com/alibaba/Nacos) an easy-to-use dynamic service discovery, configuration and service management platform for building cloud native applications.  
[Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)  provides a library for building an API Gateway on top of Spring MVC.


## Demo

### Connect to Nacos Discovery
Before we start the demo, let's learn how to connect Nacos Config to a Spring Cloud application. **Note: This section is to show you how to connect to Nacos Discovery、Nacos Discovery、Spring Cloud Gateway. The configurations have been completed in the following example, so you don't need modify the code any more.**

1. Add Nacos Discovery Starter、Spring Cloud Gateway Starter in the pom.xml file in your Spring Cloud project.

```xml
	    <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
```
2. Add Nacos server address configurations to file /src/main/resources/application.properties.
	
		spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848

3. Add Spring Cloud Gateway configurations to file /src/main/resources/application.properties.
	
```properties
spring.cloud.gateway.routes[0].id=nacos-route
spring.cloud.gateway.routes[0].uri=lb://service-gateway-provider
spring.cloud.gateway.routes[0].predicates[0].name=Path
spring.cloud.gateway.routes[0].predicates[0].args[pattern]=/nacos/**
spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1
```	  
4. Use the @EnableDiscoveryClient annotation to turn on service registration and discovery.
		
```java
    @SpringBootApplication
    @EnableDiscoveryClient
    public class GatewayApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(GatewayApplication.class, args);
        }
    
    }
```

### Start Nacos Server 

1. Install Nacos Server by downloading or build from source code.**Recommended latest version Nacos Server**

	1. Download: Download Nacos Server [download page](https://github.com/alibaba/nacos/releases) 
	2. Build from source code: Get source code by git clone git@github.com:alibaba/Nacos.git from Github Nacos and build your code. See [build reference](https://nacos.io/en-us/docs/quick-start.html) for details.
	


2. Unzip the downloaded file and go to the nacos/bin folder(), And according to the actual situation of the operating system, execute the following command。[see reference for more detail](https://nacos.io/en-us/docs/quick-start.html)。
	
	1. Linux/Unix/Mac , execute `sh startup.sh -m standalone`
	1. Windows , execute `cmd startup.cmd`

### Start Spring Cloud Gateway Application
Start the application in IDE or by building a fatjar.

1. Start in IDE: Find main class `GatewayApplication ` in project `nacos-gateway-discovery-example`, and execute the main method.
2. Build a fatjar：Execute command `mvn clean package` in project `nacos-gateway-discovery-example` to build a fatjar，and run command `java -jar nacos-gateway-discovery-example.jar` to start the application.


### Start Service provider Application

Start the application in IDE or by building a fatjar.

1. Start in IDE: Find main class `ProviderApplication ` in project `nacos-gateway-provider-example`, and execute the main method.
2. Build a fatjar：Execute command `mvn clean package` in project `nacos-gateway-provider-example` to build a fatjar，and run command `java -jar nacos-gateway-provider-example.jar` to start the application.


### Verification
1. 
```bash
 curl 'http://127.0.0.1:18085/nacos/echo/hello-world' 
 
 hello Nacos Discovery hello-world⏎
```
1. 
```bash
 curl 'http://127.0.0.1:18085/nacos/divide?a=6&b=2' 

 3⏎              
```

#### More introduction

[Nacos ](https://github.com/alibaba/Nacos) is committed to help you discover, configure, and manage your microservices. It provides a set of simple and useful features enabling you to realize dynamic service discovery, service configuration, service metadata and traffic management.

Nacos makes it easier and faster to construct, deliver and manage your microservices platform. It is the infrastructure that supports a service-centered modern application architecture with a microservices or cloud-native approach.

If you have any ideas or suggestions for Nacos Discovery starter, please don't hesitate to tell us by submitting github issues.

