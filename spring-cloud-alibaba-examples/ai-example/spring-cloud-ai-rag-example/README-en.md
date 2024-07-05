# Spring Cloud Alibaba AI RAG Example

This sample describes how to implement a RAG (Retrieval Augmented Generation) application using SCA AI and Spring AI RedisVector Store.

> RAG is a generative model based on retrieval, which combines retrieval and generation to produce more accurate and diverse texts.
> SCA AI: Spring Cloud Alibaba AI, adapting TongYi LLM big model through Spring AI API.
> Spring AI: The Spring AI project aims to simplify the development of applications that include artificial intelligence features and avoid unnecessary complexity.
> Spring AI RedisVector Store: Redis extends the core functionality of Redis OSS to allow Redis to be used as a vector database. Spring AI provides the RedisVector Store adapter.
> Project Code Address: [spring-cloud-ai-rag-example](https://github.com/alibaba/spring-cloud-alibaba/tree/2023.x/spring-cloud-alibaba-examples/ai-example/spring-cloud-ai-rag-example)

## 1. Environmental preparation

Use Docker Compose to deploy a Redis service to store vector data.

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

Start with `docker compose up -d`, and then you can `docker ps | grep redis` check to see if the container is running properly.

## 2. Project dependency

> This project introduces `spring-cloud-alibaba-ai-starter` and `spring-ai-redis-spring-boot-starter` realizes RAG application.

You need to introduce the following dependencies in the POM. XML:

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

## 3. Configuration

Configure the following information in application.yml: > Note: It is recommended that you configure apiKey via an environment variable for apiKey security.

> Note: It is recommended to configure the apiKey via environment variables for apiKey security.
> Reference: https://github.com/alibaba/spring-cloud-alibaba/tree/2023.x/spring-cloud-alibaba-examples/ai-example/spring-cloud-ai-example#% E6%8E%A5%E5%85%A5-spring-cloud-starter-alibaba-ai:

```yaml
spring:
  ai:
    vectorstore:
      redis:
        # Configure Redis connection URI, default value is redis://127.0.0.1:6379
        # uri: redis://127.0.0.1:6379
        index: peer
        Prefix: peer
```

## 4. Write the code

The `loader` classes are as follows:

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

The `Service` classes are as follows:

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

## 5. Run and verify

You can start the SprigBoot main class and then use a browser to access:

```shell
# request params is prompt，the default value：What ber pairs well with smoked meats?"
http://localhost:8081/rag/chat
```

To experience the RAG application.
