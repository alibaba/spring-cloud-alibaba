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

![](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/images/SCSt-with-binder.png)



## 准备工作

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

### 引入依赖

修改 `pom.xml` 文件，引入 RocketMQ Stream Starter。

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>
```

## 简单示例

### 创建Topic

```sh
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t test-topic
```

### 示例代码

配置 Input 和 Output 的 Binding 信息并配合 `@EnableBinding` 注解使其生效

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

## 广播消费示例

​	广播会发送消息给所有消费者。如果你想同一消费组下所有消费者接收到同一个topic下的消息，广播消费非常适合此场景。

### 创建Topic

```sh
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t broadcast
```

### 生产者

**application.yml**

```yaml
server:
  port: 28085
spring:
  application:
    name: rocketmq-broadcast-producer-example
  cloud:
    stream:
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          producer-out-0:
            producer:
              group: output_1
      bindings:
        producer-out-0:
          destination: broadcast
logging:
  level:
    org.springframework.context.support: debug
```

**code**

使用`ApplicationRunner`和`StreamBridge`发送消息。

```java
@SpringBootApplication
public class RocketMQBroadcastProducerApplication {
   private static final Logger log = LoggerFactory
         .getLogger(RocketMQBroadcastProducerApplication.class);
   @Autowired
   private StreamBridge streamBridge;
   public static void main(String[] args) {
      SpringApplication.run(RocketMQBroadcastProducerApplication.class, args);
   }

   @Bean
   public ApplicationRunner producer() {
      return args -> {
         for (int i = 0; i < 100; i++) {
            String key = "KEY" + i;
            Map<String, Object> headers = new HashMap<>();
            headers.put(MessageConst.PROPERTY_KEYS, key);
            headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);
            Message<SimpleMsg> msg = new GenericMessage<SimpleMsg>(new SimpleMsg("Hello RocketMQ " + i), headers);
            streamBridge.send("producer-out-0", msg);
         }
      };
   }
}
```

### 消费者

启动两个消费者实例。

#### 消费者1

**application.yml**

```yaml
server:
  port: 28084
spring:
  application:
    name: rocketmq-broadcast-consumer1-example
  cloud:
    stream:
      function:
        definition: consumer;
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          consumer-in-0:
            consumer:
              messageModel: BROADCASTING
      bindings:
        consumer-in-0:
          destination: broadcast
          group: broadcast-consumer
logging:
  level:
    org.springframework.context.support: debug
```

**code**

```java
@SpringBootApplication
public class RocketMQBroadcastConsumer1Application {
   private static final Logger log = LoggerFactory
         .getLogger(RocketMQBroadcastConsumer1Application.class);

   public static void main(String[] args) {
      SpringApplication.run(RocketMQBroadcastConsumer1Application.class, args);
   }

   @Bean
   public Consumer<Message<SimpleMsg>> consumer() {
      return msg -> {
         log.info(Thread.currentThread().getName() + " Consumer1 Receive New Messages: " + msg.getPayload().getMsg());
      };
   }
}
```

#### 消费者2

**application.yml**

```yaml
server:
  port: 28083
spring:
  application:
    name: rocketmq-broadcast-consumer2-example
  cloud:
    stream:
      function:
        definition: consumer;
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          consumer-in-0:
            consumer:
              messageModel: BROADCASTING
      bindings:
        consumer-in-0:
          destination: broadcast
          group: broadcast-consumer
logging:
  level:
    org.springframework.context.support: debug
```

**code**

```java
@SpringBootApplication
public class RocketMQBroadcastConsumer2Application {
   private static final Logger log = LoggerFactory
         .getLogger(RocketMQBroadcastConsumer2Application.class);

   public static void main(String[] args) {
      SpringApplication.run(RocketMQBroadcastConsumer2Application.class, args);
   }

