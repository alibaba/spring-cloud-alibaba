package com.alibaba.cloud.sentinel.gateway.zuul;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.DefaultBlockFallbackProvider;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;

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
