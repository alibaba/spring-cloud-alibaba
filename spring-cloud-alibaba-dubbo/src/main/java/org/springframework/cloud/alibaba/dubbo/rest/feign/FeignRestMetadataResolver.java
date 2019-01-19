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
package org.springframework.cloud.alibaba.dubbo.rest.feign;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Contract;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestTemplate;
import feign.jaxrs2.JAXRS2Contract;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry;
import org.springframework.cloud.alibaba.dubbo.rest.metadata.MethodRestMetadata;
import org.springframework.cloud.alibaba.dubbo.rest.metadata.ServiceRestMetadata;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The REST metadata resolver for Feign
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class FeignRestMetadataResolver implements BeanClassLoaderAware, SmartInitializingSingleton {

    private static final String METHOD_PROPERTY_NAME = "method";
    private static final String URL_PROPERTY_NAME = "url";
    private static final String HEADERS_PROPERTY_NAME = "headers";

    private static final String[] CONTRACT_CLASS_NAMES = {
            "feign.jaxrs2.JAXRS2Contract",
            "org.springframework.cloud.openfeign.support.SpringMvcContract",
    };

    private final ObjectMapper objectMapper;

    private final ObjectProvider<Contract> contract;

    /**
     * Feign Contracts
     */
    private Collection<Contract> contracts;

    private ClassLoader classLoader;

    @Value("${spring.application.name}")
    private String currentApplicationName;

    public FeignRestMetadataResolver(ObjectProvider<ObjectMapper> objectMapper, ObjectProvider<Contract> contract) {
        this.objectMapper = objectMapper.getIfAvailable();
        this.contract = contract;
    }

    @Override
    public void afterSingletonsInstantiated() {

        LinkedList<Contract> contracts = new LinkedList<>();

        // Add injected Contract if available, for example SpringMvcContract Bean under Spring Cloud Open Feign
        contract.ifAvailable(contracts::add);

        Stream.of(CONTRACT_CLASS_NAMES)
                .filter(this::isClassPresent) // filter the existed classes
                .map(this::loadContractClass) // load Contract Class
                .map(this::createContract)    // create instance by the specified class
                .forEach(contracts::add);     // add the Contract instance into contracts

        this.contracts = Collections.unmodifiableCollection(contracts);
    }

    private Contract createContract(Class<?> contractClassName) {
        return (Contract) BeanUtils.instantiateClass(contractClassName);
    }

    private Class<?> loadContractClass(String contractClassName) {
        return ClassUtils.resolveClassName(contractClassName, classLoader);
    }

    private boolean isClassPresent(String className) {
        return ClassUtils.isPresent(className, classLoader);
    }

    public Set<ServiceRestMetadata> resolveServiceRestMetadata(ServiceBean serviceBean) {

        Object bean = serviceBean.getRef();

        Class<?> beanType = bean.getClass();

        Class<?> interfaceClass = serviceBean.getInterfaceClass();

        Set<ServiceRestMetadata> serviceRestMetadata = new LinkedHashSet<>();

        Set<MethodRestMetadata> methodRestMetadata = resolveMethodRestMetadata(beanType, interfaceClass);

        List<URL> urls = serviceBean.getExportedUrls();

        urls.stream()
                .map(SpringCloudRegistry::getServiceName)
                .forEach(serviceName -> {
                    ServiceRestMetadata metadata = new ServiceRestMetadata();
                    metadata.setName(serviceName);
                    metadata.setMeta(methodRestMetadata);
                    serviceRestMetadata.add(metadata);
                });

        return serviceRestMetadata;
    }

    public Set<MethodRestMetadata> resolveMethodRestMetadata(Class<?> targetClass) {
        return resolveMethodRestMetadata(targetClass, targetClass);
    }

    protected Set<MethodRestMetadata> resolveMethodRestMetadata(Class<?> targetClass, Class revisedClass) {
        return contracts.stream()
                .map(contract -> contract.parseAndValidatateMetadata(targetClass))
                .flatMap(v -> v.stream())
                .map(methodMetadata -> resolveMethodRestMetadata(methodMetadata, targetClass, revisedClass))
                .collect(Collectors.toSet());
    }

    private String toJson(Object object) {
        String jsonContent = null;
        try {
            jsonContent = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        return jsonContent;
    }

    private String regenerateConfigKey(String configKey, Class<?> beanType, Class<?> interfaceClass) {
        if (beanType.equals(interfaceClass)) {
            return configKey;
        }
        return StringUtils.replace(configKey, beanType.getSimpleName(), interfaceClass.getSimpleName());
    }

    protected MethodRestMetadata resolveMethodRestMetadata(MethodMetadata methodMetadata, Class<?> beanType,
                                                           Class<?> interfaceClass) {
        RequestTemplate requestTemplate = methodMetadata.template();
        Request request = requestTemplate.request();

        String configKey = methodMetadata.configKey();
        String newConfigKey = regenerateConfigKey(configKey, beanType, interfaceClass);

        MethodRestMetadata methodRestMetadata = new MethodRestMetadata();
        methodRestMetadata.setConfigKey(newConfigKey);
        methodRestMetadata.setMethod(request.method());
        methodRestMetadata.setUrl(request.url());
        methodRestMetadata.setHeaders(request.headers());
        methodRestMetadata.setIndexToName(methodMetadata.indexToName());

        return methodRestMetadata;
    }

    public Request resolveRequest(String json) {
        Request request = null;
        try {
            Map<String, Object> data = objectMapper.readValue(json, Map.class);
            String method = (String) data.get(METHOD_PROPERTY_NAME);
            String url = (String) data.get(URL_PROPERTY_NAME);
            Map<String, Collection<String>> headers = (Map) data.get(HEADERS_PROPERTY_NAME);
            request = Request.create(method, url, headers, null, null);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return request;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

}