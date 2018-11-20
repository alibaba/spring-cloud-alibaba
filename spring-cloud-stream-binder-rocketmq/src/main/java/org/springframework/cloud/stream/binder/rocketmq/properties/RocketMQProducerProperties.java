package org.springframework.cloud.stream.binder.rocketmq.properties;

import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQProducerProperties {

	private Boolean enabled = true;

	/**
	 * Maximum allowed message size in bytes {@link DefaultMQProducer#maxMessageSize}
	 */
	private Integer maxMessageSize = 0;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Integer getMaxMessageSize() {
		return maxMessageSize;
	}

	public void setMaxMessageSize(Integer maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}

}
