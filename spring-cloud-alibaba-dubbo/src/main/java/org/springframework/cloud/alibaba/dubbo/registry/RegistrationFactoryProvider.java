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
package org.springframework.cloud.alibaba.dubbo.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;

import java.util.List;

import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.ResolvableType.forInstance;
import static org.springframework.core.ResolvableType.forType;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.isPresent;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * {@link RegistrationFactory} Provider
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class RegistrationFactoryProvider implements FactoryBean<RegistrationFactory>, ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private RegistrationFactory registrationFactory;

    @Override
    public RegistrationFactory getObject() throws BeansException {
        return registrationFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return RegistrationFactory.class;
    }

    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ServiceRegistry<Registration> serviceRegistry = applicationContext.getBean(ServiceRegistry.class);
        ClassLoader classLoader = applicationContext.getClassLoader();
        this.registrationFactory = buildRegistrationFactory(serviceRegistry, classLoader);
    }

    private RegistrationFactory buildRegistrationFactory(ServiceRegistry<Registration> serviceRegistry,
                                                         ClassLoader classLoader) {
        RegistrationFactory registrationFactory = null;
        List<String> factoryClassNames = loadFactoryNames(RegistrationFactory.class, classLoader);

        ResolvableType serviceRegistryType = forInstance(serviceRegistry);
        // Get first generic Class
        Class<?> registrationClass = resolveGenericClass(serviceRegistryType, ServiceRegistry.class, 0);

        for (String factoryClassName : factoryClassNames) {
            if (isPresent(factoryClassName, classLoader)) { // ignore compilation issue
                Class<?> factoryClass = resolveClassName(factoryClassName, classLoader);
                ResolvableType registrationFactoryType = forType(factoryClass);
                Class<?> actualRegistrationClass = resolveGenericClass(registrationFactoryType, RegistrationFactory.class, 0);
                if (registrationClass.equals(actualRegistrationClass)) {
                    registrationFactory = (RegistrationFactory) instantiateClass(registrationFactoryType.getRawClass());
                    break;
                }
            }
        }

        if (registrationFactory == null) {

            if (logger.isWarnEnabled()) {
                logger.warn("{} implementation can't be resolved by ServiceRegistry[{}]",
                        registrationClass.getSimpleName(), serviceRegistry.getClass().getName());
            }

            registrationFactory = new DefaultRegistrationFactory();
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("{} has been resolved by ServiceRegistry[{}]",
                        registrationFactory.getClass().getName(), serviceRegistry.getClass().getName());
            }
        }

        return registrationFactory;
    }

    private Class<?> resolveGenericClass(ResolvableType implementedType, Class<?> interfaceClass, int index) {

        ResolvableType resolvableType = implementedType;

        try {
            OUTER:
            while (true) {

                ResolvableType[] interfaceTypes = resolvableType.getInterfaces();

                for (ResolvableType interfaceType : interfaceTypes) {
                    if (interfaceType.resolve().equals(interfaceClass)) {
                        resolvableType = interfaceType;
                        break OUTER;
                    }
                }

                ResolvableType superType = resolvableType.getSuperType();

                Class<?> superClass = superType.resolve();

                if (Object.class.equals(superClass)) {
                    break;
                }

                resolvableType = superType;
            }

        } catch (Throwable e) {
            resolvableType = ResolvableType.forType(void.class);
        }

        return resolvableType.resolveGeneric(index);
    }

}
