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

package com.alibaba.cloud.sentinel;

import java.util.Optional;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.SentinelWebInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author chao.wu
 */
public class SentinelWebMvcConfigurer implements WebMvcConfigurer {

	private static final Logger log = LoggerFactory
			.getLogger(SentinelWebMvcConfigurer.class);

	@Autowired
	private SentinelProperties sentinelProperties;

	@Autowired
	private Optional<SentinelWebInterceptor> sentinelWebInterceptorOptional;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (!sentinelWebInterceptorOptional.isPresent()) {
			return;
		}
		SentinelProperties.Filter filterConfig = sentinelProperties.getFilter();
		registry.addInterceptor(sentinelWebInterceptorOptional.get())
				.order(filterConfig.getOrder())
				.addPathPatterns(filterConfig.getUrlPatterns());
		log.info(
				"[Sentinel Starter] register SentinelWebInterceptor with urlPatterns: {}.",
				filterConfig.getUrlPatterns());
	}

}
