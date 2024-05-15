# Spring Cloud Alibaba AI Audio

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/audio`。
`controller` 将会调用 `TongYiService` 中的 `genAudio` 方法，完成服务请求得到响应。

有一个可选的 `ptompt` 参数，其默认值为“你好，Spring Cloud Alibaba AI 框架！”。 请求响应来自 Alibaba TongYi 语音合成服务。

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl http://localhost:8080/ai/audio

# Response: 
D:\open_sources\sca-ai\spring-ai\04-11-18-21-59.wav
```

返回参数为保存到当前根路径下的音频文明路径。
