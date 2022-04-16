# Nacos Config 2.4.x Example

## Project Instruction

Spring Boot version 2.4.0 does not start the bootstrap container by default. 
This project demonstrates how to use nacos when Spring boot >= 2.4.0 version does not enable the bootstrap container.

***<font color=red>Applicable to Spring boot >= 2.4.0 and import the configuration using the import method, the configuration will no longer be pulled by default, and the dataId needs to be configured manually.</font>***

## Demo

### How to use

1. First, modify the pom.xml file and introduce Nacos Config Starter
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

	
2. Configure Nacos Config metadata in the application's src/main/resources/***application.yml*** configuration file
```yaml
server:
  port: 8888
spring:
  application:
    name: nacos-config-import-example
  cloud:
    nacos:
      config:
        group: DEFAULT_GROUP
        server-addr: 127.0.0.1:8848
  config:
    import:
      - optional:nacos:test.yml
      - optional:nacos:test01.yml?group=group_02
      - optional:nacos:test02.yml?group=group_03&refreshEnabled=false
```

3. Create test.yml in nacos
```yaml
configdata:
  user:
    age: 21
    name: freeman
    map:
      hobbies:
        - art
        - programming
      intro: Hello, I'm freeman
    users:
      - name: dad
        age: 20
      - name: mom
        age: 18
```
		  
4. After completing the above operations, the application will obtain the corresponding configuration from Nacos Config and add it to the PropertySources of Spring Environment
```java
// controller
@RestController
public class UserController {

    @Autowired
    private UserConfig userConfig;

    @GetMapping
    public String get() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(userConfig);
    }
    
}

// ConfigurationProperties
@ConfigurationProperties(prefix = "configdata.user")
public class UserConfig {
    private String name;
    private Integer age;
    private Map<String, Object> map;
    private List<User> users;
    // getters and setters ...
    
    public static class User {
        private String name;
        private Integer age;
        // getters and setters ...
    }
}
```

Verify dynamic refresh 
access http://localhost:8888  
Then modify the configuration from nacos, and visit again to verify that the dynamic configuration takes effect.
