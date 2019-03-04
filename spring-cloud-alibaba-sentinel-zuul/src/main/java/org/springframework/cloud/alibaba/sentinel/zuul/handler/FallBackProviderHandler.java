package org.springframework.cloud.alibaba.sentinel.zuul.handler;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.alibaba.csp.sentinel.adapter.zuul.fallback.DefaultBlockFallbackProvider;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.ZuulBlockFallbackProvider;

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
		if (MapUtils.isNotEmpty(providerMap)) {
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
