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

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;
import org.springframework.cloud.alibaba.dubbo.client.loadbalancer.DubboMetadataInitializerInterceptor;
import org.springframework.cloud.alibaba.dubbo.client.loadbalancer.DubboTransporterInterceptor;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.metadata.resolver.DubboTransportedAttributesResolver;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContextFactory;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.type.MethodMetadata;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
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
@ConditionalOnClass(name = {"org.springframework.web.client.RestTemplate"})
@AutoConfigureAfter(name = {"org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration"})
public class DubboLoadBalancedRestTemplateAutoConfiguration implements BeanClassLoaderAware {

    private static final Class<DubboTransported> DUBBO_TRANSPORTED_CLASS = DubboTransported.class;

    private static final String DUBBO_TRANSPORTED_CLASS_NAME = DUBBO_TRANSPORTED_CLASS.getName();

    @Autowired
    private DubboServiceMetadataRepository repository;

    @Autowired
    private LoadBalancerInterceptor loadBalancerInterceptor;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private DubboGenericServiceFactory serviceFactory;

    @Autowired
    private DubboGenericServiceExecutionContextFactory contextFactory;

    @Autowired
    private Environment environment;

    @LoadBalanced
    @Autowired(required = false)
    private Map<String, RestTemplate> restTemplates = Collections.emptyMap();

    private ClassLoader classLoader;


    /**
     * Adapt the {@link RestTemplate} beans that are annotated  {@link LoadBalanced @LoadBalanced} and
     * {@link LoadBalanced @LoadBalanced} when Spring Boot application started
     * (after the callback of {@link SmartInitializingSingleton} beans or
     * {@link RestTemplateCustomizer#customize(RestTemplate) customization})
     */
    @EventListener(ApplicationStartedEvent.class)
    public void adaptRestTemplates() {

        DubboTransportedAttributesResolver attributesResolver = new DubboTransportedAttributesResolver(environment);

        for (Map.Entry<String, RestTemplate> entry : restTemplates.entrySet()) {
            String beanName = entry.getKey();
            Map<String, Object> dubboTranslatedAttributes = getDubboTranslatedAttributes(beanName, attributesResolver);
            if (!CollectionUtils.isEmpty(dubboTranslatedAttributes)) {
                adaptRestTemplate(entry.getValue(), dubboTranslatedAttributes);
            }
        }
    }

    /**
     * Gets the annotation attributes {@link RestTemplate} bean being annotated
     * {@link DubboTransported @DubboTransported}
     *
     * @param beanName           the bean name of {@link LoadBalanced @LoadBalanced} {@link RestTemplate}
     * @param attributesResolver {@link DubboTransportedAttributesResolver}
     * @return non-null {@link Map}
     */
    private Map<String, Object> getDubboTranslatedAttributes(String beanName,
                                                             DubboTransportedAttributesResolver attributesResolver) {
        Map<String, Object> attributes = Collections.emptyMap();
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            MethodMetadata factoryMethodMetadata = annotatedBeanDefinition.getFactoryMethodMetadata();
            attributes = factoryMethodMetadata != null ?
                    factoryMethodMetadata.getAnnotationAttributes(DUBBO_TRANSPORTED_CLASS_NAME) : Collections.emptyMap();
        }
        return attributesResolver.resolve(attributes);
    }


    /**
     * Adapt the instance of {@link DubboTransporterInterceptor} to the {@link LoadBalancerInterceptor} Bean.
     *
     * @param restTemplate              {@link LoadBalanced @LoadBalanced} {@link RestTemplate} Bean
     * @param dubboTranslatedAttributes the annotation dubboTranslatedAttributes {@link RestTemplate} bean being annotated
     *                                  {@link DubboTransported @DubboTransported}
     */
    private void adaptRestTemplate(RestTemplate restTemplate, Map<String, Object> dubboTranslatedAttributes) {

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());

        int index = interceptors.indexOf(loadBalancerInterceptor);

        index = index < 0 ? 0 : index;

        // Add ClientHttpRequestInterceptor instances before loadBalancerInterceptor
        interceptors.add(index++, new DubboMetadataInitializerInterceptor(repository));

        interceptors.add(index++, new DubboTransporterInterceptor(repository, restTemplate.getMessageConverters(),
                classLoader, dubboTranslatedAttributes, serviceFactory, contextFactory));

        restTemplate.setInterceptors(interceptors);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
