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

package com.alibaba.cloud.sentinel;

import java.util.Optional;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author xiaojing
 * @author yuhuangbin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
@ConditionalOnClass(SentinelWebInterceptor.class)
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelWebAutoConfiguration implements WebMvcConfigurer {

	private static final Logger log = LoggerFactory
			.getLogger(SentinelWebAutoConfiguration.class);

	@Autowired
	private SentinelProperties properties;

	@Autowired
	private Optional<UrlCleaner> urlCleanerOptional;

	@Autowired
	private Optional<BlockExceptionHandler> blockExceptionHandlerOptional;

	@Autowired
	private Optional<RequestOriginParser> requestOriginParserOptional;

	@Autowired
	private Optional<SentinelWebInterceptor> sentinelWebInterceptorOptional;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (!sentinelWebInterceptorOptional.isPresent()) {
			return;
		}
		SentinelProperties.Filter filterConfig = properties.getFilter();
		registry.addInterceptor(sentinelWebInterceptorOptional.get())
				.order(filterConfig.getOrder())
				.addPathPatterns(filterConfig.getUrlPatterns());
		log.info(
				"[Sentinel Starter] register SentinelWebInterceptor with urlPatterns: {}.",
				filterConfig.getUrlPatterns());
	}

	@Bean
	@ConditionalOnProperty(name = "spring.cloud.sentinel.filter.enabled",
			matchIfMissing = true)
	public SentinelWebInterceptor sentinelWebInterceptor(
			SentinelWebMvcConfig sentinelWebMvcConfig) {
		return new SentinelWebInterceptor(sentinelWebMvcConfig);
	}

	@Bean
	@ConditionalOnProperty(name = "spring.cloud.sentinel.filter.enabled",
			matchIfMissing = true)
	public SentinelWebMvcConfig sentinelWebMvcConfig() {
		SentinelWebMvcConfig sentinelWebMvcConfig = new SentinelWebMvcConfig();
		sentinelWebMvcConfig.setHttpMethodSpecify(properties.getHttpMethodSpecify());
		sentinelWebMvcConfig.setWebContextUnify(properties.getWebContextUnify());

		if (blockExceptionHandlerOptional.isPresent()) {
			blockExceptionHandlerOptional
					.ifPresent(sentinelWebMvcConfig::setBlockExceptionHandler);
		}
		else {
			if (StringUtils.hasText(properties.getBlockPage())) {
				sentinelWebMvcConfig.setBlockExceptionHandler(((request, response,
						e) -> response.sendRedirect(properties.getBlockPage())));
			}
			else {
				sentinelWebMvcConfig
						.setBlockExceptionHandler(new DefaultBlockExceptionHandler());
			}
		}

		urlCleanerOptional.ifPresent(sentinelWebMvcConfig::setUrlCleaner);
		requestOriginParserOptional.ifPresent(sentinelWebMvcConfig::setOriginParser);
		return sentinelWebMvcConfig;
	}

}
