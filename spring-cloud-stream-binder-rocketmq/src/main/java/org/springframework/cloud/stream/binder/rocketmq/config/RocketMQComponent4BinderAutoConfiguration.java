/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.binder.rocketmq.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties.Producer;
import org.apache.rocketmq.spring.config.RocketMQConfigUtils;
import org.apache.rocketmq.spring.config.RocketMQTransactionAnnotationProcessor;
import org.apache.rocketmq.spring.config.TransactionHandlerRegistry;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.binder.rocketmq.RocketMQBinderConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
@ConditionalOnMissingBean(DefaultMQProducer.class)
public class RocketMQComponent4BinderAutoConfiguration {

	private final Environment environment;

	public RocketMQComponent4BinderAutoConfiguration(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public DefaultMQProducer defaultMQProducer() {
		RocketMQProperties rocketMQProperties = new RocketMQProperties();
		String configNameServer = environment
				.getProperty("spring.cloud.stream.rocketmq.binder.namesrv-addr");
		if (StringUtils.isEmpty(configNameServer)) {
			rocketMQProperties.setNameServer(RocketMQBinderConstants.DEFAULT_NAME_SERVER);
		}
		else {
			rocketMQProperties.setNameServer(configNameServer);
		}
		RocketMQProperties.Producer producerConfig = new Producer();
		rocketMQProperties.setProducer(producerConfig);
		producerConfig.setGroup(RocketMQBinderConstants.DEFAULT_GROUP);

		String nameServer = rocketMQProperties.getNameServer();
		String groupName = producerConfig.getGroup();
		Assert.hasText(nameServer, "[rocketmq.name-server] must not be null");
		Assert.hasText(groupName, "[rocketmq.producer.group] must not be null");

		DefaultMQProducer producer = new DefaultMQProducer(groupName);
		producer.setNamesrvAddr(nameServer);
		producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
		producer.setRetryTimesWhenSendFailed(
				producerConfig.getRetryTimesWhenSendFailed());
		producer.setRetryTimesWhenSendAsyncFailed(
				producerConfig.getRetryTimesWhenSendAsyncFailed());
		producer.setMaxMessageSize(producerConfig.getMaxMessageSize());
		producer.setCompressMsgBodyOverHowmuch(
				producerConfig.getCompressMessageBodyThreshold());
		producer.setRetryAnotherBrokerWhenNotStoreOK(producerConfig.isRetryNextServer());

		return producer;
	}

	@Bean(destroyMethod = "destroy")
	@ConditionalOnBean(DefaultMQProducer.class)
	@ConditionalOnMissingBean(RocketMQTemplate.class)
	public RocketMQTemplate rocketMQTemplate(DefaultMQProducer mqProducer,
			ObjectMapper rocketMQMessageObjectMapper) {
		RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
		rocketMQTemplate.setProducer(mqProducer);
		rocketMQTemplate.setObjectMapper(rocketMQMessageObjectMapper);
		return rocketMQTemplate;
	}

	@Bean
	@ConditionalOnBean(RocketMQTemplate.class)
	@ConditionalOnMissingBean(TransactionHandlerRegistry.class)
	public TransactionHandlerRegistry transactionHandlerRegistry(
			RocketMQTemplate template) {
		return new TransactionHandlerRegistry(template);
	}

	@Bean(name = RocketMQConfigUtils.ROCKETMQ_TRANSACTION_ANNOTATION_PROCESSOR_BEAN_NAME)
	@ConditionalOnBean(TransactionHandlerRegistry.class)
	public static RocketMQTransactionAnnotationProcessor transactionAnnotationProcessor(
			TransactionHandlerRegistry transactionHandlerRegistry) {
		return new RocketMQTransactionAnnotationProcessor(transactionHandlerRegistry);
	}

}
