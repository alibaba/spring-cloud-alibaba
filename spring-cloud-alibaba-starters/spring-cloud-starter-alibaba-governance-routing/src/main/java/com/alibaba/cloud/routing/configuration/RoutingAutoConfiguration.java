/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.configuration;

import com.alibaba.cloud.routing.aop.ReactiveBeanPostProcessor;
import com.alibaba.cloud.routing.aop.RestBeanPostProcessor;
import com.alibaba.cloud.routing.aop.interceptor.OpenFeignInterceptor;
import com.alibaba.cloud.routing.aop.interceptor.ReactiveInterceptor;
import com.alibaba.cloud.routing.aop.interceptor.RestTemplateInterceptor;
import com.alibaba.cloud.routing.constant.LabelRoutingConstants;
import com.alibaba.cloud.routing.context.LabelRoutingContextHolder;
import com.alibaba.cloud.routing.context.defaults.DefaultLabelLabelRoutingContextHolder;
import com.alibaba.cloud.routing.listener.RoutingDataListener;
import com.alibaba.cloud.routing.properties.LabelRoutingProperties;
import com.alibaba.cloud.routing.publish.TargetServiceChangedPublisher;
import com.alibaba.cloud.routing.repository.FilterService;
import com.alibaba.cloud.routing.repository.RoutingDataRepository;
import com.alibaba.cloud.routing.ribbon.RoutingLoadBalanceRule;
import feign.Feign;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author HH
 * @since 2.2.10-RC1
 */

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ LabelRoutingProperties.class })
@AutoConfigureOrder(RoutingAutoConfiguration.ROUTING_AUTO_CONFIG_ORDER)
public class RoutingAutoConfiguration {

	/**
	 * Order of label routing auto config.
	 */
	public static final int ROUTING_AUTO_CONFIG_ORDER = 10;

	@Bean
	@ConditionalOnMissingBean
	public RoutingDataRepository routingDataRepository() {

		return new RoutingDataRepository();
	}

	@Bean
	@ConditionalOnMissingBean
	public FilterService filterService() {

		return new FilterService();
	}

	@Bean
	public RoutingDataListener routingDataListener(
			RoutingDataRepository routingDataRepository, FilterService filterService) {

		return new RoutingDataListener(routingDataRepository, filterService);
	}

	@Bean
	public TargetServiceChangedPublisher targetServiceChangedPublisher() {

		return new TargetServiceChangedPublisher();
	}

	@Bean
	@ConditionalOnMissingBean
	public OpenFeignInterceptor routingFeignInterceptor() {

		return new OpenFeignInterceptor();
	}

	@Bean
	@ConditionalOnMissingBean
	public RoutingLoadBalanceRule routingLoadBalanceRule() {

		return new RoutingLoadBalanceRule();
	}

	@Bean
	@ConditionalOnMissingBean
	public LabelRoutingContextHolder routingContextHolder() {

		return new DefaultLabelLabelRoutingContextHolder();
	}

	@ConditionalOnClass(Feign.class)
	protected static class RoutingFeignConfiguration {

		@Bean
		@ConditionalOnProperty(
				value = LabelRoutingConstants.WebClient.FEIGN_INTERCEPT_ENABLED,
				matchIfMissing = true)
		public OpenFeignInterceptor feignRequestInterceptor() {
			return new OpenFeignInterceptor();
		}

	}

	@ConditionalOnClass(RestTemplate.class)
	protected static class RoutingRestTemplateConfiguration {

		@Bean
		@ConditionalOnProperty(
				value = LabelRoutingConstants.WebClient.REST_INTERCEPT_ENABLED,
				matchIfMissing = true)
		public RestTemplateInterceptor restTemplateInterceptor() {
			return new RestTemplateInterceptor();
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(
				value = LabelRoutingConstants.WebClient.REST_INTERCEPT_ENABLED,
				matchIfMissing = true)
		public RestBeanPostProcessor restBeanPostProcessor() {
			return new RestBeanPostProcessor();
		}

	}

	@ConditionalOnClass(WebClient.class)
	@ConditionalOnBean(WebClient.Builder.class)
	protected static class RoutingWebClientConfiguration {

		@Bean
		@ConditionalOnProperty(
				value = LabelRoutingConstants.WebClient.REACTIVE_INTERCEPT_ENABLED,
				matchIfMissing = true)
		public ReactiveInterceptor reactiveInterceptor() {
			return new ReactiveInterceptor();
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(
				value = LabelRoutingConstants.WebClient.REACTIVE_INTERCEPT_ENABLED,
				matchIfMissing = true)
		public ReactiveBeanPostProcessor reactiveBeanPostProcessor() {
			return new ReactiveBeanPostProcessor();
		}

	}

}
