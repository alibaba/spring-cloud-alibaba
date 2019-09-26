/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.stream.binder.rocketmq.provisioning;

import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQConsumerProperties;
import com.alibaba.cloud.stream.binder.rocketmq.properties.RocketMQProducerProperties;
import org.apache.rocketmq.client.Validators;
import org.apache.rocketmq.client.exception.MQClientException;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
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
			throw new AssertionError(e);
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
