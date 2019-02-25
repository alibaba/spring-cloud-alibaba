package org.springframework.cloud.alibaba.sentinel.zuul.listener;

import com.alibaba.csp.sentinel.adapter.zuul.fallback.DefaultBlockFallbackProvider;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.ZuulBlockFallbackProvider;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;


/**
 * @author tiger
 */
public class FallBackProviderListener implements SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(FallBackProviderListener.class);

    private final DefaultListableBeanFactory beanFactory;

    public FallBackProviderListener(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, ZuulBlockFallbackProvider> providerMap = beanFactory.getBeansOfType(ZuulBlockFallbackProvider.class);
        if (MapUtils.isNotEmpty(providerMap)) {
            providerMap.forEach((k, v) -> {
                logger.info("[Sentinel] Register provider name:{}, instance: {}", k, v);
                ZuulBlockFallbackManager.registerProvider(v);
            });
        } else {
            logger.info("[Sentinel] Register default fallback provider. ");
            ZuulBlockFallbackManager.registerProvider(new DefaultBlockFallbackProvider());
        }
    }
}
