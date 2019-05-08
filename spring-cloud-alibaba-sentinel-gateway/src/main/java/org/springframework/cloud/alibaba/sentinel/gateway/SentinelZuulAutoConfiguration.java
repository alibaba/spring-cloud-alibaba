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

package org.springframework.cloud.alibaba.sentinel.gateway;

import java.util.Optional;

import javax.annotation.PostConstruct;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.ZuulGatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulErrorFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPostFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.http.ZuulServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.sentinel.gateway.handler.FallBackProviderHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Sentinel Spring Cloud Zuul AutoConfiguration
 *
 * @author tiger
 */
@Configuration
@ConditionalOnClass(ZuulServlet.class)
@ConditionalOnProperty(prefix = SentinelZuulAutoConfiguration.PREFIX, name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class SentinelZuulAutoConfiguration {

    private static final Logger logger = LoggerFactory
        .getLogger(SentinelZuulAutoConfiguration.class);

    public static final String PREFIX = "spring.cloud.sentinel.zuul";

    @Autowired
    private Environment environment;

    @Autowired
    private Optional<RequestOriginParser> requestOriginParserOptional;

    @PostConstruct
    private void init() {
        requestOriginParserOptional
            .ifPresent(ZuulGatewayCallbackManager::setOriginParser);
    }

    @Bean
    public ZuulFilter sentinelZuulPreFilter() {
        String preOrderStr = environment.getProperty(PREFIX + "." + "order.pre");
        int order = 10000;
        try {
            order = Integer.parseInt(preOrderStr);
        } catch (NumberFormatException e) {
            // ignore
        }
        logger.info("[Sentinel Zuul] register SentinelZuulPreFilter {}", order);
        return new SentinelZuulPreFilter(order);
    }

    @Bean
    public ZuulFilter sentinelZuulPostFilter() {
        String postOrderStr = environment.getProperty(PREFIX + "." + "order.post");
        int order = 1000;
        try {
            order = Integer.parseInt(postOrderStr);
        } catch (NumberFormatException e) {
            // ignore
        }
        logger.info("[Sentinel Zuul] register SentinelZuulPostFilter {}", order);
        return new SentinelZuulPostFilter(order);
    }

    @Bean
    public ZuulFilter sentinelZuulErrorFilter() {
        String errorOrderStr = environment.getProperty(PREFIX + "." + "order.error");
        int order = -1;
        try {
            order = Integer.parseInt(errorOrderStr);
        } catch (NumberFormatException e) {
            // ignore
        }
        logger.info("[Sentinel Zuul] register SentinelZuulErrorFilter {}", order);
        return new SentinelZuulErrorFilter(order);
    }

    @Bean
    public FallBackProviderHandler fallBackProviderHandler(
        DefaultListableBeanFactory beanFactory) {
        return new FallBackProviderHandler(beanFactory);
    }

}
