# Nacos Config Example

## Project Instruction

This example illustrates how to use Nacos Config Starter implement externalized configuration for Spring Cloud applications.

[Nacos](https://github.com/alibaba/Nacos) an easy-to-use dynamic service discovery, configuration and service management platform for building cloud native applications.

## Demo

### Connect to Nacos Config
Before we start the demo, let's learn how to connect Nacos Config to a Spring Cloud application. **Note: This section is to show you how to connect to Nacos Config. The configurations have been completed in the following example, so you don't need to modify the code anymore.**


1. Add dependency spring-cloud-starter-alibaba-nacos-config in the pom.xml file in your Spring Cloud project.

	    <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
	
2. Add Nacos config metadata configurations to file /src/main/resources/bootstrap.properties
	
        spring.application.name=nacos-config-example
        spring.cloud.nacos.config.server-addr=127.0.0.1:8848

3. After completing the above two steps, the application will obtain the corresponding configuration from Nacos Config and add it to the PropertySources of Spring Environment. Suppose we save part of the configuration of Nacos through the Nacos configuration center, there are the following four examples:
- BeanAutoRefreshConfigExample: An example that supports automatic refresh of configuration changes by configuring configuration information as beans
- ConfigListenerExample: Example of listening configuration information
- DockingInterfaceExample: An example of docking the nacos interface and completing the addition, deletion, modification and checking of configuration information through the interface
- ValueAnnotationExample: An example of obtaining configuration information through @Value annotation
- SharedConfigExample:           Example of shared configuration
- ExtensionConfigExample:        Example of extended configuration

### Start Nacos Server 

1. Install Nacos Server by downloading or build from source code.**Recommended latest version Nacos Server**

	1. Download: Download Nacos Server [download page](https://github.com/alibaba/nacos/releases) 
	2. Build from source code: Get source code by git clone git@github.com:alibaba/Nacos.git from GitHub Nacos and build your code. See [build reference](https://nacos.io/en-us/docs/quick-start.html) for details.
	
2. Unzip the downloaded file and go to the nacos/bin folder(), And according to the actual situation of the operating system, execute the following command。[see reference for more detail](https://nacos.io/en-us/docs/quick-start.html)。
	
	1. Linux/Unix/Mac , execute `sh startup.sh -m standalone`
	2. Windows , execute `cmd startup.cmd -m standalone`

3. Execute the following command to add a configuration to Nacos Server.

   	curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=spring.cloud.nacos.config.serverAddr=127.0.0.1:8848%0Aspring.cloud.nacos.config.prefix=PREFIX%0Aspring.cloud.nacos.config.group=GROUP%0Aspring.cloud.nacos.config.namespace=NAMESPACE"
		
	**Note: You can also add it in other ways. If you are using the Nacos version with its own console, it is recommended to configure it directly using the console.**
	
	
	Details of the added configuration are as follows
	
		dataId is nacos-config-example.properties
		group is DEFAULT_GROUP
		
		content is:
		
   		spring.cloud.nacos.config.serverAddr=127.0.0.1:8848
	    spring.cloud.nacos.config.prefix=PREFIX
        spring.cloud.nacos.config.group=GROUP
        spring.cloud.nacos.config.namespace=NAMESPACE

4. Add shared configuration and extended configuration

   Shared configuration:
   ```
      dataId is: data-source.yaml
      group is： DEFAULT_GROUP
		
      content is:
		
      spring:
       datasource:
        name: datasource
        url: jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=convertToNull&useDynamicCharsetInfo=false&useSSL=false
        username: root
        password: root
        driverClassName: com.mysql.jdbc.Driver
   ```
   Extended configuration:
   > Configuration in shared configuration can be overridden with extended configuration
   ```
      dataId is: ext-data-source.yaml
      group is： DEFAULT_GROUP
		
      content is:
		
      spring:
       datasource:
        username: ext-root
        password: ext-root
   ```


### Start Application

1. Add necessary configurations to file /src/main/resources/application.properties
	
        server.port=18084
        management.endpoints.web.exposure.include=*

		
2. Start the application in IDE or by building a fatjar.

	1. Start in IDE: Find main class `Application`, and execute the main method.
	2. Build a fatjar：Execute command `mvn clean package` to build a fatjar，and run command `java -jar nacos-config-example.jar` to start the application.

### Verification

#### Automatic Injection
Enter `http://127.0.0.1:18084/nacos/bean` in the browser address bar and click Go to, we can see the data successfully obtained from Nacos Config Server.

![get](https://tva1.sinaimg.cn/large/e6c9d24ely1h2gbowleyrj20o40bo753.jpg)

#### Dynamic Refresh
1. Run the following command to modify the configuration data on the Nacos Server side.

   	curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=nacos-config-example.properties&group=DEFAULT_GROUP&content=spring.cloud.nacos.config.serveraddr=127.0.0.1:8848%0Aspring.cloud.nacos.config.prefix=PREFIX%0Aspring.cloud.nacos.config.group=DEFAULT_GROUP%0Aspring.cloud.nacos.config.namespace=NAMESPACE"

2. Enter `http://127.0.0.1:18084/nacos/bean` in the address bar of the browser, and click Flip, you can see that the application has obtained the latest data from Nacos Server, and the group has become DEFAULT_GROUP.

![refresh](https://tva1.sinaimg.cn/large/e6c9d24ely1h2gbpram9rj20nq0ccmxz.jpg)


## Principle


### Nacos Config Data Structure

Nacos Config primarily determines a piece of config through dataId and group, and we assume that you already know this background. If you don't understand, please refer to [Nacos Doc](https://nacos.io/en-us/docs/concepts.html)。

Nacos Client gets data from Nacos Server through this method. `ConfigService.getConfig(String dataId, String group, long timeoutMs)`。


### Spring Cloud Retrieve Data

#### dataID

In Nacos Config Starter, the splicing format of dataId is as follows

	${prefix} - ${spring.profiles.active} . ${file-extension}

* `prefix` default value is `spring.application.name` value, which can also be configured via the configuration item `spring.cloud.nacos.config.prefix`.

* `spring.profiles.active` is the profile corresponding to the current environment. For details, please refer to [Spring Boot Doc](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html#boot-features-profiles)

	**Note: when the active profile is empty, the corresponding connector `-` will also not exist, and the splicing format of the dataId becomes `${prefix}`.`${file-extension}`**

* `file-extension` is the data format of the configuration content, which can be configured by the configuration item `spring.cloud.nacos.config.file-extension`.
Currently, only the `properties` type is supported.

#### group
* `group` defaults to `DEFAULT_GROUP` and can be configured via `spring.cloud.nacos.config.group`.


### Automatic Injection
Nacos Config Starter implement `org.springframework.cloud.bootstrap.config.PropertySourceLocator` interface, and set order to 0.

In the startup phase of the Spring Cloud application, the corresponding data is obtained from the Nacos Server side, and the acquired data is converted into a PropertySource and injected into the PropertySources property of the Spring Environment. so the @Value annotation can also directly obtain the configuration of the Nacos Server side.

### Dynamic Refresh

By default, Nacos Config Starter adds a listening function to all Nacos configuration items that have successfully acquired data. It will trigger `org.springframework.cloud.context.refresh.ContextRefresher` 's refresh method in real time when it detects changes in the server configuration. 
		
If you need to dynamically refresh a bean, please refer to the Spring and Spring Cloud specifications. It is recommended to add `@RefreshScope` or `@ConfigurationProperties ` annotations to the class.

Please refer to [ContextRefresher Java Doc](http://static.javadoc.io/org.springframework.cloud/spring-cloud-context/2.0.0.RELEASE/org/springframework/cloud/context/refresh/ContextRefresher.html) for more details. 

	


## Endpoint

Nacos Config starter also supports the implementation of Spring Boot actuator endpoints.

**Prerequisite:**

Add dependency spring-boot-starter-actuator to your pom.xml file, and configure your endpoint security strategy.

Spring Boot 1.x: Add configuration management.security.enabled=false
Spring Boot 2.x: Add configuration management.endpoints.web.exposure.include=*
To view the endpoint information, visit the following URLS:

Spring Boot 1.x: Nacos Config  Endpoint URL is http://127.0.0.1:18084/nacos_config.
Spring Boot 2.x: Nacos Config  Endpoint URL is http://127.0.0.1:18084/actuator/nacosconfig.

![actuator](https://cdn.nlark.com/lark/0/2018/png/54319/1536986344822-279e1edc-ebca-4201-8362-0ddeff240b85.png)

As shown in the figure above, Sources indicates which Nacos Config configuration items the client has obtained information, RefreshHistory indicates the dynamic refresh history, and up to 20, and NacosConfigProperties is the configuration of Nacos Config Starter itself.
    	
## More

#### More configuration items

Configuration item|key|default value|Description
----|----|-----|-----
server address|spring.cloud.nacos.config.server-addr||server id and port
DataId prefix|spring.cloud.nacos.config.prefix|${spring.application.name}|the prefix of nacos config DataId
Group|spring.cloud.nacos.config.group|DEFAULT_GROUP|
DataID suffix|spring.cloud.nacos.config.file-extension|properties|the suffix of nacos config DataId, also the file extension of config content.
encoding |spring.cloud.nacos.config.encode|UTF-8|Content encoding
timeout|spring.cloud.nacos.config.timeout|3000|Get the configuration timeout period,unit is ms
namespace|spring.cloud.nacos.config.namespace||One of the common scenarios is the separation of the configuration of different environments, such as the development of the test environment and the resource isolation of the production environment.
AccessKey|spring.cloud.nacos.config.access-key||
SecretKey|spring.cloud.nacos.config.secret-key||
context-path|spring.cloud.nacos.config.context-path||Relative path of the server API
endpoint|spring.cloud.nacos.config.endpoint||The domain name of a service, through which the server address can be dynamically obtained.
refresh|spring.cloud.nacos.config.refresh.enabled|true|enable auto refresh
cluster name|spring.cloud.nacos.config.cluster-name||



#### More introduction
[Nacos](https://github.com/alibaba/Nacos) is committed to help you discover, configure, and manage your microservices. It provides a set of simple and useful features enabling you to realize dynamic service discovery, service configuration, service metadata and traffic management.

Nacos makes it easier and faster to construct, deliver and manage your microservices platform. It is the infrastructure that supports a service-centered modern application architecture with a microservices or cloud-native approach.

If you have any ideas or suggestions for Nacos Config starter, please don't hesitate to tell us by submitting GitHub issues.

