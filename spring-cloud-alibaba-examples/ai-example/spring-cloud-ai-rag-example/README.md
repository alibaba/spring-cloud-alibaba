# Spring Cloud Alibaba AI RAG Example

本示例介绍如何使用 SCA AI 和 Spring AI RedisVectorStore 实现 RAG（Retrieval Augmented Generation）应用。

> RAG 是一个基于检索的生成模型，它将检索和生成结合在一起，以生成更加准确和多样化的文本。
> SCA AI: Spring Cloud Alibaba AI, 通过 Spring AI API 适配 TongYi LLM 大模型。
> Spring AI: Spring AI项目旨在简化包含人工智能功能的应用程序的开发，避免不必要的复杂性。
> Spring AI RedisVectorStore: Redis 扩展了 Redis OSS 的核心功能，允许将 Redis 用作矢量数据库，Spring AI 提供了 RedisVectorStore 适配器。
> 项目代码地址：[spring-cloud-ai-rag-example](https://github.com/alibaba/spring-cloud-alibaba/tree/2023.x/spring-cloud-alibaba-examples/ai-example/spring-cloud-ai-rag-example)

## 1. 环境准备

使用 Docker Compose 部署一个 Redis 服务，用于存储向量数据。

```yaml
version: '3.8'

services:
  redis:
    image: redis/redis-stack-server
    container_name: redis
    hostname: redis
    ports:
      - 6379:6379
```

使用 `docker compose up -d` 启动，然后您可以通过 `docker ps | grep redis` 查看容器是否正常运行。

## 2. 项目依赖

> 本项目通过引入 `spring-cloud-alibaba-ai-starter` 和 `spring-ai-redis-spring-boot-starter` 实现 RAG 应用。

您需要在 pom.xml 中引入如下依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-ai</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-redis-store-spring-boot-starter</artifactId>
    <version>${spring.ai.version}</version>
</dependency>
```

## 3. 配置

在 application.yml 中配置如下信息：

> 注意：为了保证 apiKey 安全，建议通过环境变量的方式配置 apiKey。
> 参考：https://github.com/alibaba/spring-cloud-alibaba/tree/2023.x/spring-cloud-alibaba-examples/ai-example/spring-cloud-ai-example#%E6%8E%A5%E5%85%A5-spring-cloud-starter-alibaba-ai:

```yaml
spring:
  ai:
    vectorstore:
      redis:
        # Configure the Redis connection URI, default value is redis://127.0.0.1:6379
        # uri: redis://127.0.0.1:6379
        index: peer
        prefix: peer
```

## 4. 编写代码

`loader` 类如下所示：

```java 
@Override
public void run(ApplicationArguments args) throws Exception {

    Map<String, Object> indexInfo = vectorStore.getJedis().ftInfo(properties.getIndex());
    int numDocs = Integer.parseInt((String) indexInfo.getOrDefault("num_docs", "0"));
    if (numDocs > 20000) {
        logger.info("Embeddings already loaded. Skipping");
        return;
    }

    Resource file = data;
    if (Objects.requireNonNull(data.getFilename()).endsWith(".gz")) {
        GZIPInputStream inputStream = new GZIPInputStream(data.getInputStream());
        file = new InputStreamResource(inputStream, "beers.json.gz");
    }

    logger.info("Creating Embeddings...");
    JsonReader loader = new JsonReader(file, KEYS);
    vectorStore.add(loader.get());
    logger.info("Embeddings created.");
}
```

`Service` 类如下所示：

```java
public Generation retrieve(String message) {

    SearchRequest request = SearchRequest.query(message).withTopK(topK);
    List<Document> docs = store.similaritySearch(request);

    Message systemMessage = getSystemMessage(docs);
    UserMessage userMessage = new UserMessage(message);

    Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
    ChatResponse response = client.call(prompt);

    return response.getResult();
}

private Message getSystemMessage(List<Document> similarDocuments) {

    String documents = similarDocuments.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n"));
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemBeerPrompt);

    return systemPromptTemplate.createMessage(Map.of("documents", documents));
}
```

## 5. 运行并验证

您可以通过启动 SprigBoot 主类，之后使用浏览器访问：

```shell
# 参数为 prompt，默认值为：What ber pairs well with smoked meats?"
http://localhost:8081/rag/chat
```

来体验 RAG 应用。
