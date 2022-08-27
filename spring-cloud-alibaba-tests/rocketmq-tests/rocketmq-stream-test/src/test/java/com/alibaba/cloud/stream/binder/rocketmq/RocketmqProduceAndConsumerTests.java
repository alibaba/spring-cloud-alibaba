package com.alibaba.cloud.stream.binder.rocketmq;

import com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate.RocketMQBinderAutoConfiguration;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/rocket-compose-test.yml", serviceName = "rocketmq-standalone")
@TestExtend(time = 10 * TIME_OUT)
@RunWith(SpringRunner.class)
@EnableBinding(RocketmqProduceAndConsumerTests.TestConfig.PolledProcessor.class)
@SpringBootTest(classes = RocketmqProduceAndConsumerTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876,127.0.0.1:9877",
		"spring.cloud.stream.pollable-source=pollable",
		"spring.cloud.stream.bindings.output.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.output.content-type=application/json",
		"spring.cloud.stream.bindings.input1.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.input1.content-type=application/json",
		"spring.cloud.stream.bindings.input1.group=test-group1",
		"spring.cloud.stream.rocketmq.bindings.input1.consumer.push.orderly=true",
		"spring.cloud.stream.bindings.input1.consumer.maxAttempts=1",
		"spring.cloud.stream.bindings.input2.destination=TopicOrderTest",
		"spring.cloud.stream.bindings.input2.content-type=application/json",
		"spring.cloud.stream.bindings.input2.group=test-group2",
		"spring.cloud.stream.rocketmq.bindings.input2.consumer.push.orderly=false",
		"spring.cloud.stream.rocketmq.bindings.input2.consumer.subscription=tag1" })
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
		public ApplicationRunner produceRunner(PollableMessageSource source,
				MessageChannel dest) {
			return args -> {
				while (true) {
					boolean result = source.poll(m -> {
						String payload = (String) m.getPayload();
						dest.send(MessageBuilder.withPayload(payload.toUpperCase())
								.copyHeaders(m.getHeaders())
								.build());
					}, new ParameterizedTypeReference<String>() { });
					if (result) {
						System.out.println("Processed a message");
					}
					else {
						System.out.println("Nothing to do");
					}
					Thread.sleep(5_000);
				}
			};
		}

		@Bean(name = "consumer")
		@Profile("test")
		@DependsOn(value = "produce")
		public ApplicationRunner consumerRunner(MessageChannel dest) {
			return args -> {
				((SubscribableChannel) dest).subscribe(message -> System.out.println(message));
			};
		}

		public interface PolledProcessor {

			@Input
			PollableMessageSource source();

			@Output
			MessageChannel dest();

		}
	}
}