   @Bean
   public Consumer<Message<SimpleMsg>> consumer() {
      return msg -> {
         log.info(Thread.currentThread().getName() + " Consumer2 Receive New Messages: " + msg.getPayload().getMsg());
      };
   }
}
```

## 顺序消费示例

顺序消息（FIFO消息）是消息队列RocketMQ版提供的一种严格按照顺序来发布和消费的消息类型。

顺序消息分为两类：

- 全局顺序：对于指定的一个Topic，所有消息按照严格的先入先出FIFO（First In First Out）的顺序进行发布和消费。分区顺序：对于指定的一个Topic，所有消息根据Sharding Key进行区块分区。同一个分区内的消息按照严格的FIFO顺序进行发布和消费。Sharding Key是顺序消息中用来区分不同分区的关键字段，和普通消息的Key是完全不同的概念。

### 创建Topic

```sh
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t orderly
```

### 示例代码

**application.yml**

```yaml
server:
  port: 28082
spring:
  application:
    name: rocketmq-orderly-consume-example
  cloud:
    stream:
      function:
        definition: consumer;
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          producer-out-0:
            producer:
              group: output_1
              # 定义messageSelector
              messageQueueSelector: orderlyMessageQueueSelector
          consumer-in-0:
            consumer:
              # tag: {@code tag1||tag2||tag3 }; sql: {@code 'color'='blue' AND 'price'>100 } .
              subscription: 'TagA || TagC || TagD'
              push:
                orderly: true
      bindings:
        producer-out-0:
          destination: orderly
        consumer-in-0:
          destination: orderly
          group: orderly-consumer

logging:
  level:
    org.springframework.context.support: debug
```

**MessageQueueSelector**

选择适合自己的分区选择算法，保证同一个参数得到的结果相同。

```java
@Component
public class OrderlyMessageQueueSelector implements MessageQueueSelector {
   private static final Logger log = LoggerFactory
         .getLogger(OrderlyMessageQueueSelector.class);
   @Override
   public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
      Integer id = (Integer) ((MessageHeaders) arg).get(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID);
      String tag = (String) ((MessageHeaders) arg).get(MessageConst.PROPERTY_TAGS);
      int index = id % RocketMQOrderlyConsumeApplication.tags.length % mqs.size();
      return mqs.get(index);
   }
}
```

**生产者&消费者**

```java
@SpringBootApplication
public class RocketMQOrderlyConsumeApplication {
   private static final Logger log = LoggerFactory
         .getLogger(RocketMQOrderlyConsumeApplication.class);

   @Autowired
   private StreamBridge streamBridge;

   /***
    * tag array.
    */
   public static final String[] tags = new String[] {"TagA", "TagB", "TagC", "TagD", "TagE"};

   public static void main(String[] args) {
      SpringApplication.run(RocketMQOrderlyConsumeApplication.class, args);
   }

   @Bean
   public ApplicationRunner producer() {
      return args -> {
         for (int i = 0; i < 100; i++) {
            String key = "KEY" + i;
            Map<String, Object> headers = new HashMap<>();
            headers.put(MessageConst.PROPERTY_KEYS, key);
            headers.put(MessageConst.PROPERTY_TAGS, tags[i % tags.length]);
            headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);
            Message<SimpleMsg> msg = new GenericMessage(new SimpleMsg("Hello RocketMQ " + i), headers);
            streamBridge.send("producer-out-0", msg);
         }
      };
   }

   @Bean
   public Consumer<Message<SimpleMsg>> consumer() {
      return msg -> {
         String tagHeaderKey = RocketMQMessageConverterSupport.toRocketHeaderKey(
               MessageConst.PROPERTY_TAGS).toString();
         log.info(Thread.currentThread().getName() + " Receive New Messages: " + msg.getPayload().getMsg() + " TAG:" +
               msg.getHeaders().get(tagHeaderKey).toString());
         try {
            Thread.sleep(100);
         }
         catch (InterruptedException ignored) {
         }
      };
   }

}
```

## 延时消息示例

- 延时消息：Producer将消息发送到消息队列RocketMQ服务端，但并不期望立马投递这条消息，而是延迟一定时间后才投递到Consumer进行消费，该消息即延时消息。

### 创建Topic

```sh
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t delay
```

### 示例代码

**application.yml**

```yaml
server:
  port: 28086
