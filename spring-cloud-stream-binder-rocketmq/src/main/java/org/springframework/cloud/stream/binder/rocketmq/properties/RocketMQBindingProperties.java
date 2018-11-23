package org.springframework.cloud.stream.binder.rocketmq.properties;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQBindingProperties {

	private RocketMQConsumerProperties consumer = new RocketMQConsumerProperties();

	private RocketMQProducerProperties producer = new RocketMQProducerProperties();

	public RocketMQConsumerProperties getConsumer() {
		return consumer;
	}

	public void setConsumer(RocketMQConsumerProperties consumer) {
		this.consumer = consumer;
	}

	public RocketMQProducerProperties getProducer() {
		return producer;
	}

	public void setProducer(RocketMQProducerProperties producer) {
		this.producer = producer;
	}
}
