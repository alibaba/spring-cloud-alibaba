package com.alibaba.cloud.stream.binder.rocketmq;

import com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate.RocketMQBinderAutoConfiguration;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/rocket-compose-test.yml", serviceName = "rocketmq-standalone")
@TestExtend(time = 6 * TIME_OUT)
@SpringBootTest(classes = RocketMQAutoConfigurationTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.cloud.stream.rocketmq.binder.name-server=127.0.0.1:9876,127.0.0.1:9877",
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
public class RocketMQAutoConfigurationTests {

	@Autowired
	private RocketMQBinderConfigurationProperties binderConfigurationProperties;
	@Autowired
	private RocketMQExtendedBindingProperties bindingProperties;

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(RocketMQBinderAutoConfiguration.class));

	@BeforeAll
	public static void setUp() {

	}

	@BeforeEach
	public void prepare() {

	}

	@Test
	public void testProperties() {
		this.contextRunner.run(context -> {
			assertThat(binderConfigurationProperties.getNameServer())
					.isEqualTo("127.0.0.1:9876,127.0.0.1:9877");
			assertThat(bindingProperties.getExtendedConsumerProperties("input2")
					.getSubscription()).isEqualTo("tag1");
			assertThat(bindingProperties.getExtendedConsumerProperties("input2").getPush()
					.getOrderly()).isFalse();
			assertThat(bindingProperties.getExtendedConsumerProperties("input1").getPush()
					.getOrderly()).isTrue();
		});
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			RocketMQBinderAutoConfiguration.class })
	public static class TestConfig {

	}
}
