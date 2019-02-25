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
package org.springframework.cloud.alibaba.dubbo.service;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.service.GenericService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboServiceMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboTransportedMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.dubbo.common.Constants.DEFAULT_CLUSTER;
import static com.alibaba.dubbo.common.Constants.DEFAULT_PROTOCOL;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceGroup;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceInterface;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceSegments;
import static org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistry.getServiceVersion;

/**
 * Dubbo {@link GenericService} Factory
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboGenericServiceFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConcurrentMap<Integer, ReferenceBean<GenericService>> cache = new ConcurrentHashMap<>();

    public GenericService create(DubboServiceMetadata dubboServiceMetadata,
                                 DubboTransportedMetadata dubboTransportedMetadata) {

        ReferenceBean<GenericService> referenceBean = build(dubboServiceMetadata.getServiceRestMetadata(), dubboTransportedMetadata);

        return referenceBean == null ? null : referenceBean.get();
    }

    public GenericService create(String serviceName, Class<?> serviceClass) {
        String interfaceName = serviceClass.getName();
        ReferenceBean<GenericService> referenceBean = build(interfaceName, serviceName, null,
                DEFAULT_PROTOCOL, DEFAULT_CLUSTER);
        return referenceBean.get();
    }


    private ReferenceBean<GenericService> build(ServiceRestMetadata serviceRestMetadata,
                                                DubboTransportedMetadata dubboTransportedMetadata) {
        String dubboServiceName = serviceRestMetadata.getName();
        String[] segments = getServiceSegments(dubboServiceName);
        String interfaceName = getServiceInterface(segments);
        String version = getServiceVersion(segments);
        String group = getServiceGroup(segments);
        String protocol = dubboTransportedMetadata.getProtocol();
        String cluster = dubboTransportedMetadata.getCluster();

        return build(interfaceName, version, group, protocol, cluster);
    }

    private ReferenceBean<GenericService> build(String interfaceName, String version, String group, String protocol,
                                                String cluster) {

        Integer key = Objects.hash(interfaceName, version, group, protocol, cluster);

        ReferenceBean<GenericService> referenceBean = cache.get(key);

        if (referenceBean == null) {
            referenceBean = new ReferenceBean<>();
            referenceBean.setGeneric(true);
            referenceBean.setInterface(interfaceName);
            referenceBean.setVersion(version);
            referenceBean.setGroup(group);
            referenceBean.setProtocol(protocol);
            referenceBean.setCluster(cluster);
        }

        return referenceBean;
    }

    @PreDestroy
    public void destroy() {
        destroyReferenceBeans();
        cache.values();
    }

    private void destroyReferenceBeans() {
        Collection<ReferenceBean<GenericService>> referenceBeans = cache.values();
        if (logger.isInfoEnabled()) {
            logger.info("The Dubbo GenericService ReferenceBeans are destroying...");
        }
        for (ReferenceBean referenceBean : referenceBeans) {
            referenceBean.destroy(); // destroy ReferenceBean
            if (logger.isInfoEnabled()) {
                logger.info("Destroyed the ReferenceBean  : {} ", referenceBean);
            }
        }
    }

}
