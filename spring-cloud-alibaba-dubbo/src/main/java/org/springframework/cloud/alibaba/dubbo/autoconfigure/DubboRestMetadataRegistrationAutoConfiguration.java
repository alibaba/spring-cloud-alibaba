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

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.context.event.ServiceBeanExportedEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Contract;
import feign.jaxrs2.JAXRS2Contract;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.alibaba.dubbo.rest.feign.RestMetadataResolver;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceName;

/**
 * The Auto-Configuration class for Dubbo REST metadata registration,
 * REST metadata that is a part of {@link Registration#getMetadata() Spring Cloud service instances' metadata}
 * will be registered Spring Cloud registry.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@AutoConfigureAfter(value = {
        DubboRestAutoConfiguration.class, DubboServiceRegistrationAutoConfiguration.class})
public class DubboRestMetadataRegistrationAutoConfiguration implements BeanClassLoaderAware {

    /**
     * A Map to store REST metadata temporary, its' key is the special service name for a Dubbo service,
     * the value is a JSON content of JAX-RS or Spring MVC REST metadata from the annotated methods.
     */
    private final Map<String, Set<String>> restMetadata = new LinkedHashMap<>();

    /**
     * Feign Contracts
     */
    private Collection<Contract> contracts = Collections.emptyList();

    private ClassLoader classLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestMetadataResolver restMetadataResolver;

    @PostConstruct
    public void init() {
        contracts = initFeignContracts();
    }

    private Collection<Contract> initFeignContracts() {
        Collection<Contract> contracts = new LinkedList<>();

        if (ClassUtils.isPresent("javax.ws.rs.Path", classLoader)) {
            contracts.add(new JAXRS2Contract());
        }

        if (ClassUtils.isPresent("org.springframework.web.bind.annotation.RequestMapping", classLoader)) {
            contracts.add(new SpringMvcContract());
        }

        return contracts;
    }


    @EventListener(ServiceBeanExportedEvent.class)
    public void recordRestMetadata(ServiceBeanExportedEvent event) {
        ServiceBean serviceBean = event.getServiceBean();
        List<URL> urls = serviceBean.getExportedUrls();
        Object bean = serviceBean.getRef();

        Set<String> metadata = contracts.stream()
                .map(contract -> contract.parseAndValidatateMetadata(bean.getClass()))
                .flatMap(v -> v.stream())
                .map(restMetadataResolver::resolve)
                .collect(Collectors.toSet());

        urls.forEach(url -> {
            String serviceName = getServiceName(url);
            restMetadata.put(serviceName, metadata);
        });
    }

    /**
     * Pre-handle Spring Cloud application service registered:
     * <p>
     * Put <code>restMetadata</code> with the JSON format into
     * {@link Registration#getMetadata() service instances' metadata}
     * <p>
     *
     * @param event {@link InstancePreRegisteredEvent} instance
     */
    @EventListener(InstancePreRegisteredEvent.class)
    public void registerRestMetadata(InstancePreRegisteredEvent event) throws JsonProcessingException {
        Registration registration = event.getRegistration();
        Map<String, String> serviceInstanceMetadata = registration.getMetadata();
        String restMetadataJson = objectMapper.writeValueAsString(restMetadata);
        serviceInstanceMetadata.put("restMetadata", restMetadataJson);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
