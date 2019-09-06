# RocketMQ Example

## Project Instruction

This example illustrates how to use RocketMQ Binder implement pub/sub messages for Spring Cloud applications.

[RocketMQ](https://rocketmq.apache.org/) is a distributed messaging and streaming platform with low latency, high performance and reliability, trillion-level capacity and flexible scalability.

Before we start the demo, let's look at Spring Cloud Stream.

Spring Cloud Stream is a framework for building message-driven microservice applications. Spring Cloud Stream builds upon Spring Boot to create standalone, production-grade Spring applications and uses Spring Integration to provide connectivity to message brokers. It provides opinionated configuration of middleware from several vendors, introducing the concepts of persistent publish-subscribe semantics, consumer groups, and partitions.

There are two concepts in Spring Cloud Stream: Binder 和 Binding.

* Binder: A strategy interface used to bind an app interface to a logical name.

Binder Implementations includes `KafkaMessageChannelBinder` of kafka, `RabbitMessageChannelBinder` of RabbitMQ and `RocketMQMessageChannelBinder` of `RocketMQ`.  

* Binding: Including Input Binding and Output Binding.

Binding is Bridge between the external messaging systems and application provided Producers and Consumers of messages.

This is a overview of Spring Cloud Stream.

![](https://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/images/SCSt-overview.png)

## Demo

### Integration with RocketMQ Binder

Before we start the demo, let's learn how to Integration with RocketMQ Binder to a Spring Cloud application.

**Note: This section is to show you how to connect to Sentinel. The configurations have been completed in the following example, so you don't need modify the code any more.**

1. Add dependency spring-cloud-starter-stream-rocketmq in the pom.xml file in your Spring Cloud project.

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
</dependency>
```

2. Configure Input and Output Binding and cooperate with `@EnableBinding` annotation

```java
@SpringBootApplication
@EnableBinding({ Source.class, Sink.class })
public class RocketMQApplication {
	public static void main(String[] args) {
		SpringApplication.run(RocketMQApplication.class, args);
	}
}
```

Configure Binding:
```properties
# configure the nameserver of rocketmq
spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876
# configure the output binding named output
spring.cloud.stream.bindings.output.destination=test-topic
spring.cloud.stream.bindings.output.content-type=application/json
# configure the input binding named input
spring.cloud.stream.bindings.input.destination=test-topic
spring.cloud.stream.bindings.input.content-type=application/json
spring.cloud.stream.bindings.input.group=test-group

```
	
3. pub/sub messages

### Download and Startup RocketMQ

You should startup Name Server and Broker before using RocketMQ Binder.

1. Download [RocketMQ](https://www.apache.org/dyn/closer.cgi?path=rocketmq/4.3.2/rocketmq-all-4.3.2-bin-release.zip) and unzip it.

2. Startup Name Server

```bash
sh bin/mqnamesrv
```

3. Startup Broker

```bash
sh bin/mqbroker -n localhost:9876
```

4. Create topic: test-topic

```bash
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t test-topic
```

### Start Application

1. Add necessary configurations to file `/src/main/resources/application.properties`.
	
```properties
spring.application.name=rocketmq-example
server.port=28081
```
	
2. Start the application in IDE or by building a fatjar.

	1. Start in IDE: Find main class  `RocketMQApplication`, and execute the main method.
    2. Build a fatjar: Execute command `mvn clean package` to build a fatjar, and run command `java -jar rocketmq-example.jar` to start the application.


### Message Handle

Using the binding named output and sent messages to `test-topic` topic.

And using two input bindings to subscribe messages.

* input1: subscribe the message of `test-topic` topic and consume ordered messages(all messages should in the same MessageQueue if you want to consuming ordered messages).

* input2: subscribe the message of `test-topic` topic and consume concurrent messages which tags is `tagStr`, the thread number in pool is 20 in Consumer side.

see the configuration below:

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

#### Pub Messages

Using MessageChannel to send messages:

```java
public class ProducerRunner implements CommandLineRunner {
    @Autowired
    private MessageChannel output;
    @Override
    public void run(String... args) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageConst.PROPERTY_TAGS, "tagStr");
        Message message = MessageBuilder.createMessage(msg, new MessageHeaders(headers));
        output.send(message);
    }
}
```

Or you can using the native API of RocketMQ to send messages:

```java
public class RocketMQProducer {
    DefaultMQProducer producer = new DefaultMQProducer("producer_group");
    producer.setNamesrvAddr("127.0.0.1:9876");
    producer.start();
    
    Message msg = new Message("test-topic", "tagStr", "message from rocketmq producer".getBytes());
    producer.send(msg);
}
```

#### Sub Messages

Using `@StreamListener` to receive messages:

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

## Endpoint

Add dependency `spring-cloud-starter-stream-rocketmq` to your pom.xml file, and configure your endpoint security strategy.

* Spring Boot1.x: Add configuration `management.security.enabled=false`    
* Spring Boot2.x: Add configuration `management.endpoints.web.exposure.include=*`

To view the endpoint information, visit the following URLS:
* Spring Boot1.x: Sentinel Endpoint URL is http://127.0.0.1:18083/rocketmq_binder.
* Spring Boot2.x: Sentinel Endpoint URL is http://127.0.0.1:18083/actuator/rocketmq-binder.

Endpoint will metrics some data like last send timestamp, sending or receive message successfully times or unsuccessfully times. 

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

Note: You should add [metrics-core dependency](https://mvnrepository.com/artifact/io.dropwizard.metrics/metrics-core) if you want to see metrics data. endpoint will show warning information if you don't add that dependency:

```json
{
    "warning": "please add metrics-core dependency, we use it for metrics"
}
```

## More

For more information about RocketMQ, see [RocketMQ Project](https://rocketmq.apache.org).

If you have any ideas or suggestions for Spring Cloud RocketMQ Binder, please don't hesitate to tell us by submitting github issues.

