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

package com.alibaba.cloud.examples.config;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Load balancing and sentinel configuration for RestTemplate.
 *
 * @author fangjian0423, MieAh
 */
@Configuration
public class RestTemplateConfiguration {

	@LoadBalanced
	@Bean
	@SentinelRestTemplate(urlCleanerClass = UrlCleaner.class, urlCleaner = "clean")
	public RestTemplate urlCleanedRestTemplate() {
		return new RestTemplate();
	}

	@LoadBalanced
	@Bean
	@SentinelRestTemplate
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
