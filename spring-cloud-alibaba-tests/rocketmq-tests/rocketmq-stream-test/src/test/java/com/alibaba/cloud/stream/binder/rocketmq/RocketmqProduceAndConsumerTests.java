package com.alibaba.cloud.stream.binder.rocketmq;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.alibaba.cloud.rocketmq.SimpleMsg;
import com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate.RocketMQBinderAutoConfiguration;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.support.MessageCollector;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import org.apache.rocketmq.common.message.MessageConst;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/rocket-compose-test.yml", serviceName = "rocketmq-standalone")
@TestExtend(time = 6 * TIME_OUT)
//@EnableBinding({Processor.class, RocketmqProduceAndConsumerTests.PolledProcessor.class})
@SpringBootTest(classes = RocketmqProduceAndConsumerTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876,127.0.0.1:9877",
		"spring.cloud.stream.rocketmq.binder.group=flaky-group",
//		"spring.cloud.stream.rocketmq.binder.consumer-group=flaky-group",
//		"spring.cloud.stream.pollable-source=pollable",
		"spring.cloud.stream.bindings.output.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.output.content-type=application/json",
		"spring.cloud.stream.bindings.output.group=test-group1",
		"spring.cloud.stream.bindings.input1.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.input1.content-type=application/json",
		"spring.cloud.stream.bindings.input1.group=test-group1",
		"spring.cloud.stream.bindings.input1.consumer.push.orderly=true",
		"spring.cloud.stream.bindings.input1.consumer.maxAttempts=1",})
public class RocketmqProduceAndConsumerTests {
	
	@Autowired
	private MessageCollector collector;

	@Autowired
	@Qualifier("input1")
	private MessageChannel input1;

	@Autowired
	@Qualifier("output")
	private MessageChannel output;

	@BeforeAll
	public static void prepare(){

	}

	@BeforeEach
	public void setup(){
		String key = "KEY";
		String messageId = "1";
		Map<String, Object> headers = new HashMap<>();
		headers.put(MessageConst.PROPERTY_KEYS, key);
		headers.put(MessageConst.PROPERTY_TAGS, "TagA");
		headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, messageId);
		Message<SimpleMsg> msg = new GenericMessage(new SimpleMsg("Hello RocketMQ"), headers);
		input1.send(msg);
	}

	@Test
	public void testConsumeAndProduce() throws Exception {
		BlockingQueue<Message<?>> messages = this.collector.forChannel(this.output);

		assertThat(messages, is("Hello RocketMQ"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration(value = { AutoServiceRegistrationConfiguration.class,
			RocketMQBinderAutoConfiguration.class}, exclude = {
			DataSourceAutoConfiguration.class,
			TransactionAutoConfiguration.class,
			DataSourceTransactionManagerAutoConfiguration.class})
	public static class TestConfig {

	}
	/**
	 * 自定义发送消息接口
	 */
	public interface PolledProcessor {

		String CUSTOMIZE_OUTPUT = "output";

		@Output(CUSTOMIZE_OUTPUT)
		MessageChannel output();

		String CUSTOMIZE_INPUT = "input1";
		@Input(CUSTOMIZE_INPUT)
		SubscribableChannel input();
	}

}
