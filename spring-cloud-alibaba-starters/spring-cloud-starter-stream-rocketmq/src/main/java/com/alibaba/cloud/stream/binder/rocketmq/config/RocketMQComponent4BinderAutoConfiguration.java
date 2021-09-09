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

package com.alibaba.cloud.stream.binder.rocketmq.config;

import com.alibaba.cloud.stream.binder.rocketmq.RocketMQBinderConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.config.RocketMQConfigUtils;
import org.apache.rocketmq.spring.config.RocketMQTransactionAnnotationProcessor;
import org.apache.rocketmq.spring.config.TransactionHandlerRegistry;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RocketMQAutoConfiguration.class)
@ConditionalOnMissingBean(DefaultMQProducer.class)
public class RocketMQComponent4BinderAutoConfiguration {

	private final Environment environment;

	public RocketMQComponent4BinderAutoConfiguration(Environment environment) {
		this.environment = environment;
	}

	@Bean
	@ConditionalOnMissingBean(DefaultMQProducer.class)
	public DefaultMQProducer defaultMQProducer() {
		DefaultMQProducer producer;
		String configNameServer = environment.resolveRequiredPlaceholders(
				"${spring.cloud.stream.rocketmq.binder.name-server:${rocketmq.producer.name-server:}}");
		String ak = environment.resolveRequiredPlaceholders(
				"${spring.cloud.stream.rocketmq.binder.access-key:${rocketmq.producer.access-key:}}");
		String sk = environment.resolveRequiredPlaceholders(
				"${spring.cloud.stream.rocketmq.binder.secret-key:${rocketmq.producer.secret-key:}}");
		String accessChannel = environment.resolveRequiredPlaceholders(
			"${spring.cloud.stream.rocketmq.binder.access-channel:${rocketmq.access-channel:}}");
		if (!StringUtils.isEmpty(ak) && !StringUtils.isEmpty(sk)) {
			producer = new DefaultMQProducer(RocketMQBinderConstants.DEFAULT_GROUP,
					new AclClientRPCHook(new SessionCredentials(ak, sk)));
			producer.setVipChannelEnabled(false);
		}
		else {
			producer = new DefaultMQProducer(RocketMQBinderConstants.DEFAULT_GROUP);
		}
		if (StringUtils.isEmpty(configNameServer)) {
			configNameServer = RocketMQBinderConstants.DEFAULT_NAME_SERVER;
		}
		producer.setNamesrvAddr(configNameServer);
		if (!StringUtils.isEmpty(configNameServer)) {
			producer.setAccessChannel(AccessChannel.valueOf(accessChannel));
		}
		return producer;
	}

	@Bean(destroyMethod = "destroy")
	@ConditionalOnMissingBean
	public RocketMQTemplate rocketMQTemplate(DefaultMQProducer mqProducer,
			ObjectMapper objectMapper) {
		RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
		rocketMQTemplate.setProducer(mqProducer);
		rocketMQTemplate.setObjectMapper(objectMapper);
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
