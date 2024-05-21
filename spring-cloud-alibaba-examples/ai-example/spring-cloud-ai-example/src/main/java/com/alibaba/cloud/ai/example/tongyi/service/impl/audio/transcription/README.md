# Spring Cloud Alibaba AI Audio Transcription

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/audio/transcription`
`controller` 将会调用 `TongYiService` 中的 `audioTranscription` 方法，完成服务请求得到响应。

可设置`file_urls`参数，提供一个或多个需要进行语音识别的音视频文件。

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl -X GET "http://localhost:8080/ai/audio/transcription?audioUrls=url1&audioUrls=url2"

# Response:
D:\Code\spring-cloud-alibaba\05-13-20-47-08.txt
D:\Code\spring-cloud-alibaba\05-13-20-47-09.txt
```

返回参数为保存到当前根路径下的音频转录文本文件的路径。
