# Spring Cloud Alibaba AI Example

## Project description

The Spring Cloud Alibaba AI module is based on [Spring AI 0.8.0](https://docs.spring.io/spring-ai/reference/0.8-SNAPSHOT/index.html) the project API to complete the access of the general series of large models. This project demonstrates how to use `spring-cloud-starter-alibaba-ai` the Spring Cloud microservice application to integrate with the generic family model.

[model service dashscope](https://help.aliyun.com/zh/dashscope/) It is a big model application service launched by Alibaba. Based on the concept of "Model-as-a-Service" (MaaS), Lingji Model Service provides a variety of model services including model reasoning and model fine-tuning training through standardized APIs around AI models in various fields.

- Current completion of spring-ai

## Application access

### Access `spring-cloud-starter-alibaba-ai`

1. Add the following dependencies to the project POM. XML:


   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-ai</artifactId>
   </dependency>
   ```

2. Add the following configuration to the application. Yml configuration file:


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

3. Add the following code:


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

4. Start the application

   This Example project supports the following two startup methods:

   1. IDE direct startup: find the main class `TongYiApplication` and execute the main method to start the application.
   2. Start after packaging and compiling: First `mvn clean package`, compile and package the project, and then enter the `target` folder to `java -jar spring-cloud-ai-example.jar` start the application.

## Validate

Browser address bar input: `http://localhost:8080/ai/example`

The following response is returned:


```json
{
    "Tell me a joke": "Sure, here's a classic one for you:\n\nWhy was the math book sad?\n\nBecause it had too many problems.\n\nI hope that made you smile! If you're looking for more, just let me know."
}
```

## Configuration item description

https://help.aliyun.com/zh/dashscope/developer-reference/api-details



