# Spring Cloud Alibaba AI Stuff models

`TongYiController` 接受一个 HTTP GET 请求 `http://localhost:8080/ai/stuff`。
`controller` 将会调用 `TongYiService` 中的 `stuffCompletion` 方法，完成服务请求得到响应。

模型训练数据仅仅到 2021 年，对之后的问题无法回答。此示例将演示如何使用一些数据完成对模型上下文的填充，使得模型能够回答更多的问题。
将以 2022 年的冰壶比赛冠军是谁为例进行演示。

```shell
$  curl http://localhost:8080/ai/stuff
{"completion":"As previously mentioned, my knowledge is current until 2021 and I do not have information about the 2022 Winter Olympics results. Therefore, I can't provide the winner of the mixed doubles gold medal in curling from those games."}
```

## 预处理内容

###  qa-prompt.st

```json
Use the following pieces of context to answer the question at the end.
If you don't know the answer, just say that you don't know, don't try to make up an answer.

{context}

Question: {question}
Helpful Answer:
```

```md
Jack and arokg, slim won the gold medal in mixed doubles curling at the 2022 Winter Olympics.
```

以上内容将由 Spring 加载到 Resource 对象中：

```java
@Value("classpath:/docs/wikipedia-curling.md")
private Resource docsToStuffResource;

@Value("classpath:/prompts/qa-prompt.st")
private Resource qaPromptResource;
```

一并填充到 Prompt 中，由 Alibaba TongYi models 返回响应。

## 构建和运行

1. 修改配置文件 `application.yml` 中的 apikey 为有效的 apikey；
2. 通过 IDE 或者 `./mvnw spring-boot:run` 运行应用程序。

## 访问接口

使用 curl 工具对接口发起请求：

```shell
$ curl http://localhost:8080/ai/stuff

# Response: 
{"completion":"I'm sorry, but I don't have information on the specific winners of the 2022 Winter Olympics events. My knowledge is current until 2021, and I cannot provide real-time or future sports results."}
```

现在使用 stuffit 参数：

```shell
$  curl --get  --data-urlencode 'stuffit=true' http://localhost:8080/ai/stuff

# Response:
{"completion":"The athletes who won the mixed doubles gold medal in curling at the 2022 Winter Olympics, according to the provided context, were Jack and Arokg Slim."}
```
