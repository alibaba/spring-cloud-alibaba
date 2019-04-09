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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alibaba.dubbo.env.DubboCloudProperties;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContextFactory;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceFactory;
import org.springframework.cloud.alibaba.dubbo.service.parameter.PathVariableServiceParameterResolver;
import org.springframework.cloud.alibaba.dubbo.service.parameter.RequestBodyServiceParameterResolver;
import org.springframework.cloud.alibaba.dubbo.service.parameter.RequestHeaderServiceParameterResolver;
import org.springframework.cloud.alibaba.dubbo.service.parameter.RequestParamServiceParameterResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot Auto-Configuration class for Dubbo Service
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@EnableConfigurationProperties(DubboCloudProperties.class)
public class DubboServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DubboGenericServiceFactory dubboGenericServiceFactory() {
        return new DubboGenericServiceFactory();
    }

    @Configuration
    @Import(value = {
            DubboGenericServiceExecutionContextFactory.class,
            RequestParamServiceParameterResolver.class,
            RequestBodyServiceParameterResolver.class,
            RequestHeaderServiceParameterResolver.class,
            PathVariableServiceParameterResolver.class
    })
    static class ParameterResolversConfiguration {
    }

//    /**
//     * Bugfix code for an issue : https://github.com/apache/incubator-dubbo-spring-boot-project/issues/459
//     *
//     * @param environment {@link ConfigurableEnvironment}
//     * @return a Bean of {@link PropertyResolver}
//     */
//    @Primary
//    @Bean(name = BASE_PACKAGES_PROPERTY_RESOLVER_BEAN_NAME)
//    public PropertyResolver dubboScanBasePackagesPropertyResolver(ConfigurableEnvironment environment) {
//        ConfigurableEnvironment propertyResolver = new AbstractEnvironment() {
//            @Override
//            protected void customizePropertySources(MutablePropertySources propertySources) {
//                Map<String, Object> dubboScanProperties = PropertySourcesUtils.getSubProperties(environment, DUBBO_SCAN_PREFIX);
//                propertySources.addLast(new MapPropertySource("dubboScanProperties", dubboScanProperties));
//            }
//        };
//        ConfigurationPropertySources.attach(propertyResolver);
//        return new DelegatingPropertyResolver(propertyResolver);
//    }
//
//
//    private static class DelegatingPropertyResolver implements PropertyResolver {
//
//        private final PropertyResolver delegate;
//
//        DelegatingPropertyResolver(PropertyResolver delegate) {
//            Assert.notNull(delegate, "The delegate of PropertyResolver must not be null");
//            this.delegate = delegate;
//        }
//
//        @Override
//        public boolean containsProperty(String key) {
//            return delegate.containsProperty(key);
//        }
//
//        @Override
//        @Nullable
//        public String getProperty(String key) {
//            return delegate.getProperty(key);
//        }
//
//        @Override
//        public String getProperty(String key, String defaultValue) {
//            return delegate.getProperty(key, defaultValue);
//        }
//
//        @Override
//        @Nullable
//        public <T> T getProperty(String key, Class<T> targetType) {
//            return delegate.getProperty(key, targetType);
//        }
//
//        @Override
//        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
//            return delegate.getProperty(key, targetType, defaultValue);
//        }
//
//        @Override
//        public String getRequiredProperty(String key) throws IllegalStateException {
//            return delegate.getRequiredProperty(key);
//        }
//
//        @Override
//        public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
//            return delegate.getRequiredProperty(key, targetType);
//        }
//
//        @Override
//        public String resolvePlaceholders(String text) {
//            return delegate.resolvePlaceholders(text);
//        }
//
//        @Override
//        public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
//            return delegate.resolveRequiredPlaceholders(text);
//        }
//    }
}