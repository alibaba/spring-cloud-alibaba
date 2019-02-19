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
package org.springframework.cloud.alibaba.dubbo.metadata.service;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.service.GenericService;

import org.springframework.cloud.alibaba.dubbo.metadata.DubboServiceMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboTransportedMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.ServiceRestMetadata;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private final ConcurrentMap<Integer, GenericService> cache = new ConcurrentHashMap<>();

    public GenericService create(DubboServiceMetadata dubboServiceMetadata,
                                 DubboTransportedMetadata dubboTransportedMetadata) {

        Integer key = Objects.hash(dubboServiceMetadata, dubboTransportedMetadata);

        GenericService genericService = cache.get(key);

        if (genericService == null) {
            genericService = build(dubboServiceMetadata.getServiceRestMetadata(), dubboTransportedMetadata);
            cache.putIfAbsent(key, genericService);
        }

        return genericService;
    }


    private GenericService build(ServiceRestMetadata serviceRestMetadata,
                                 DubboTransportedMetadata dubboTransportedMetadata) {
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
        referenceBean.setProtocol(dubboTransportedMetadata.getProtocol());
        referenceBean.setCluster(dubboTransportedMetadata.getCluster());

        return referenceBean.get();
    }

}
