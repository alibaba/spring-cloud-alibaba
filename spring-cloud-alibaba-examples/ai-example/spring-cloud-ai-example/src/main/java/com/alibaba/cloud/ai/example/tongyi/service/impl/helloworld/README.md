# Spring Cloud Alibaba AI Hello World

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/example`。
`controller` 将会调用 `TongYiService` 中的 `completion` 方法，完成服务请求得到响应。

有一个可选的 `message` 参数，其默认值为“告诉我一个笑话”。 请求响应来自 Alibaba TongYi models 服务。

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl http://localhost:8080/ai/example

# Response: 
Sure, here's one for you:

Why don't scientists trust atoms?

Because they make up everything!
```

现在使用 message 参数：

```shell
$ curl --get  --data-urlencode 'message=Tell me a joke about a cow.' http://localhost:8080/ai/example

# Response:
Here's a classic cow joke for you:

Why did the farmer separate the chicken and the sheep from the cows?

Because he wanted to have eggs-clusive relationships with his hens!
```
