# Nacos Config Example

## Project Instruction

This example illustrates how to use Nacos Config Starter implement externalized configuration for Spring Cloud applications.

[Nacos](https://github.com/alibaba/Nacos) an easy-to-use dynamic service discovery, configuration and service management platform for building cloud native applications.

## Demo

### Connect to Nacos Config
Before we start the demo, let's learn how to connect Nacos Config to a Spring Cloud application. **Note: This section is to show you how to connect to Nacos Config. The configurations have been completed in the following example, so you don't need modify the code any more.**


1. Add dependency spring-cloud-starter-alibaba-nacos-config in the pom.xml file in your Spring Cloud project.

	    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
	
2. Add Nacos config metadata configurations to file /src/main/resources/bootstrap.properties
	
        spring.application.name=nacos-config-example
        spring.cloud.nacos.config.server-addr=127.0.0.1:8848
		  
3. After completing the above two steps, the application will get the externalized configuration from Nacos Server and put it in the Spring Environment's PropertySources.We use the @Value annotation to inject the corresponding configuration into the userName and age fields of the SampleController, and add @RefreshScope to turn on dynamic refresh .		
		@RefreshScope
		class SampleController {
	
    		@Value("${user.name}")
    		String userName;
	
    		@Value("${user.age}")
    		int age;
		}

### Start Nacos Server 

1. Install Nacos Server by downloading or build from source code.**Recommended latest version Nacos Server**

	1. Download: Download Nacos Server [download page](https://github.com/alibaba/nacos/releases) 
	2. Build from source code: Get source code by git clone git@github.com:alibaba/Nacos.git from Github Nacos and build your code. See [build reference](https://nacos.io/en-us/docs/quick-start.html) for details.
	


2. Unzip the downloaded file and go to the nacos/bin folder(), And according to the actual situation of the operating system, execute the following command。[see reference for more detail](https://nacos.io/en-us/docs/quick-start.html)。
	
	1. Linux/Unix/Mac , execute `sh startup.sh -m standalone`
	1. Windows , execute `cmd startup.cmd`

3. Execute the following command to add a configuration to Nacos Server.
	
		curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=user.id=1%0Auser.name=james%0Auser.age=17"
		
	**Note: You can also add it in other ways. If you are using the Nacos version with its own console, it is recommended to configure it directly using the console.**
	
	
	Details of the added configuration are as follows
	
		dataId is nacos-config-example.properties
		group is DEFAULT_GROUP
		
		content is
		
			user.id=1
			user.name=james
			user.age=17	

### Start Application

1. Add necessary configurations to file /src/main/resources/application.properties
	
        server.port=18084
        management.endpoints.web.exposure.include=*

		
2. Start the application in IDE or by building a fatjar.

	1. Start in IDE: Find main class `Application`, and execute the main method.
	2. Build a fatjar：Execute command `mvn clean package` to build a fatjar，and run command `java -jar nacos-config-example.jar` to start the application.

### Verification

#### Automatic Injection
Enter `http://127.0.0.1:18084/user` in the browser address bar and click Go to, we can see the data successfully obtained from Nacos Config Server.

![get](https://cdn.nlark.com/lark/0/2018/png/54319/1536986328663-5e3503c2-7e14-4c56-b5f9-72fecc6898d2.png)

#### Dynamic Refresh
1. Run the following command to modify the configuration data on the Nacos Server side.

		curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=user.id=1%0Auser.name=james%0Auser.age=18"

2. Enter `http://127.0.0.1:18084/user` in the browser address bar and click Go to,
We can see that the app got the latest data from Nacos Server and the age becomes 18.

![refresh](https://cdn.nlark.com/lark/0/2018/png/54319/1536986336535-c0efdf6d-a5d3-4f33-8d26-fe3a36cdacf6.png)


## Principle


### Nacos Config Data Structure

Nacos Config primarily determines a piece of config through dataId and group, and we assume that you already know this background. If you don't understand, please refer to [Nacos Doc](https://nacos.io/en-us/docs/concepts.html)。

Nacos Client gets data from Nacos Server through this method. `ConfigService.getConfig(String dataId, String group, long timeoutMs)`。


### Spring Cloud Retrieve Data

#### dataID

In Nacos Config Starter, the splicing format of dataId is as follows

	${prefix} - ${spring.active.profile} . ${file-extension}

* `prefix` default value is `spring.application.name` value, which can also be configured via the configuration item `spring.cloud.nacos.config.prefix`.

* `spring.active.profile` is the profile corresponding to the current environment. For details, please refer to [Spring Boot Doc](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html#boot-features-profiles)

	**Note: when the activeprofile is empty, the corresponding connector `-` will also not exist, and the splicing format of the dataId becomes `${prefix}`.`${file-extension}`**

* `file-extension` is the data format of the configuration content, which can be configured by the configuration item `spring.cloud.nacos.config.file-extension`.
Currently only the `properties` type is supported.

#### group
* `group` defaults to `DEFAULT_GROUP` and can be configured via `spring.cloud.nacos.config.group`.


### Automatic Injection
Nacos Config Starter implement `org.springframework.cloud.bootstrap.config.PropertySourceLocator` interface, and set order to 0.

In the startup phase of the Spring Cloud application, the corresponding data is obtained from the Nacos Server side, and the acquired data is converted into a PropertySource and injected into the PropertySources property of the Spring Environment. so the @Value annotation can also directly obtain the configuration of the Nacos Server side.

### Dynamic Refresh

By default, Nacos Config Starter adds a listening function to all Nacos configuration items that have successfully acquired data. It will trigger `org.springframework.cloud.context.refresh.ContextRefresher` 's refresh method in real time when it detects changes in the server configuration. 
		
If you need to dynamically refresh a bean, please refer to the Spring and Spring Cloud specifications. It is recommended to add `@RefreshScope` or `@ConfigurationProperties ` annotations to the class.

Please refer to[ContextRefresher Java Doc](http://static.javadoc.io/org.springframework.cloud/spring-cloud-context/2.0.0.RELEASE/org/springframework/cloud/context/refresh/ContextRefresher.html) for more details. 

	


## Endpoint

Nacos Config starter also supports the implementation of Spring Boot actuator endpoints.

**Prerequisite:**

Add dependency spring-boot-starter-actuator to your pom.xml file, and configure your endpoint security strategy.

Spring Boot 1.x: Add configuration management.security.enabled=false
Spring Boot 2.x: Add configuration management.endpoints.web.exposure.include=*
To view the endpoint information, visit the following URLS:

Spring Boot1.x: Nacos Config  Endpoint URL is http://127.0.0.1:18083/nacos_config.
Spring Boot2.x: Nacos Config  Endpoint URL is http://127.0.0.1:18083/actuator/nacos-config.

![actuator](https://cdn.nlark.com/lark/0/2018/png/54319/1536986344822-279e1edc-ebca-4201-8362-0ddeff240b85.png)

As shown in the figure above, Sources indicates which Nacos Config configuration items the client has obtained information, RefreshHistory indicates the dynamic refresh history, and up to 20, and NacosConfigProperties is the configuration of Nacos Config Starter itself.
    	
## More

#### More configuration items

Configuration item|key|default value|Description
----|----|-----|-----
server address|spring.cloud.nacos.config.server-addr||
DataId prefix|spring.cloud.nacos.config.prefix||spring.application.name
Group|spring.cloud.nacos.config.group|DEFAULT_GROUP|
dataID suffix|spring.cloud.nacos.config.file-extension|properties|the suffix of nacos config dataId, also the file extension of config content.
encoding |spring.cloud.nacos.config.encode|UTF-8|Content encoding
timeout|spring.cloud.nacos.config.timeout|3000|Get the configuration timeout period,unit is ms
namespace|spring.cloud.nacos.config.namespace||One of the common scenarios is the separation of the configuration of different environments, such as the development of the test environment and the resource isolation of the production environment.
AccessKey|spring.cloud.nacos.config.access-key||
SecretKey|spring.cloud.nacos.config.secret-key||
context-path|spring.cloud.nacos.config.context-path||Relative path of the server API
endpoint|spring.cloud.nacos.config.endpoint|UTF-8|The domain name of a service, through which the server address can be dynamically obtained.
refresh|spring.cloud.nacos.config.refresh.enabled|true|enable auto refresh



#### More introduction
[Nacos](https://github.com/alibaba/Nacos) is committed to help you discover, configure, and manage your microservices. It provides a set of simple and useful features enabling you to realize dynamic service discovery, service configuration, service metadata and traffic management.

Nacos makes it easier and faster to construct, deliver and manage your microservices platform. It is the infrastructure that supports a service-centered modern application architecture with a microservices or cloud-native approach.

If you have any ideas or suggestions for Nacos Config starter, please don't hesitate to tell us by submitting github issues.

