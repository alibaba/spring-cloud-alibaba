/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.alibaba.dubbo.autoconfigure;

import feign.Feign;
import feign.RequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.alibaba.dubbo.openfeign.DubboFeignClientsConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboOpenFeignAutoConfiguration.FEIGN_CLIENT_FACTORY_BEAN_CLASS_NAME;


/**
 * Dubbo Feign Auto-{@link Configuration Configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@ConditionalOnClass(value = Feign.class, name = FEIGN_CLIENT_FACTORY_BEAN_CLASS_NAME)
@EnableFeignClients(defaultConfiguration = DubboFeignClientsConfiguration.class)
@AutoConfigureAfter(FeignAutoConfiguration.class)
@Configuration
public class DubboOpenFeignAutoConfiguration {

    static final String FEIGN_CLIENT_FACTORY_BEAN_CLASS_NAME =
            "org.springframework.cloud.openfeign.FeignClientFactoryBean";

    @Autowired
    private ObjectProvider<FeignContext> feignContextObjectProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        // Resolve the subscribed service names for @FeignClient
        Set<String> feignClientServiceNames = resolveFeignClientServiceNames(applicationContext);
        // FeignContext
        FeignContext feignContext = feignContextObjectProvider.getIfAvailable();

    }

    /**
     * Resolve the subscribed service names for @FeignClient
     *
     * @param applicationContext Current {@link ConfigurableApplicationContext}
     * @return non-null {@link Set}
     */
    private Set<String> resolveFeignClientServiceNames(ConfigurableApplicationContext applicationContext) {
        Set<String> feignClientServiceNames = new LinkedHashSet<>();
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            if (FEIGN_CLIENT_FACTORY_BEAN_CLASS_NAME.equals(beanDefinition.getBeanClassName())) {
                String feignClientServiceName = (String) beanDefinition.getPropertyValues().get("name");
                feignClientServiceNames.add(feignClientServiceName);
            }
        }
        return feignClientServiceNames;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            System.out.println(template);
        };
    }

}
