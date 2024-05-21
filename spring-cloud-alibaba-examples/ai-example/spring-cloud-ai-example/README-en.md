# Spring Cloud Alibaba AI Example

## Project description

The Spring Cloud Alibaba AI module is based on [Spring AI 0.8.1](https://docs.spring.io/spring-ai/reference/0.8-SNAPSHOT/index.html) the project API to complete the access of the general series of large models. This project demonstrates how to use `spring-cloud-starter-alibaba-ai` the Spring Cloud microservice application to integrate with the generic family model.

[model service dashscope](https://help.aliyun.com/zh/dashscope/) It is a big model application service launched by Alibaba. Based on the concept of "Model-as-a-Service" (MaaS), Lingji Model Service provides a variety of model services including model reasoning and model fine-tuning training through standardized APIs around AI models in various fields.

- Current completion of spring-ai chat api and image api.

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

    > Note: It is recommended to set the api-key as an environment variable to avoid api-key leakage.
    >    
    > ```shell
    > export SPRING_CLOUD_AI_TONGYI_API_KEY=sk-a3d73b1709bf4a178c28ed7c8b3b5a45
    > ```

   ```yml
   spring:
     cloud:
       ai:
         tongyi:
           # apikey is invalid.
           api-key: sk-a3d73b1709bf4a178c28ed7c8b3b5a45
   ```
   
3. Add the following code:

   ```yml
   controller:
   
   @Autowired
   @Qualifier("tongYiSimpleServiceImpl")
   private TongYiService tongYiSimpleService;
   
   @GetMapping("/example")
   public String completion(
       @RequestParam(value = "message", defaultValue = "Tell me a joke")
       String message
   ) {
   
       return tongYiSimpleService.completion(message);
   }
   
   service:
   
   private final ChatClient chatClient;
   
   
   @Autowired
   public TongYiSimpleServiceImpl(ChatClient chatClient, StreamingChatClient streamingChatClient) {
   
       this.chatClient = chatClient;
       this.streamingChatClient = streamingChatClient;
   }
   
   @Override
   public String completion(String message) {
   
       Prompt prompt = new Prompt(new UserMessage(message));
   
       return chatClient.call(prompt).getResult().getOutput().getContent();
   }
   ```

   At this point, the simplest model access is complete! It is slightly different from the code in this example, but the code in the example does not need to be modified. The corresponding function can be accomplished without modification.

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

## Simple front pages 

cd `resources/static`，open index.html file by local browser, input your question. then you get ai-models output(make api-keys effective):

![ai-example](./images/sca-ai-example-front.gif)

## Configuration item description

More configuration refer:

https://help.aliyun.com/zh/dashscope/developer-reference/api-details

## More examples:

This example consists of 6 samples, implemented by different serviceimpl, you can refer to the readme file in each serviceimpl, use @RestController as the entry point in the controller, you can use the browser or curl tool to request the interface. You can use a browser or curl tool to request the interface.

> The example is already functional and does not require any code changes. Just replace the apikey in the corresponding example, the apikey provided in the project is invalid.
