/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.stream.binder.rocketmq.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * Container object for RocketMQ specific extended producer and consumer binding
 * properties.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class RocketMQSpecificPropertiesProvider
		implements BinderSpecificPropertiesProvider {

	/**
	 * Consumer specific binding properties. @see {@link RocketMQConsumerProperties}.
	 */
	private RocketMQConsumerProperties consumer = new RocketMQConsumerProperties();

	/**
	 * Producer specific binding properties. @see {@link RocketMQProducerProperties}.
	 */
	private RocketMQProducerProperties producer = new RocketMQProducerProperties();

	/**
	 * @return {@link RocketMQConsumerProperties} Consumer specific binding
	 * properties. @see {@link RocketMQConsumerProperties}.
	 */
	@Override
	public RocketMQConsumerProperties getConsumer() {
		return this.consumer;
	}

	public void setConsumer(RocketMQConsumerProperties consumer) {
		this.consumer = consumer;
	}

	/**
	 * @return {@link RocketMQProducerProperties} Producer specific binding
	 * properties. @see {@link RocketMQProducerProperties}.
	 */
	@Override
	public RocketMQProducerProperties getProducer() {
		return this.producer;
	}

	public void setProducer(RocketMQProducerProperties producer) {
		this.producer = producer;
	}

}
