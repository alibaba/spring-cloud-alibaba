package org.springframework.cloud.stream.binder.rocketmq.provisioning;

import org.apache.rocketmq.client.Validators;
import org.apache.rocketmq.client.exception.MQClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import org.springframework.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQTopicProvisioner implements
		ProvisioningProvider<ExtendedConsumerProperties<RocketMQConsumerProperties>, ExtendedProducerProperties<RocketMQProducerProperties>> {

	private static final Logger logger = LoggerFactory
			.getLogger(RocketMQTopicProvisioner.class);

	@Override
	public ProducerDestination provisionProducerDestination(String name,
			ExtendedProducerProperties<RocketMQProducerProperties> properties)
			throws ProvisioningException {
		checkTopic(name);
		return new RocketProducerDestination(name);
	}

	@Override
	public ConsumerDestination provisionConsumerDestination(String name, String group,
			ExtendedConsumerProperties<RocketMQConsumerProperties> properties)
			throws ProvisioningException {
		checkTopic(name);
		return new RocketConsumerDestination(name);
	}

	private void checkTopic(String topic) {
		try {
			Validators.checkTopic(topic);
		}
		catch (MQClientException e) {
			logger.error("topic check error: " + topic, e);
			throw new AssertionError(e); // Can't happen
		}
	}

	private static final class RocketProducerDestination implements ProducerDestination {

		private final String producerDestinationName;

		RocketProducerDestination(String destinationName) {
			this.producerDestinationName = destinationName;
		}

		@Override
		public String getName() {
			return producerDestinationName;
		}

		@Override
		public String getNameForPartition(int partition) {
			return producerDestinationName;
		}

	}

	private static final class RocketConsumerDestination implements ConsumerDestination {

		private final String consumerDestinationName;

		RocketConsumerDestination(String consumerDestinationName) {
			this.consumerDestinationName = consumerDestinationName;
		}

		@Override
		public String getName() {
			return this.consumerDestinationName;
		}

	}

}
