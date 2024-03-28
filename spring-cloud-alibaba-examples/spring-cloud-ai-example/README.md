# Spring Cloud Alibaba AI Example

## 项目说明

Spring Cloud Alibaba AI 模块基于 [Spring AI 0.8.0](https://docs.spring.io/spring-ai/reference/0.8-SNAPSHOT/index.html) 项目 API 完成通义系列大模型的接入。本项目演示如何使用 `spring-cloud-starter-alibaba-ai` 完成 Spring Cloud 微服务应用与通义系列模型的整合。

[模型服务灵积](https://help.aliyun.com/zh/dashscope/) 是阿里巴巴推出的一个大模型应用服务。灵积模型服务建立在“模型即服务”（Model-as-a-Service，MaaS）的理念基础之上，围绕AI各领域模型，通过标准化的API提供包括模型推理、模型微调训练在内的多种模型服务。

- 目前之完成 spring-ai

## 应用接入

### 接入 `spring-cloud-starter-alibaba-ai`

1. 在项目 pom.xml 中加入以下依赖：

   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-ai</artifactId>
   </dependency>
   ```

2. 在 application.yml 配置文件中加入以下配置：

   ```yaml
   spring:
     cloud:
       ai:
         tongyi:
           chat:
             options:
               # api_key is invalied.
               api-key: sk-a3d73b1709bf4a178c28ed7c8b3b5a45
               system-user: "You are a helpful assistant."
   ```

3. 添加如下代码：

   ```java
   controller:
   
   @GetMapping("/example")
   public Map<String, String> completion(
       @RequestParam(value = "message", defaultValue = "Tell me a joke")
       String message
   ) {
   
       return tongyiService.completion(message);
   }
   
   service:
   
   @Resource
   private MessageManager msgManager;
   
   private final ChatClient chatClient;
   
   @Autowired
   public TongYiServiceImpl(ChatClient chatClient) {
   
       this.chatClient = chatClient;
   }
   
   @Override
   public Map<String, String> completion(String message) {
   
       Message userMsg = Message.builder()
           .role(Role.USER.getValue())
           .content(message)
           .build();
       msgManager.add(userMsg);
   
       return Map.of(message, chatClient.call(message));
   }
   ```

4. 启动应用

   本 Example 项目支持如下两种启动方式：

   1. IDE 直接启动：找到主类 `TongYiApplication`，执行 main 方法启动应用。
   2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，进入 `target` 文件夹执行 `java -jar spring-cloud-ai-example.jar` 启动应用。

## 验证

浏览器地址栏输入：`http://localhost:8080/ai/example`

返回如下响应：

```json
{
    "Tell me a joke": "Sure, here's a classic one for you:\n\nWhy was the math book sad?\n\nBecause it had too many problems.\n\nI hope that made you smile! If you're looking for more, just let me know."
}
```

## 配置项说明

https://help.aliyun.com/zh/dashscope/developer-reference/api-details

## 



