# Spring Cloud Alibaba AI Example

## 项目说明

Spring Cloud Alibaba AI 模块基于 [Spring AI 0.8.1](https://docs.spring.io/spring-ai/reference/0.8-SNAPSHOT/index.html) 项目 API 完成通义系列大模型的接入。本项目演示如何使用 `spring-cloud-starter-alibaba-ai` 完成 Spring Cloud 微服务应用与通义系列模型的整合。

[模型服务灵积](https://help.aliyun.com/zh/dashscope/) 是阿里巴巴推出的一个大模型应用服务。灵积模型服务建立在“模型即服务”（Model-as-a-Service，MaaS）的理念基础之上，围绕AI各领域模型，通过标准化的API提供包括模型推理、模型微调训练在内的多种模型服务。

- 目前只完成 spring-ai chat completion api 和 image api 的接入。

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

    > Note: 推荐使用环境变量的方式设置 api-key，避免 api-key 泄露。
    >
    > ```shell
    > export SPRING_CLOUD_AI_TONGYI_API_KEY=sk-a3d73b1709bf4a178c28ed7c8b3b5a45
    > ```

   ```yaml
   spring:
    cloud:
       ai:
         tongyi:
           # apiKey is invalid.
           api-key: sk-a3d73b1709bf4a178c28ed7c8b3b5a45
   ```
   
3. 添加如下代码：

   ```java
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

   至此，便完成了最简单的模型接入！和本 example 中的代码略有不同，但 example 中的代码无需修改。可完成对应功能。

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

## 简易前端验证

进入 `resources/static` 目录下，使用浏览器打开 index.html 文件，输入问题，即可获得输出响应（确保 api-key 有效）：

![ai-example](./images/sca-ai-example-front.gif)

## 配置项说明

更多配置项参考：

https://help.aliyun.com/zh/dashscope/developer-reference/api-details

## 更多 example 示例：

本 example 由 6 个示例组成，由不同的 serviceimpl 实现，您可以参考每个 serviceimpl 中的 readme 文件，在 controller 中使用 @RestController 作为入口点，您可以使用浏览器或者 curl 工具对接口进行请求。完成对应功能接入的练习。

> example 已完成功能，无需修改代码。只在对应的 example 中替换 apikey 即可，项目中提供的 apikey 无效。
