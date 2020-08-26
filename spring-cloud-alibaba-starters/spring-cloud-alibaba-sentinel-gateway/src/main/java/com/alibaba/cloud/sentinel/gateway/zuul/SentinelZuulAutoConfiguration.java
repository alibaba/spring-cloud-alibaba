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

package com.alibaba.cloud.sentinel.gateway.zuul;

import java.util.Optional;

import javax.annotation.PostConstruct;

import com.alibaba.cloud.sentinel.gateway.ConfigConstants;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.ZuulGatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulErrorFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPostFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.netflix.zuul.http.ZuulServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel Spring Cloud Zuul AutoConfiguration.
 *
 * @author tiger
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ZuulServlet.class)
@ConditionalOnProperty(prefix = ConfigConstants.ZUUl_PREFIX, name = "enabled",
		havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SentinelZuulProperties.class)
public class SentinelZuulAutoConfiguration {

	private static final Logger logger = LoggerFactory
			.getLogger(SentinelZuulAutoConfiguration.class);

	@Autowired
	private Optional<RequestOriginParser> requestOriginParserOptional;

	@Autowired
	private SentinelZuulProperties zuulProperties;

	@PostConstruct
	private void init() {
		requestOriginParserOptional
				.ifPresent(ZuulGatewayCallbackManager::setOriginParser);
		System.setProperty(SentinelConfig.APP_TYPE_PROP_KEY,
				String.valueOf(ConfigConstants.APP_TYPE_ZUUL_GATEWAY));
	}

	@Bean
	@ConditionalOnMissingBean
	public SentinelZuulPreFilter sentinelZuulPreFilter() {
		logger.info("[Sentinel Zuul] register SentinelZuulPreFilter {}",
				zuulProperties.getOrder().getPre());
		return new SentinelZuulPreFilter(zuulProperties.getOrder().getPre());
	}

	@Bean
	@ConditionalOnMissingBean
	public SentinelZuulPostFilter sentinelZuulPostFilter() {
		logger.info("[Sentinel Zuul] register SentinelZuulPostFilter {}",
				zuulProperties.getOrder().getPost());
		return new SentinelZuulPostFilter(zuulProperties.getOrder().getPost());
	}

	@Bean
	@ConditionalOnMissingBean
	public SentinelZuulErrorFilter sentinelZuulErrorFilter() {
		logger.info("[Sentinel Zuul] register SentinelZuulErrorFilter {}",
				zuulProperties.getOrder().getError());
		return new SentinelZuulErrorFilter(zuulProperties.getOrder().getError());
	}

	@Bean
	public FallBackProviderHandler fallBackProviderHandler(
			DefaultListableBeanFactory beanFactory) {
		return new FallBackProviderHandler(beanFactory);
	}

}
