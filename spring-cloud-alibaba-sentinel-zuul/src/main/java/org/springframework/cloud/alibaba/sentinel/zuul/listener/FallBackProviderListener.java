package org.springframework.cloud.alibaba.sentinel.zuul.listener;

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
public class FallBackProviderListener implements SmartInitializingSingleton {

	private static final Logger logger = LoggerFactory
			.getLogger(FallBackProviderListener.class);

	private final DefaultListableBeanFactory beanFactory;

	public FallBackProviderListener(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterSingletonsInstantiated() {
		Map<String, ZuulBlockFallbackProvider> providerMap = beanFactory
				.getBeansOfType(ZuulBlockFallbackProvider.class);
		if (MapUtils.isNotEmpty(providerMap)) {
			for (String k : providerMap.keySet()) {
				logger.info("[Sentinel] Register provider name:{}, instance: {}", k,
						providerMap.get(k));
				ZuulBlockFallbackManager.registerProvider(providerMap.get(k));
			}
		}
		else {
			logger.info("[Sentinel] Register default fallback provider. ");
			ZuulBlockFallbackManager.registerProvider(new DefaultBlockFallbackProvider());
		}
	}
}