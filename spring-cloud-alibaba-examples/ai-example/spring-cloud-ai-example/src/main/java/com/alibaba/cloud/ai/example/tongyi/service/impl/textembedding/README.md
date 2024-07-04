# Spring Cloud Alibaba AI Text Embedding

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/textEmbedding`。
`controller` 将会调用 `TongYiService` 中的 `genAudio` 方法，完成服务请求得到响应。

有一个可选的 `text` 参数，其默认值为“Spring Cloud Alibaba AI 框架！”。 请求响应来自 Alibaba TongYi Text Embedding 服务。

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具或者使用浏览器对接口发起请求：

```shell
$ curl http://localhost:8080/ai/textEmbedding

# Response: 
为一组向量集合
```
