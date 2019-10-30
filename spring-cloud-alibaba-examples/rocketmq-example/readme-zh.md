# RocketMQ Example

## 项目说明

本项目演示如何使用 RocketMQ Binder 完成 Spring Cloud 应用消息的订阅和发布。

[RocketMQ](https://rocketmq.apache.org/) 是一款开源的分布式消息系统，基于高可用分布式集群技术，提供低延时的、高可靠的消息发布与订阅服务。

在说明 RocketMQ 的示例之前，我们先了解一下 Spring Cloud Stream。

这是官方对 Spring Cloud Stream 的一段介绍：

Spring Cloud Stream 是一个用于构建基于消息的微服务应用框架。它基于 SpringBoot 来创建具有生产级别的单机 Spring 应用，并且使用 `Spring Integration` 与 Broker 进行连接。
 
Spring Cloud Stream 提供了消息中间件配置的统一抽象，推出了 publish-subscribe、consumer groups、partition 这些统一的概念。

Spring Cloud Stream 内部有两个概念：Binder 和 Binding。

* Binder: 跟外部消息中间件集成的组件，用来创建 Binding，各消息中间件都有自己的 Binder 实现。

比如 `Kafka` 的实现 `KafkaMessageChannelBinder`，`RabbitMQ` 的实现 `RabbitMessageChannelBinder` 以及 `RocketMQ` 的实现 `RocketMQMessageChannelBinder`。

* Binding: 包括 Input Binding 和 Output Binding。

Binding 在消息中间件与应用程序提供的 Provider 和 Consumer 之间提供了一个桥梁，实现了开发者只需使用应用程序的 Provider 或 Consumer 生产或消费数据即可，屏蔽了开发者与底层消息中间件的接触。

下图是 Spring Cloud Stream 的架构设计。

![](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/images/SCSt-overview.png)

## 示例

### 如何接入

在启动示例进行演示之前，我们先了解一下 Spring Cloud 应用如何接入 RocketMQ Binder。

> **注意：本章节只是为了便于您理解接入方式，本示例代码中已经完成****接入工作，您无需再进行修改。**

1. 首先，修改 `pom.xml` 文件，引入 RocketMQ Stream Starter。

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>
```

2. 配置 Input 和 Output 的 Binding 信息并配合 `@EnableBinding` 注解使其生效

```java
@SpringBootApplication
@EnableBinding({ Source.class, Sink.class })
public class RocketMQApplication {
	public static void main(String[] args) {
		SpringApplication.run(RocketMQApplication.class, args);
	}
}
```

配置 Binding 信息：
```properties
# 配置rocketmq的nameserver地址
spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876
# 定义name为output的binding
spring.cloud.stream.bindings.output.destination=test-topic
spring.cloud.stream.bindings.output.content-type=application/json
# 定义name为input的binding
spring.cloud.stream.bindings.input.destination=test-topic
spring.cloud.stream.bindings.input.content-type=application/json
spring.cloud.stream.bindings.input.group=test-group

```
	
3. 消息发送及消息订阅

### 下载并启动 RocketMQ

**在接入 RocketMQ Binder 之前，首先需要启动 RocketMQ 的 Name Server 和 Broker。**

1. 下载[RocketMQ最新的二进制文件](https://www.apache.org/dyn/closer.cgi?path=rocketmq/4.3.2/rocketmq-all-4.3.2-bin-release.zip)，并解压

2. 启动 Name Server

```bash
sh bin/mqnamesrv
```

3. 启动 Broker

```bash
sh bin/mqbroker -n localhost:9876
```

4. 创建 Topic: test-topic

```bash
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t test-topic
```

### 应用启动

1. 增加配置，在应用的 /src/main/resources/application.properties 中添加基本配置信息
	
```properties
spring.application.name=rocketmq-example
server.port=28081
```
	
2. 启动应用，支持 IDE 直接启动和编译打包后启动。

	1. IDE 直接启动：找到主类 `RocketMQApplication`，执行 main 方法启动应用。
	2. 打包编译后启动：首先执行 `mvn clean package` 将工程编译打包，然后执行 `java -jar rocketmq-example.jar` 启动应用。


### 消息处理

使用 name 为 output 对应的 binding 发送消息到 test-topic 这个 topic。

使用2个 input binding 订阅数据。

* input1: 订阅 topic 为 test-topic 的消息，顺序消费所有消息(顺序消费的前提是所有消息都在一个 MessageQueue 中)

* input2: 订阅 topic 为 test-topic 的消息，异步消费 tags 为 tagStr 的消息，Consumer 端线程池个数为20

配置信息如下：

```properties
spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876

spring.cloud.stream.bindings.output.destination=test-topic
spring.cloud.stream.bindings.output.content-type=application/json

spring.cloud.stream.bindings.input1.destination=test-topic
spring.cloud.stream.bindings.input1.content-type=text/plain
spring.cloud.stream.bindings.input1.group=test-group1
spring.cloud.stream.rocketmq.bindings.input1.consumer.orderly=true

spring.cloud.stream.bindings.input2.destination=test-topic
spring.cloud.stream.bindings.input2.content-type=text/plain
spring.cloud.stream.bindings.input2.group=test-group2
spring.cloud.stream.rocketmq.bindings.input2.consumer.orderly=false
spring.cloud.stream.rocketmq.bindings.input2.consumer.tags=tagStr
spring.cloud.stream.bindings.input2.consumer.concurrency=20

```

#### 消息发送

使用 MessageChannel 进行消息发送：

```java
public class ProducerRunner implements CommandLineRunner {
    @Autowired
    private MessageChannel output; // 获取name为output的binding
    @Override
    public void run(String... args) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageConst.PROPERTY_TAGS, "tagStr");
        Message message = MessageBuilder.createMessage(msg, new MessageHeaders(headers));
        output.send(message);
    }
}
```

或者使用 RocketMQ 原生的 API 进行消息发送:

```java
public class RocketMQProducer {
    DefaultMQProducer producer = new DefaultMQProducer("producer_group");
    producer.setNamesrvAddr("127.0.0.1:9876");
    producer.start();
    
