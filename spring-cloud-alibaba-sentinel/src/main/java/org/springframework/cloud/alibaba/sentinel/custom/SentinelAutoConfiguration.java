/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alibaba.sentinel.custom;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alibaba.sentinel.SentinelProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.SentinelDataSourcePostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;

import javax.annotation.PostConstruct;

/**
 * @author xiaojing
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelAutoConfiguration {

    @Value("${project.name:${spring.application.name:}}")
    private String projectName;

    @Autowired
    private SentinelProperties properties;

    @PostConstruct
    private void init() {
        if (StringUtils.isEmpty(System.getProperty(AppNameUtil.APP_NAME))) {
            System.setProperty(AppNameUtil.APP_NAME, projectName);
        }
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.SERVER_PORT))) {
            System.setProperty(TransportConfig.SERVER_PORT, properties.getPort());
        }
        if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))) {
            System.setProperty(TransportConfig.CONSOLE_SERVER, properties.getDashboard());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
    public SentinelBeanPostProcessor sentinelBeanPostProcessor() {
        return new SentinelBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelDataSourcePostProcessor sentinelDataSourcePostProcessor() {
        return new SentinelDataSourcePostProcessor();
    }

}
