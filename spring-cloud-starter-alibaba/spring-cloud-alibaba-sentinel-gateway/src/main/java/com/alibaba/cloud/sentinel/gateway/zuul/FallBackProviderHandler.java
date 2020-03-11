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

import java.util.Map;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.DefaultBlockFallbackProvider;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.CollectionUtils;

/**
 * @author tiger
 */
public class FallBackProviderHandler implements SmartInitializingSingleton {

	private static final Logger logger = LoggerFactory
			.getLogger(FallBackProviderHandler.class);

	private final DefaultListableBeanFactory beanFactory;

	public FallBackProviderHandler(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterSingletonsInstantiated() {
		Map<String, ZuulBlockFallbackProvider> providerMap = beanFactory
				.getBeansOfType(ZuulBlockFallbackProvider.class);
		if (!CollectionUtils.isEmpty(providerMap)) {
			providerMap.forEach((k, v) -> {
				logger.info("[Sentinel Zuul] Register provider name:{}, instance: {}", k,
						v);
				ZuulBlockFallbackManager.registerProvider(v);
			});
		}
		else {
			logger.info("[Sentinel Zuul] Register default fallback provider. ");
			ZuulBlockFallbackManager.registerProvider(new DefaultBlockFallbackProvider());
		}
	}

}
