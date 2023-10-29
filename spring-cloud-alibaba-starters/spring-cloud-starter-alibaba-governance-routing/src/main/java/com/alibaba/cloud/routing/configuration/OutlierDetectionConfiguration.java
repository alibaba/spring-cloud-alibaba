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

import com.alibaba.cloud.routing.decorator.OutlierDetectionFeignClientDecorator;
import com.alibaba.cloud.routing.recover.OutlierDetectionRecover;
import feign.Client;

import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xqw
 * @author 550588941@qq.com
 */

@Configuration(proxyBeanMethods = false)
public class OutlierDetectionConfiguration {

	@Bean
	public Client outlierDetectionFeignClientDecorator(
			CachingSpringLoadBalancerFactory cachingSpringLoadBalancerFactory,
			SpringClientFactory clientFactory) {

		return new LoadBalancerFeignClient(
				new OutlierDetectionFeignClientDecorator(new Client.Default(null, null)),
				cachingSpringLoadBalancerFactory, clientFactory);
	}

	@Bean
	public OutlierDetectionRecover outlierDetectionRecover() {

		return new OutlierDetectionRecover();
	}

}
