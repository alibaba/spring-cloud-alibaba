package com.alibaba.cloud.stream.binder.rocketmq;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.rocketmq.SimpleMsg;
import com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate.RocketMQBinderAutoConfiguration;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import org.apache.rocketmq.common.message.MessageConst;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/rocket-compose-test.yml", serviceName = "rocketmq-standalone")
@TestExtend(time = 11 * TIME_OUT)
@RunWith(SpringRunner.class)
@EnableBinding({Processor.class, RocketmqProduceAndConsumerTests.PolledProcessor.class})
@SpringBootTest(classes = RocketmqProduceAndConsumerTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876,127.0.0.1:9877",
		"spring.cloud.stream.rocketmq.binder.group=test-group1",
		"spring.cloud.stream.pollable-source=pollable",
		"spring.cloud.stream.bindings.output.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.output.content-type=application/json",
		"spring.cloud.stream.bindings.input1.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.input1.content-type=application/json",
		"spring.cloud.stream.bindings.input1.group=test-group1",
		"spring.cloud.stream.rocketmq.bindings.input1.consumer.push.orderly=true",
		"spring.cloud.stream.bindings.input1.consumer.maxAttempts=1"})
public class RocketmqProduceAndConsumerTests {


	@Test
	public void testConsumeAndProduce(){

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			RocketMQBinderAutoConfiguration.class })
	public static class TestConfig {

		@Bean(name = "produce")
		@Profile("test")
		public ApplicationRunner produceRunner(MessageChannel dest) {
			return args -> {
					String key = "KEY";
					String messageId = "1";
					Map<String, Object> headers = new HashMap<>();
					headers.put(MessageConst.PROPERTY_KEYS, key);
					headers.put(MessageConst.PROPERTY_TAGS, "TagA");
					headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, messageId);
					Message<SimpleMsg> msg = new GenericMessage(new SimpleMsg("Hello RocketMQ"), headers);
					dest.send(msg);
					Thread.sleep(5_000);
			};
		}

		@Bean(name = "consumer")
		@Profile("test")
		@DependsOn(value = "produce")
		public ApplicationRunner consumerRunner(PollableMessageSource dest) {
			return args -> {
				((SubscribableChannel) dest).subscribe(message -> Assertions.assertEquals(message,"Hello RocketMQ" ));
			};
		}

	}
	/**
	 * 自定义发送消息接口
	 */

		public interface PolledProcessor extends Source, Sink {

			String CUSTOMIZE_OUTPUT = "output";

			@Output(CUSTOMIZE_OUTPUT)
			MessageChannel output();

			String CUSTOMIZE_INPUT = "input1";
			@Input(CUSTOMIZE_INPUT)
			SubscribableChannel input();
	}

}
