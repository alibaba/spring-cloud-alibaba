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

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;
import org.springframework.cloud.alibaba.dubbo.client.loadbalancer.DubboAdapterLoadBalancerInterceptor;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.type.MethodMetadata;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dubbo Auto-{@link Configuration} for {@link LoadBalanced @LoadBalanced} {@link RestTemplate}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@ConditionalOnClass(RestTemplate.class)
@AutoConfigureAfter(LoadBalancerAutoConfiguration.class)
public class DubboLoadBalancedRestTemplateAutoConfiguration {

    private static final Class<DubboTransported> DUBBO_TRANSPORTED_CLASS = DubboTransported.class;

    private static final String DUBBO_TRANSPORTED_CLASS_NAME = DUBBO_TRANSPORTED_CLASS.getName();

    @Autowired
    private DubboServiceMetadataRepository repository;

    @Autowired
    private LoadBalancerInterceptor loadBalancerInterceptor;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @LoadBalanced
    @Autowired(required = false)
    private Map<String, RestTemplate> restTemplates = Collections.emptyMap();

    /**
     * Adapt the {@link RestTemplate} beans that are annotated  {@link LoadBalanced @LoadBalanced} and
     * {@link LoadBalanced @LoadBalanced} when Spring Boot application started
     * (after the callback of {@link SmartInitializingSingleton} beans or
     * {@link RestTemplateCustomizer#customize(RestTemplate) customization})
     */
    @EventListener(ApplicationStartedEvent.class)
    public void adaptRestTemplates() {
        for (Map.Entry<String, RestTemplate> entry : restTemplates.entrySet()) {
            String beanName = entry.getKey();
            if (isDubboTranslatedAnnotated(beanName)) {
                adaptRestTemplate(entry.getValue());
            }
        }
    }

    /**
     * Judge {@link RestTemplate} bean being annotated {@link DubboTransported @DubboTransported} or not
     *
     * @param beanName the bean name of {@link LoadBalanced @LoadBalanced} {@link RestTemplate}
     * @return
     */
    private boolean isDubboTranslatedAnnotated(String beanName) {
        boolean annotated = false;
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            MethodMetadata factoryMethodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();
            annotated = factoryMethodMetadata != null &&
                    !factoryMethodMetadata.getAnnotationAttributes(DUBBO_TRANSPORTED_CLASS_NAME).isEmpty();
        }
        return annotated;
    }


    /**
     * Adapt the instance of {@link DubboAdapterLoadBalancerInterceptor} to the {@link LoadBalancerInterceptor} Bean.
     *
     * @param restTemplate {@link LoadBalanced @LoadBalanced} {@link RestTemplate} Bean
     */
    private void adaptRestTemplate(RestTemplate restTemplate) {

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());

        int index = interceptors.indexOf(loadBalancerInterceptor);

        if (index > -1) {
            interceptors.set(index, new DubboAdapterLoadBalancerInterceptor(repository, loadBalancerInterceptor,
                    restTemplate.getMessageConverters()));
        }

        restTemplate.setInterceptors(interceptors);
    }

}
