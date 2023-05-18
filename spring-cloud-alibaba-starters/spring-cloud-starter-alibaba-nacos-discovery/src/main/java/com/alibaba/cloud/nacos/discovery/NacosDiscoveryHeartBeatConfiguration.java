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

package com.alibaba.cloud.nacos.discovery;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaojing
 * @author echooymxq
 * @author ruansheng
 * @author zhangbin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnBlockingDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureAfter(value = NacosDiscoveryAutoConfiguration.class,
		name = "de.codecentric.boot.admin.server.cloud.config.AdminServerDiscoveryAutoConfiguration")
public class NacosDiscoveryHeartBeatConfiguration {

	/**
	 * Nacos HeartBeat is no longer enabled by default .
	 * publish an event every 30 seconds
	 * see https://github.com/alibaba/spring-cloud-alibaba/issues/2868
	 * see https://github.com/alibaba/spring-cloud-alibaba/issues/3258
	 */
	@Bean
	@ConditionalOnMissingBean
	@Conditional(NacosDiscoveryHeartBeatCondition.class)
	public NacosDiscoveryHeartBeatPublisher nacosDiscoveryHeartBeatPublisher(NacosDiscoveryProperties nacosDiscoveryProperties) {
		return new NacosDiscoveryHeartBeatPublisher(nacosDiscoveryProperties);
	}

	private static class NacosDiscoveryHeartBeatCondition extends AnyNestedCondition {

		NacosDiscoveryHeartBeatCondition()  {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		/**
         * Spring Cloud Gateway HeartBeat .
		 */
		@ConditionalOnProperty(value = "spring.cloud.gateway.discovery.locator.enabled", matchIfMissing = false)
		static class GatewayLocatorHeartBeatEnabled { }

		/**
		 * Spring Boot Admin HeartBeat .
		 */
		@ConditionalOnBean(type = "de.codecentric.boot.admin.server.cloud.discovery.InstanceDiscoveryListener")
		static class SpringBootAdminHeartBeatEnabled { }

		/**
		 * Nacos HeartBeat .
		 */
		@ConditionalOnProperty(value = "spring.cloud.nacos.discovery.heart-beat.enabled", matchIfMissing = false)
		static class NacosDiscoveryHeartBeatEnabled { }
	}

}