spring:
  application:
    name: rocketmq-delay-consume-example
  cloud:
    stream:
      function:
        definition: consumer;
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          producer-out-0:
            producer:
              group: output_1
      bindings:
        producer-out-0:
          destination: delay
        consumer-in-0:
          destination: delay
          group: delay-group
logging:
  level:
    org.springframework.context.support: debug
```

**code**

```java
@SpringBootApplication
public class RocketMQDelayConsumeApplication {
   private static final Logger log = LoggerFactory
         .getLogger(RocketMQDelayConsumeApplication.class);
   @Autowired
   private StreamBridge streamBridge;

   public static void main(String[] args) {
      SpringApplication.run(RocketMQDelayConsumeApplication.class, args);
   }

   @Bean
   public ApplicationRunner producerDelay() {
      return args -> {
         for (int i = 0; i < 100; i++) {
            String key = "KEY" + i;
            Map<String, Object> headers = new HashMap<>();
            headers.put(MessageConst.PROPERTY_KEYS, key);
            headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);
   			// 设置延时等级1~10
            headers.put(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 2);
            Message<SimpleMsg> msg = new GenericMessage(new SimpleMsg("Delay RocketMQ " + i), headers);
            streamBridge.send("producer-out-0", msg);
         }
      };
   }

   @Bean
   public Consumer<Message<SimpleMsg>> consumer() {
      return msg -> {
         log.info(Thread.currentThread().getName() + " Consumer Receive New Messages: " + msg.getPayload().getMsg());
      };
   }
}
```

## 过滤消息示例

### 创建Topic

```sh
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t sql
```

### 示例代码

**application.yml**

支持tag过滤或者sql过滤，设置`spring.cloud.stream.rocketmq.bindings.<channelName>.consumer.subscription`即可。

tag示例: `tag:red || blue`

sql示例: `sql:(color in ('red1', 'red2', 'red4') and price>3)`

更多请参考: [Filter](https://rocketmq.apache.org/docs/filter-by-sql92-example/)

```yaml
server:
  port: 28087
spring:
  application:
    name: rocketmq-sql-consume-example
  cloud:
    stream:
      function:
        definition: consumer;
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          producer-out-0:
            producer:
              group: output_1
          consumer-in-0:
            consumer:
              # tag: {@code tag1||tag2||tag3 }; sql: {@code 'color'='blue' AND 'price'>100 } .
              subscription: sql:(color in ('red1', 'red2', 'red4') and price>3)
      bindings:
        producer-out-0:
          destination: sql
        consumer-in-0:
          destination: sql
          group: sql-group
logging:
  level:
    org.springframework.context.support: debug
```

**code**

```java
@SpringBootApplication
public class RocketMQSqlConsumeApplication {
   private static final Logger log = LoggerFactory
         .getLogger(RocketMQSqlConsumeApplication.class);
   @Autowired
   private StreamBridge streamBridge;
   public static void main(String[] args) {
      SpringApplication.run(RocketMQSqlConsumeApplication.class, args);
   }

   /**
    * color array.
    */
   public static final String[] color = new String[] {"red1", "red2", "red3", "red4", "red5"};

   /**
    * price array.
    */
   public static final Integer[] price = new Integer[] {1, 2, 3, 4, 5};

