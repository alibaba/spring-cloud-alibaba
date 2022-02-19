# Nacos Config 2.4.x Example

## 项目说明

Spring Boot 2.4.0 版本开始默认不启动 bootstrap 容器
本项目演示如何在 Spring boot >= 2.4.0 版本不启用 bootstrap 容器情况下如何使用 nacos

[Nacos](https://github.com/alibaba/Nacos) 是阿里巴巴开源的一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。

## 示例

### 如何接入

1. 首先，修改 pom.xml 文件，引入 Nacos Config Starter
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
	<!-- 不再需要 bootstrap 容器 -->
    <exclusions>
        <exclusion>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

	
2. 在应用的 /src/main/resources/***application.yml*** 配置文件中配置 Nacos Config 元数据
```yaml
server:
  port: 8888
spring:
  application:
    name: nacos-config-import-example
  cloud:
    nacos:
      config:
        name: test.yml
        file-extension: yml
  # 2.4.0 新增配置 spring.config.import
  config:
    import:
      - optional:nacos:127.0.0.1:8848
```

3. 在 nacos 创建 test.yml 
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
		  
4. 完成上述操作后，应用会从 Nacos Config 中获取相应的配置，并添加在 Spring Environment 的 PropertySources 中
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

验证动态刷新  
访问 http://localhost:8888  
再从 nacos 修改配置, 再次访问即可验证动态配置生效
