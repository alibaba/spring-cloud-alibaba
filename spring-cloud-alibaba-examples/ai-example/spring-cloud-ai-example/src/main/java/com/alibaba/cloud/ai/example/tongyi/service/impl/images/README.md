# Spring Cloud Alibaba AI Image

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/img`。
`controller` 将会调用 `TongYiService` 中的 `genImg` 方法，完成服务请求得到响应。

有一个可选的 `prompt` 参数为生成图片的提示信息，其默认值为“Painting a picture of blue water and blue sky.”。

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl http://localhost:8080/ai/img
```

响应结果为：(base64 数据太多，使用 `xxx` 代替)

```json
{
  "result": {
    "output": {
      "url": "https://dashscope-result-bj.oss-cn-beijing.aliyuncs.com/1d/42/20240408/8d820c8d/e9913e23-24e9-4de7-8977-e4ccab33a231-1.png?Expires=1712670359&OSSAccessKeyId=LTAI5tQZd8AEcZX6KZV4G8qL&Signature=bMEaNS0RGTpD2yXO0lTMMY5AWxM%3D",
      "b64Json": "xxxx"
    },
    "metadata": null
  },
  "metadata": {
    "created": 1712583967354,
    "taskId": "43d7e458-9f3e-4a51-9a39-17559d8f135d",
    "metrics": {
      "total": 4,
      "succeeded": 4,
      "failed": 0
    },
    "usage": {
      "imageCount": 4
    }
  },
  "results": [
    {
      "output": {
        "url": "https://dashscope-result-bj.oss-cn-beijing.aliyuncs.com/1d/42/20240408/8d820c8d/e9913e23-24e9-4de7-8977-e4ccab33a231-1.png?Expires=1712670359&OSSAccessKeyId=LTAI5tQZd8AEcZX6KZV4G8qL&Signature=bMEaNS0RGTpD2yXO0lTMMY5AWxM%3D",
        "b64Json": "xxxx"
      },
      "metadata": null
    },
    {
      "output": {
        "url": "https://dashscope-result-bj.oss-cn-beijing.aliyuncs.com/1d/2b/20240408/8d820c8d/0bd0b40f-4e34-46da-8706-8f2ec86274d7-1.png?Expires=1712670359&OSSAccessKeyId=LTAI5tQZd8AEcZX6KZV4G8qL&Signature=dl3sMGQn8p7y%2FzKOmPR%2B64prQV4%3D",
        "b64Json": "xxxx"
      },
      "metadata": null
    },
    {
      "output": {
        "url": "https://dashscope-result-bj.oss-cn-beijing.aliyuncs.com/1d/62/20240408/c34adf05/ffb89a14-14c5-4740-ab55-37b59a69aaef-1.png?Expires=1712670359&OSSAccessKeyId=LTAI5tQZd8AEcZX6KZV4G8qL&Signature=vYd667hVPQUTt8aiJDBFxN%2B08jI%3D",
        "b64Json": "xxxx"
      },
      "metadata": null
    },
    {
      "output": {
        "url": "https://dashscope-result-bj.oss-cn-beijing.aliyuncs.com/1d/b5/20240408/8d820c8d/594b8672-c1ce-49b6-bab0-06e3616b4e0e-1.png?Expires=1712670359&OSSAccessKeyId=LTAI5tQZd8AEcZX6KZV4G8qL&Signature=ERyoV7pmmI8sZJwSFLpzknhfFEk%3D",
        "b64Json": "xxxx"
      },
      "metadata": null
    }
  ]
}
```
