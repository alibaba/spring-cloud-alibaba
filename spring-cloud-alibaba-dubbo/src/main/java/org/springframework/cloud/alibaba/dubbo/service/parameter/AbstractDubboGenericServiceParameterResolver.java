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
package org.springframework.cloud.alibaba.dubbo.service.parameter;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.FormattingConversionService;

import static org.springframework.context.ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * Abstract {@link DubboGenericServiceParameterResolver} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class AbstractDubboGenericServiceParameterResolver implements DubboGenericServiceParameterResolver,
        BeanClassLoaderAware {

    private int order;

    @Autowired(required = false)
    @Qualifier(CONVERSION_SERVICE_BEAN_NAME)
    private ConversionService conversionService = new FormattingConversionService();

    private ClassLoader classLoader;

    public ConversionService getConversionService() {
        return conversionService;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    protected Class<?> resolveClass(String className) {
        return resolveClassName(className, classLoader);
    }

    protected Object resolveValue(Object parameterValue, String parameterType) {
        Class<?> targetType = resolveClass(parameterType);
        return resolveValue(parameterValue, targetType);
    }

    protected Object resolveValue(Object parameterValue, Class<?> parameterType) {
        return conversionService.convert(parameterValue, parameterType);
    }
}
