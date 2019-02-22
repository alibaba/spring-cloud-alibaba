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

import com.alibaba.csp.sentinel.adapter.zuul.fallback.DefaultRequestOriginParser;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.DefaultUrlCleaner;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.zuul.fallback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelErrorFilter;
import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelPostFilter;
import com.alibaba.csp.sentinel.adapter.zuul.filters.SentinelPreFilter;
import com.alibaba.csp.sentinel.adapter.zuul.properties.SentinelZuulProperties;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.netflix.zuul.ZuulFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.sentinel.zuul.listener.FallBackProviderListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.springframework.cloud.alibaba.sentinel.zuul.SentinelZuulAutoConfiguration.PREFIX;

/**
 * Sentinel Spring Cloud Zuul AutoConfiguration
 *
 * @author tiger
 */
@Configuration
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class SentinelZuulAutoConfiguration {

    @Autowired
    private Environment environment;

    public static final String PREFIX = "spring.cloud.alibaba.sentinel.zuul";

    @Bean
    public SentinelZuulProperties sentinelZuulProperties() {
        SentinelZuulProperties properties = new SentinelZuulProperties();
        String enabledStr = environment.getProperty(PREFIX + "." + "enabled");
        String preOrderStr = environment.getProperty(PREFIX + "." + "order.pre");
        String postOrderStr = environment.getProperty(PREFIX + "." + "order.post");
        String errorOrderStr = environment.getProperty(PREFIX + "." + "order.error");
        if (StringUtil.isNotEmpty(enabledStr)) {
            Boolean enabled = Boolean.valueOf(enabledStr);
            properties.setEnabled(enabled);
        }
        if (StringUtil.isNotEmpty(preOrderStr)) {
            properties.getOrder().setPre(Integer.parseInt(preOrderStr));
        }
        if (StringUtil.isNotEmpty(postOrderStr)) {
            properties.getOrder().setPost(Integer.parseInt(postOrderStr));
        }
        if (StringUtil.isNotEmpty(errorOrderStr)) {
            properties.getOrder().setError(Integer.parseInt(errorOrderStr));
        }
        return properties;
    }

    @Bean
    @ConditionalOnMissingBean(UrlCleaner.class)
    public UrlCleaner urlCleaner(){
        return new DefaultUrlCleaner();
    }

    @Bean
    @ConditionalOnMissingBean(RequestOriginParser.class)
    public RequestOriginParser requestOriginParser(){
        return new DefaultRequestOriginParser();
    }

    @Bean
    public ZuulFilter preFilter(SentinelZuulProperties sentinelZuulProperties,UrlCleaner urlCleaner,
                                RequestOriginParser requestOriginParser) {
        return new SentinelPreFilter(sentinelZuulProperties,urlCleaner,requestOriginParser);
    }

    @Bean
    public ZuulFilter postFilter(SentinelZuulProperties sentinelZuulProperties) {
        return new SentinelPostFilter(sentinelZuulProperties);
    }

    @Bean
    public ZuulFilter errorFilter(SentinelZuulProperties sentinelZuulProperties) {
        return new SentinelErrorFilter(sentinelZuulProperties);
    }

    @Bean
    public FallBackProviderListener fallBackProviderListener(DefaultListableBeanFactory beanFactory) {
        return new FallBackProviderListener(beanFactory);
    }

}
