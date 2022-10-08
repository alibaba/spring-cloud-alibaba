/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.bus.rocketmq.autoconfigurate;

import com.alibaba.cloud.stream.binder.rocketmq.convert.RocketMQMessageConverter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.bus.BusEnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.CompositeMessageConverter;

/**
 * BusAutoConfiguration of rocketmq.
 *
 * @author <a href="mailto:819294006@qq.com">Sorie</a>
 * @see BusEnvironmentPostProcessor
 */
@Configuration(proxyBeanMethods = false)
public class RocketMQBusAutoConfiguration {
	/**
	 * isse: https://github.com/alibaba/spring-cloud-alibaba/issues/2742
	 * if you want to customize a bean, please use this BeanName {@code RocketMQMessageConverter.DEFAULT_NAME}.
	 */
	@Bean(RocketMQMessageConverter.DEFAULT_NAME)
	@ConditionalOnMissingBean(name = { RocketMQMessageConverter.DEFAULT_NAME })
	public CompositeMessageConverter rocketMQMessageConverter() {
		return new RocketMQMessageConverter().getMessageConverter();
	}
}
