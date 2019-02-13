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
package org.springframework.cloud.alibaba.dubbo.metadata.repository;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.service.MetadataConfigService;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceGroup;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceInterface;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceSegments;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceVersion;

/**
 * Dubbo Service Metadata {@link Repository}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Repository
public class DubboServiceMetadataRepository {

    /**
     * Key is application name
     * Value is  Map<RequestMetadata, GenericService>
     */
    private Map<String, Map<RequestMetadata, GenericService>> genericServicesRepository = new HashMap<>();

    private Map<String, Map<RequestMetadata, MethodMetadata>> methodMetadataRepository = new HashMap<>();

    @Autowired
    private MetadataConfigService metadataConfigService;

    @Value("${dubbo.target.protocol:dubbo}")
    private String targetProtocol;

    @Value("${dubbo.target.cluster:failover}")
    private String targetCluster;

    public void updateMetadata(String serviceName) {

        Map<RequestMetadata, GenericService> genericServicesMap = genericServicesRepository.computeIfAbsent(serviceName, k -> new HashMap<>());

        Map<RequestMetadata, MethodMetadata> methodMetadataMap = methodMetadataRepository.computeIfAbsent(serviceName, k -> new HashMap<>());

        Set<ServiceRestMetadata> serviceRestMetadataSet = metadataConfigService.getServiceRestMetadata(serviceName);

        for (ServiceRestMetadata serviceRestMetadata : serviceRestMetadataSet) {

            ReferenceBean<GenericService> referenceBean = adaptReferenceBean(serviceRestMetadata);

            serviceRestMetadata.getMeta().forEach(restMethodMetadata -> {
                RequestMetadata requestMetadata = restMethodMetadata.getRequest();
                genericServicesMap.put(requestMetadata, referenceBean.get());
                methodMetadataMap.put(requestMetadata, restMethodMetadata.getMethod());
            });
        }
    }

    public GenericService getGenericService(String serviceName, RequestMetadata requestMetadata) {
        return getGenericServicesMap(serviceName).get(requestMetadata);
    }

    public MethodMetadata getMethodMetadata(String serviceName, RequestMetadata requestMetadata) {
        return getMethodMetadataMap(serviceName).get(requestMetadata);
    }

    private ReferenceBean<GenericService> adaptReferenceBean(ServiceRestMetadata serviceRestMetadata) {
        String dubboServiceName = serviceRestMetadata.getName();
        String[] segments = getServiceSegments(dubboServiceName);
        String interfaceName = getServiceInterface(segments);
        String version = getServiceVersion(segments);
        String group = getServiceGroup(segments);

        ReferenceBean<GenericService> referenceBean = new ReferenceBean<GenericService>();
        referenceBean.setGeneric(true);
        referenceBean.setInterface(interfaceName);
        referenceBean.setVersion(version);
        referenceBean.setGroup(group);
        referenceBean.setProtocol(targetProtocol);
        referenceBean.setCluster(targetCluster);

        return referenceBean;
    }

    private Map<RequestMetadata, GenericService> getGenericServicesMap(String serviceName) {
        return genericServicesRepository.getOrDefault(serviceName, Collections.emptyMap());
    }

    private Map<RequestMetadata, MethodMetadata> getMethodMetadataMap(String serviceName) {
        return methodMetadataRepository.getOrDefault(serviceName, Collections.emptyMap());
    }

}
