package com.alibaba.cloud.stream.binder.rocketmq;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.cloud.stream.binder.rocketmq.binder.RocketMQTestBinder;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

import com.alibaba.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQExtendedBindingProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.provisioning.RocketMQTopicProvisioner;

/**
 *  TODO Embedded RocketMQ
 *
 * @author <a href="mailto:jiashuai.xie01@gmail.com">Xiejiashuai</a>
 */
@Ignore
public class RocketMQTestBinderTest extends
		PartitionCapableBinderTests<RocketMQTestBinder, ExtendedConsumerProperties<RocketMQConsumerProperties>, ExtendedProducerProperties<RocketMQProducerProperties>> {

	private final String CLASS_UNDER_TEST_NAME = RocketMQMessageChannelBinder.class
			.getSimpleName();

	private RocketMQTestBinder testBinder;

	@Override
	protected boolean usesExplicitRouting() {
		return false;
	}

	@Override
	protected String getClassUnderTestName() {
		return CLASS_UNDER_TEST_NAME;
	}

	@Override
	protected RocketMQTestBinder getBinder() throws Exception {
		if (null == testBinder) {
			testBinder = new RocketMQTestBinder(new RocketMQTopicProvisioner(),
					new RocketMQExtendedBindingProperties(),
					new RocketMQBinderConfigurationProperties(), new RocketMQProperties(),
					new InstrumentationManager());
		}
		return testBinder;
	}

	@Override
	protected ExtendedConsumerProperties<RocketMQConsumerProperties> createConsumerProperties() {
		ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties = new ExtendedConsumerProperties<>(
				new RocketMQConsumerProperties());
		return consumerProperties;
	}

	@Override
	protected ExtendedProducerProperties<RocketMQProducerProperties> createProducerProperties() {
		ExtendedProducerProperties<RocketMQProducerProperties> producerProperties = new ExtendedProducerProperties<>(
				new RocketMQProducerProperties());
		// sync send msg
		producerProperties.getExtension().setSync(Boolean.TRUE);
		return producerProperties;
	}

	@Override
	public Spy spyOn(String name) {
		throw new UnsupportedOperationException("'spyOn' is not used by RocketMQ tests");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testTrustedPackages() throws Exception {
		Binder binder = getBinder();

		BindingProperties producerBindingProperties = createProducerBindingProperties(
				createProducerProperties());
		DirectChannel moduleOutputChannel = createBindableChannel("output",
				producerBindingProperties);

		ExtendedConsumerProperties<RocketMQConsumerProperties> consumerProperties = createConsumerProperties();

		DirectChannel moduleInputChannel = createBindableChannel("input",
				createConsumerBindingProperties(consumerProperties));

		Binding<MessageChannel> producerBinding = binder.bindProducer("bar",
				moduleOutputChannel, producerBindingProperties.getProducer());

		Binding<MessageChannel> consumerBinding = binder.bindConsumer("bar",
				"testSendAndReceiveNoOriginalContentType", moduleInputChannel,
				consumerProperties);
		binderBindUnbindLatency();

		Message<?> message = org.springframework.integration.support.MessageBuilder
				.withPayload("foo")
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN)
				.setHeader("foo", MimeTypeUtils.TEXT_PLAIN).build();

		moduleOutputChannel.send(message);
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Message<byte[]>> inboundMessageRef = new AtomicReference<>();
		moduleInputChannel.subscribe(message1 -> {
			try {
				inboundMessageRef.set((Message<byte[]>) message1);
			}
			finally {
				latch.countDown();
			}
		});
		Assert.isTrue(latch.await(5, TimeUnit.SECONDS), "Failed to receive message");

		Assertions.assertThat(inboundMessageRef.get()).isNotNull();
		Assertions.assertThat(inboundMessageRef.get().getPayload())
				.isEqualTo("foo".getBytes());
		Assertions.assertThat(inboundMessageRef.get().getHeaders()
				.get(BinderHeaders.BINDER_ORIGINAL_CONTENT_TYPE)).isNull();
		Assertions
				.assertThat(inboundMessageRef.get().getHeaders()
						.get(MessageHeaders.CONTENT_TYPE))
				.isEqualTo(MimeTypeUtils.TEXT_PLAIN);
		Assertions.assertThat(inboundMessageRef.get().getHeaders().get("foo"))
				.isInstanceOf(String.class);
		String actual = (String) inboundMessageRef.get().getHeaders().get("foo");
		Assertions.assertThat(actual).isEqualTo(MimeTypeUtils.TEXT_PLAIN.toString());
		producerBinding.unbind();
		consumerBinding.unbind();
	}
}
