# Spring Cloud Alibaba AI Prompt Template

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/prompt-tmpl`。
`controller` 将会调用 `TongYiService` 中的 `genPromptTemplates` 方法，完成服务请求得到响应。

本示例代码展示如何使用 `StringTemplate` 引擎和 `Spring AI PromptTemplate` 类。目录中 `resources\prompts` 文件为 `joke-prompt`。此文件由 Spring 加载：

```java
@Value("classpath:/prompts/joke-prompt.st")
private Resource jokeResource;
```

文件内容为：

```json
Tell me a {adjective} joke about {topic}.
```

接受两个可选参数：

1. `adjective`，其默认值为 `funny`
2. `topic`，其默认值为 `cows`

请求响应来自 Alibaba TongYi models.

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl http://localhost:8080/ai/prompt-tmpl
```

响应结果为：

```json
{
  "messageType": "ASSISTANT",
  "properties": {},
  "content": "Why did the cow go to art school? Because she wanted to learn how to draw moo-vements!",
  "media": []
}
```

现在使用 adjective 和 topic 参数：

```shell
$ curl --get --data-urlencode message='Tell me about 3 famous physicists' --data-urlencode name='yuluo' --data-urlencode voice='Rick Sanchez' http://localhost:8080/ai/roles
```

Response：

```json
{
  "messageType": "ASSISTANT",
  "properties": {},
  "content": "Sure, here's another one:\n\nWhy did the farmer separate the chicken and the cow?\n\nBecause he wanted to have eggs-clusive relationships with his hens!",
  "media": []
}
```