    Message msg = new Message("test-topic", "tagStr", "message from rocketmq producer".getBytes());
    producer.send(msg);
}
```

#### 消息接收

使用 `@StreamListener` 注解接收消息：

```java
@Service
public class ReceiveService {

	@StreamListener("input1")
	public void receiveInput1(String receiveMsg) {
		System.out.println("input1 receive: " + receiveMsg);
	}

	@StreamListener("input2")
	public void receiveInput2(String receiveMsg) {
		System.out.println("input2 receive: " + receiveMsg);
	}

}
```

## Endpoint 信息查看

Spring Boot 应用支持通过 Endpoint 来暴露相关信息，RocketMQ Stream Starter 也支持这一点。

在使用之前需要在 Maven 中添加 `spring-boot-starter-actuator`依赖，并在配置中允许 Endpoints 的访问。
* Spring Boot 1.x 中添加配置 `management.security.enabled=false`
* Spring Boot 2.x 中添加配置 `management.endpoints.web.exposure.include=*`

Spring Boot 1.x 可以通过访问 http://127.0.0.1:18083/rocketmq_binder 来查看 RocketMQ Binder Endpoint 的信息。Spring Boot 2.x 可以通过访问 http://127.0.0.1:28081/actuator/rocketmq-binder 来访问。

这里会统计消息最后一次发送的数据，消息发送成功或失败的次数，消息消费成功或失败的次数等数据。

```json
{
	"runtime": {
		"lastSend.timestamp": 1542786623915
	},
	"metrics": {
		"scs-rocketmq.consumer.test-topic.totalConsumed": {
			"count": 11
		},
		"scs-rocketmq.consumer.test-topic.totalConsumedFailures": {
			"count": 0
		},
		"scs-rocketmq.producer.test-topic.totalSentFailures": {
			"count": 0
		},
		"scs-rocketmq.consumer.test-topic.consumedPerSecond": {
			"count": 11,
			"fifteenMinuteRate": 0.012163847780107841,
			"fiveMinuteRate": 0.03614605351360527,
			"meanRate": 0.3493213353657594,
			"oneMinuteRate": 0.17099243039490175
		},
		"scs-rocketmq.producer.test-topic.totalSent": {
			"count": 5
		},
		"scs-rocketmq.producer.test-topic.sentPerSecond": {
			"count": 5,
			"fifteenMinuteRate": 0.005540151995103271,
			"fiveMinuteRate": 0.01652854617838251,
			"meanRate": 0.10697493212602836,
			"oneMinuteRate": 0.07995558537067671
		},
		"scs-rocketmq.producer.test-topic.sentFailuresPerSecond": {
			"count": 0,
			"fifteenMinuteRate": 0.0,
			"fiveMinuteRate": 0.0,
			"meanRate": 0.0,
			"oneMinuteRate": 0.0
		},
		"scs-rocketmq.consumer.test-topic.consumedFailuresPerSecond": {
			"count": 0,
			"fifteenMinuteRate": 0.0,
			"fiveMinuteRate": 0.0,
			"meanRate": 0.0,
			"oneMinuteRate": 0.0
		}
	}
}
```

注意：要想查看统计数据需要在pom里加上 [metrics-core依赖](https://mvnrepository.com/artifact/io.dropwizard.metrics/metrics-core)。如若不加，endpoint 将会显示 warning 信息而不会显示统计信息：

```json
{
    "warning": "please add metrics-core dependency, we use it for metrics"
}
```

## More

RocketMQ 是一款功能强大的分布式消息系统，广泛应用于多个领域，包括异步通信解耦、企业解决方案、金融支付、电信、电子商务、快递物流、广告营销、社交、即时通信、移动应用、手游、视频、物联网、车联网等。

此 Demo 仅演示了 RocketMQ 与 Spring Cloud Stream 结合后的使用，更多 RocketMQ 相关的信息，请参考 [RocketMQ 项目](https://github.com/apache/rocketmq)。

如果您对 spring cloud starter stream rocketmq 有任何建议或想法，欢迎在 issue 中或者通过其他社区渠道向我们提出。

