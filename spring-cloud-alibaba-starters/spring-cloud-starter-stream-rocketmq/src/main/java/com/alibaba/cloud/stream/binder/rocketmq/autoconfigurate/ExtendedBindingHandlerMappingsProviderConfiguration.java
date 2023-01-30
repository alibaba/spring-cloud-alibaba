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

package com.alibaba.cloud.stream.binder.rocketmq.autoconfigurate;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.stream.binder.rocketmq.convert.RocketMQMessageConverter;
import com.alibaba.cloud.stream.binder.rocketmq.custom.RocketMQConfigBeanPostProcessor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.cloud.stream.config.BindingHandlerAdvise.MappingsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;

@Configuration
public class ExtendedBindingHandlerMappingsProviderConfiguration {

	@Bean
	public MappingsProvider rocketExtendedPropertiesDefaultMappingsProvider() {
		return () -> {
			Map<ConfigurationPropertyName, ConfigurationPropertyName> mappings = new HashMap<>();
			mappings.put(
					ConfigurationPropertyName.of("spring.cloud.stream.rocketmq.bindings"),
					ConfigurationPropertyName.of("spring.cloud.stream.rocketmq.default"));
			mappings.put(
					ConfigurationPropertyName.of("spring.cloud.stream.rocketmq.streams"),
					ConfigurationPropertyName
							.of("spring.cloud.stream.rocketmq.streams.default"));
			return mappings;
		};
	}

	@Bean
	public static RocketMQConfigBeanPostProcessor rocketMQConfigBeanPostProcessor() {
		return new RocketMQConfigBeanPostProcessor();
	}


	/**
	 * if you want to customize a bean, please use this BeanName {@code RocketMQMessageConverter.DEFAULT_NAME}.
	 */
	@Bean(RocketMQMessageConverter.DEFAULT_NAME)
	@ConditionalOnMissingBean(name = { RocketMQMessageConverter.DEFAULT_NAME })
	public CompositeMessageConverter rocketMQMessageConverter() {
		return new RocketMQMessageConverter().getMessageConverter();
	}

	/**
	 * Register message converter to adapte Spring Cloud Stream.
	 * Refer to https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#spring-cloud-stream-overview-user-defined-message-converters .
	 * @return message converter.
	 */
	@Bean
	public MessageConverter rocketMQCustomMessageConverter() {
		return new RocketMQMessageConverter();
	}

}
