package org.springframework.cloud.stream.binder.rocketmq.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConfigurationProperties("spring.cloud.stream.rocketmq")
public class RocketMQExtendedBindingProperties implements
		ExtendedBindingProperties<RocketMQConsumerProperties, RocketMQProducerProperties> {

	private Map<String, RocketMQBindingProperties> bindings = new HashMap<>();

	public Map<String, RocketMQBindingProperties> getBindings() {
		return this.bindings;
	}

	public void setBindings(Map<String, RocketMQBindingProperties> bindings) {
		this.bindings = bindings;
	}

	@Override
	public synchronized RocketMQConsumerProperties getExtendedConsumerProperties(
			String channelName) {
		if (bindings.containsKey(channelName)) {
			if (bindings.get(channelName).getConsumer() != null) {
				return bindings.get(channelName).getConsumer();
			}
			else {
				RocketMQConsumerProperties properties = new RocketMQConsumerProperties();
				this.bindings.get(channelName).setConsumer(properties);
				return properties;
			}
		}
		else {
			RocketMQConsumerProperties properties = new RocketMQConsumerProperties();
			RocketMQBindingProperties rbp = new RocketMQBindingProperties();
			rbp.setConsumer(properties);
			bindings.put(channelName, rbp);
			return properties;
		}
	}

	@Override
	public synchronized RocketMQProducerProperties getExtendedProducerProperties(
			String channelName) {
		if (bindings.containsKey(channelName)) {
			if (bindings.get(channelName).getProducer() != null) {
				return bindings.get(channelName).getProducer();
			}
			else {
				RocketMQProducerProperties properties = new RocketMQProducerProperties();
				this.bindings.get(channelName).setProducer(properties);
				return properties;
			}
		}
		else {
			RocketMQProducerProperties properties = new RocketMQProducerProperties();
			RocketMQBindingProperties rbp = new RocketMQBindingProperties();
			rbp.setProducer(properties);
			bindings.put(channelName, rbp);
			return properties;
		}
	}
}