   @Bean
   public ApplicationRunner producer() {
      return args -> {
         for (int i = 0; i < 100; i++) {
            String key = "KEY" + i;
            Map<String, Object> headers = new HashMap<>();
            headers.put(MessageConst.PROPERTY_KEYS, key);
            headers.put("color", color[i % color.length]);
            headers.put("price", price[i % price.length]);
            headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);
            Message<SimpleMsg> msg = new GenericMessage(new SimpleMsg("Hello RocketMQ " + i), headers);
            streamBridge.send("producer-out-0", msg);
         }
      };
   }

   @Bean
   public Consumer<Message<SimpleMsg>> consumer() {
      return msg -> {
         String colorHeaderKey = "color";
         String priceHeaderKey = "price";
         log.info(Thread.currentThread().getName() + " Receive New Messages: " + msg.getPayload().getMsg() + " COLOR:" +
               msg.getHeaders().get(colorHeaderKey).toString() + " " +
               "PRICE: " + msg.getHeaders().get(priceHeaderKey).toString());
      };
   }
}
```

#### 常见问题

- MQClientException: The broker does not support consumer to filter message by SQL92  
1. 修改 RocketMQ 服务端配置文件。
在 `conf/2m-2s-async/broker-a.properties` 配置文件末尾添加 `enablePropertyFilter=true`  
2. 重启 mqbroker 并指定配置文件。
`mqbroker` 启动时指定配置文件：`conf/2m-2s-async/broker-a.properties`，例如：
```shell
bin/mqbroker -n 127.0.0.1:9876 -c conf/2m-2s-async/broker-a.properties autoCreateTopicEnable=true  
```

## 事务消息示例

### 什么是事务消息?

参考[Transaction Example](https://rocketmq.apache.org/docs/transaction-example/).

> 可以被认为是一个两阶段的提交消息实现，以确保分布式系统的最终一致性。 事务性消息确保本地事务的执行和消息的发送可以原子地执行。

### Application

> 1、 事务装填
>
> 事务消息有三个状态:
> (1) TransactionStatus.CommitTransaction: 提交事务，意味着消费者可以消费事务
> (2) TransactionStatus.RollbackTransaction: 回滚事务，消息将被删除，并且不允许被消费。
> (3) TransactionStatus.Unknown: 中间状态，意味着MQ需要回查最终状态。

### 创建Topic

```sh
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t tx
```

### 示例代码

**application.yml**

```yaml
server:
  port: 28088
spring:
  application:
    name: rocketmq-tx-example
  cloud:
    stream:
      function:
        definition: consumer;
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          producer-out-0:
            producer:
              group: output_1
              transactionListener: myTransactionListener
              producerType: Trans
      bindings:
        producer-out-0:
          destination: tx
        consumer-in-0:
          destination: tx
          group: tx-group
logging:
  level:
    org.springframework.context.support: debug
```

**TransactionListenerImpl**

执行本地事务。

```java
@Component("myTransactionListener")
public class TransactionListenerImpl implements TransactionListener {

	@Override
	public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
		Object num = msg.getProperty("test");

		if ("1".equals(num)) {
			System.out.println("executer: " + new String(msg.getBody()) + " unknown");
			return LocalTransactionState.UNKNOW;
		}
		else if ("2".equals(num)) {
			System.out.println("executer: " + new String(msg.getBody()) + " rollback");
			return LocalTransactionState.ROLLBACK_MESSAGE;
		}
		System.out.println("executer: " + new String(msg.getBody()) + " commit");
		return LocalTransactionState.COMMIT_MESSAGE;
	}

	@Override
	public LocalTransactionState checkLocalTransaction(MessageExt msg) {
		System.out.println("check: " + new String(msg.getBody()));
		return LocalTransactionState.COMMIT_MESSAGE;
	}

}
```

**producer and consumer**

```java
@SpringBootApplication
public class RocketMQTxApplication {
	private static final Logger log = LoggerFactory
			.getLogger(RocketMQTxApplication.class);
	@Autowired
	private StreamBridge streamBridge;
	public static void main(String[] args) {
		SpringApplication.run(RocketMQTxApplication.class, args);
	}


	@Bean
	public ApplicationRunner producer() {
		return args -> {
			for (int i = 1; i <= 4; i++) {
				MessageBuilder builder = MessageBuilder.withPayload(new SimpleMsg("Hello Tx msg " + i));
				builder.setHeader("test", String.valueOf(i))
						.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON);
				builder.setHeader(RocketMQConst.USER_TRANSACTIONAL_ARGS, "binder");
				Message<SimpleMsg> msg = builder.build();
				streamBridge.send("producer-out-0", msg);
				System.out.println("send Msg:" + msg.toString());
			}
		};
	}

	@Bean
	public Consumer<Message<SimpleMsg>> consumer() {
		return msg -> {
			Object arg = msg.getHeaders();
			log.info(Thread.currentThread().getName() + " Receive New Messages: " + msg.getPayload().getMsg() + " ARG:"
				+ arg.toString());
		};
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

