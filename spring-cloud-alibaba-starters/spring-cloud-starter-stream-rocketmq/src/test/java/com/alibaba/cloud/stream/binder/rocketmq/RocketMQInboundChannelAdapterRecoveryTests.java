/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.stream.binder.rocketmq;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.integration.core.RecoveryCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.util.backoff.FixedBackOff;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the recovery callback behavior in
 * {@link RocketMQInboundChannelAdapter} when a {@link RetryTemplate} exhausts
 * all attempts.
 */
public class RocketMQInboundChannelAdapterRecoveryTests {

	@Test
	public void recoveryCallbackInvokedWhenRetriesExhausted() throws Exception {
		ExtendedConsumerProperties<RocketMQConsumerProperties> props =
				new ExtendedConsumerProperties<>(new RocketMQConsumerProperties());
		RocketMQInboundChannelAdapter adapter = new RocketMQInboundChannelAdapter("topic", props);

		// Output channel that always fails — exercises the retry path.
		AtomicInteger sendAttempts = new AtomicInteger();
		SubscribableChannel outputChannel = new SubscribableChannel() {
			@Override
			public boolean send(Message<?> message, long timeout) {
				sendAttempts.incrementAndGet();
				throw new MessagingException("simulated downstream failure");
			}

			@Override
			public boolean send(Message<?> message) {
				return send(message, -1);
			}

			@Override
			public boolean subscribe(MessageHandler handler) {
				return true;
			}

			@Override
			public boolean unsubscribe(MessageHandler handler) {
				return true;
			}
		};
		adapter.setOutputChannel(outputChannel);

		// 3 total attempts: the initial call plus 2 retries, no backoff so the test is fast.
		RetryTemplate retryTemplate = new RetryTemplate(
				RetryPolicy.builder().backOff(new FixedBackOff(0L, 2L)).build());
		adapter.setRetryTemplate(retryTemplate);

		AtomicReference<Throwable> recoveredCause = new AtomicReference<>();
		AtomicInteger recoveryInvocations = new AtomicInteger();
		RecoveryCallback<Object> recoveryCallback = new RecoveryCallback<>() {
			@Override
			public Object recover(AttributeAccessor accessor, Throwable throwable) {
				recoveryInvocations.incrementAndGet();
				recoveredCause.set(throwable);
				return null;
			}
		};
		adapter.setRecoveryCallback(recoveryCallback);

		MessageExt messageExt = new MessageExt();
		messageExt.setBody("payload".getBytes());
		messageExt.setTopic("topic");
		messageExt.setMsgId("test-msg-id");

		Method consumeMessage = RocketMQInboundChannelAdapter.class
				.getDeclaredMethod("consumeMessage", List.class, Supplier.class, Supplier.class);
		consumeMessage.setAccessible(true);

		Object result = consumeMessage.invoke(adapter,
				Collections.singletonList(messageExt),
				(Supplier<String>) () -> "FAIL",
				(Supplier<String>) () -> "OK");

		// Recovery should have been called exactly once with the underlying cause.
		assertThat(recoveryInvocations.get()).isEqualTo(1);
		assertThat(recoveredCause.get()).isNotNull();
		// The retry template should have invoked the failing handler the configured number of times.
		assertThat(sendAttempts.get()).isEqualTo(3);
		// Because recovery succeeded, the loop returns the success supplier value rather than
		// falling through to the failure path that would trigger redelivery.
		assertThat(result).isEqualTo("OK");
	}

	@Test
	public void retriesExhaustedWithoutRecoveryFallsBackToFailure() throws Exception {
		ExtendedConsumerProperties<RocketMQConsumerProperties> props =
				new ExtendedConsumerProperties<>(new RocketMQConsumerProperties());
		RocketMQInboundChannelAdapter adapter = new RocketMQInboundChannelAdapter("topic", props);

		AtomicInteger sendAttempts = new AtomicInteger();
		SubscribableChannel outputChannel = new SubscribableChannel() {
			@Override
			public boolean send(Message<?> message, long timeout) {
				sendAttempts.incrementAndGet();
				throw new MessagingException("simulated downstream failure");
			}

			@Override
			public boolean send(Message<?> message) {
				return send(message, -1);
			}

			@Override
			public boolean subscribe(MessageHandler handler) {
				return true;
			}

			@Override
			public boolean unsubscribe(MessageHandler handler) {
				return true;
			}
		};
		adapter.setOutputChannel(outputChannel);

		// 2 total attempts: the initial call plus 1 retry, no backoff so the test is fast.
		adapter.setRetryTemplate(new RetryTemplate(
				RetryPolicy.builder().backOff(new FixedBackOff(0L, 1L)).build()));

		MessageExt messageExt = new MessageExt();
		messageExt.setBody("payload".getBytes());
		messageExt.setTopic("topic");
		messageExt.setMsgId("test-msg-id");

		Method consumeMessage = RocketMQInboundChannelAdapter.class
				.getDeclaredMethod("consumeMessage", List.class, Supplier.class, Supplier.class);
		consumeMessage.setAccessible(true);

		Object result = consumeMessage.invoke(adapter,
				Collections.singletonList(messageExt),
				(Supplier<String>) () -> "FAIL",
				(Supplier<String>) () -> "OK");

		// Without a recovery callback, the retry exception bubbles up to the per-message catch
		// and the failure supplier is returned so RocketMQ can redeliver.
		assertThat(sendAttempts.get()).isEqualTo(2);
		assertThat(result).isEqualTo("FAIL");
	}

}
