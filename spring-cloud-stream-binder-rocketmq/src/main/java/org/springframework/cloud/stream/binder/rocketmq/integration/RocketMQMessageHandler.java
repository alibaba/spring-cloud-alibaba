package org.springframework.cloud.stream.binder.rocketmq.integration;

import java.time.Instant;
import java.util.Map;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQMessageHeaderAccessor;
import org.springframework.cloud.stream.binder.rocketmq.metrics.InstrumentationManager;
import org.springframework.cloud.stream.binder.rocketmq.metrics.ProducerInstrumentation;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.MessagingException;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQMessageHandler extends AbstractMessageHandler implements Lifecycle {

	private DefaultMQProducer producer;

	private ProducerInstrumentation producerInstrumentation;

	private final RocketMQProducerProperties producerProperties;

	private final String destination;

	private final RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties;

	private final InstrumentationManager instrumentationManager;

	protected volatile boolean running = false;

	public RocketMQMessageHandler(String destination,
			RocketMQProducerProperties producerProperties,
			RocketMQBinderConfigurationProperties rocketBinderConfigurationProperties,
			InstrumentationManager instrumentationManager) {
		this.destination = destination;
		this.producerProperties = producerProperties;
		this.rocketBinderConfigurationProperties = rocketBinderConfigurationProperties;
		this.instrumentationManager = instrumentationManager;
	}

	@Override
	public void start() {
		producer = new DefaultMQProducer(destination);

		producerInstrumentation = instrumentationManager
				.getProducerInstrumentation(destination);
		instrumentationManager.addHealthInstrumentation(producerInstrumentation);

		producer.setNamesrvAddr(rocketBinderConfigurationProperties.getNamesrvAddr());

		if (producerProperties.getMaxMessageSize() > 0) {
			producer.setMaxMessageSize(producerProperties.getMaxMessageSize());
		}

		try {
			producer.start();
			producerInstrumentation.markStartedSuccessfully();
		}
		catch (MQClientException e) {
			producerInstrumentation.markStartFailed(e);
			logger.error(
					"RocketMQ Message hasn't been sent. Caused by " + e.getMessage());
			throw new MessagingException(e.getMessage(), e);
		}
		running = true;
	}

	@Override
	public void stop() {
		if (producer != null) {
			producer.shutdown();
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	protected void handleMessageInternal(org.springframework.messaging.Message<?> message)
			throws Exception {
		try {
			Message toSend;
			if (message.getPayload() instanceof byte[]) {
				toSend = new Message(destination, (byte[]) message.getPayload());
			}
			else if (message.getPayload() instanceof String) {
				toSend = new Message(destination,
						((String) message.getPayload()).getBytes());
			}
			else {
				throw new UnsupportedOperationException("Payload class isn't supported: "
						+ message.getPayload().getClass());
			}
			RocketMQMessageHeaderAccessor headerAccessor = new RocketMQMessageHeaderAccessor(
					message);
			headerAccessor.setLeaveMutable(true);
			toSend.setDelayTimeLevel(headerAccessor.getDelayTimeLevel());
			toSend.setTags(headerAccessor.getTags());
			toSend.setKeys(headerAccessor.getKeys());
			toSend.setFlag(headerAccessor.getFlag());
			for (Map.Entry<String, String> entry : headerAccessor.getUserProperties()
					.entrySet()) {
				toSend.putUserProperty(entry.getKey(), entry.getValue());
			}

			SendResult sendRes = producer.send(toSend);

			if (!sendRes.getSendStatus().equals(SendStatus.SEND_OK)) {
				throw new MQClientException("message hasn't been sent", null);
			}
			if (message instanceof MutableMessage) {
				RocketMQMessageHeaderAccessor.putSendResult((MutableMessage) message,
						sendRes);
			}
			instrumentationManager.getRuntime().put(
					RocketMQBinderConstants.LASTSEND_TIMESTAMP,
					Instant.now().toEpochMilli());
			producerInstrumentation.markSent();
		}
		catch (MQClientException | RemotingException | MQBrokerException
				| InterruptedException | UnsupportedOperationException e) {
			producerInstrumentation.markSentFailure();
			logger.error(
					"RocketMQ Message hasn't been sent. Caused by " + e.getMessage());
			throw new MessagingException(e.getMessage(), e);
		}

	}

}