/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel.zuul;

import static org.springframework.cloud.commons.util.InetUtilsProperties.PREFIX;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.sentinel.zuul.handler.FallBackProviderHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.DefaultRequestOriginParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulErrorFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPostFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;

import com.netflix.zuul.ZuulFilter;

/**
 * Sentinel Spring Cloud Zuul AutoConfiguration
 *
 * @author tiger
 */
@Configuration
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class SentinelZuulAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(RequestOriginParser.class)
	public RequestOriginParser requestOriginParser() {
		return new DefaultRequestOriginParser();
	}

	@Bean
	public ZuulFilter sentinelZuulPreFilter() {
		// We can also provider the filter order in the constructor.
		return new SentinelZuulPreFilter();
	}

	@Bean
	public ZuulFilter sentinelZuulPostFilter() {
		return new SentinelZuulPostFilter();
	}

	@Bean
	public ZuulFilter sentinelZuulErrorFilter() {
		return new SentinelZuulErrorFilter();
	}

	@Bean
	public FallBackProviderHandler fallBackProviderListener(
			DefaultListableBeanFactory beanFactory) {
		return new FallBackProviderHandler(beanFactory);
	}

}
