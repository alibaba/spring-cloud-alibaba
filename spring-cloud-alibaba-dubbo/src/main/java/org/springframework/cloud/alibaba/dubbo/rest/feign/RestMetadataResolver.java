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
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * The JSON resolver for {@link MethodMetadata}
 */
public class RestMetadataResolver implements BeanClassLoaderAware, SmartInitializingSingleton {

    private static final String METHOD_PROPERTY_NAME = "method";
    private static final String URL_PROPERTY_NAME = "url";
    private static final String HEADERS_PROPERTY_NAME = "headers";

    private final ObjectMapper objectMapper;

    /**
     * Feign Contracts
     */
    private Collection<Contract> contracts;

    private ClassLoader classLoader;

    @Autowired
    private ObjectProvider<Contract> contractObjectProvider;

    public RestMetadataResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterSingletonsInstantiated() {

        Collection<Contract> contracts = new LinkedList<>();

        // Add injected Contract , for example SpringMvcContract Bean under Spring Cloud Open Feign
        Contract contract = contractObjectProvider.getIfAvailable();
        if (contract != null) {
            contracts.add(contract);
        } else {
            if (ClassUtils.isPresent("org.springframework.cloud.openfeign.support.SpringMvcContract", classLoader)) {
                contracts.add(new SpringMvcContract());
            }
        }

        // Add JAXRS2Contract if it's present in Class Path
        if (ClassUtils.isPresent("javax.ws.rs.Path", classLoader)) {
            contracts.add(new JAXRS2Contract());
        }

        this.contracts = Collections.unmodifiableCollection(contracts);
    }

    public Set<ServiceRestMetadata> resolve(ServiceBean serviceBean) {

        Object bean = serviceBean.getRef();

        Class<?> beanType = bean.getClass();

        Class<?> interfaceClass = serviceBean.getInterfaceClass();

        Set<ServiceRestMetadata> serviceRestMetadata = new LinkedHashSet<>();

        Set<MethodRestMetadata> methodRestMetadata = new LinkedHashSet<>();

        contracts.stream()
                .map(contract -> contract.parseAndValidatateMetadata(bean.getClass()))
                .flatMap(v -> v.stream())
                .forEach(methodMetadata -> {
                    methodRestMetadata.add(resolveMethodRestMetadata(methodMetadata, beanType, interfaceClass));
                });

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