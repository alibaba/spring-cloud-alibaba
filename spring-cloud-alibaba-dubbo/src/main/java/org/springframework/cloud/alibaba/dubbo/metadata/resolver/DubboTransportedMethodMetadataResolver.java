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
package org.springframework.cloud.alibaba.dubbo.metadata.resolver;

import feign.Contract;
import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboTransportedMethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.MethodMetadata;
import org.springframework.cloud.alibaba.dubbo.metadata.RestMethodMetadata;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static feign.Feign.configKey;

/**
 * {@link MethodMetadata} Resolver for the {@link DubboTransported}  annotated classes or methods in client side.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DubboTransportedMethodMetadata
 */
public class DubboTransportedMethodMetadataResolver {

    private static final Class<DubboTransported> DUBBO_TRANSPORTED_CLASS = DubboTransported.class;

    private final PropertyResolver propertyResolver;

    private final Contract contract;

    public DubboTransportedMethodMetadataResolver(PropertyResolver propertyResolver, Contract contract) {
        this.propertyResolver = propertyResolver;
        this.contract = contract;
    }

    public Map<DubboTransportedMethodMetadata, RestMethodMetadata> resolve(Class<?> targetType) {
        Set<DubboTransportedMethodMetadata> dubboTransportedMethodMetadataSet =
                resolveDubboTransportedMethodMetadataSet(targetType);
        Map<String, RestMethodMetadata> restMethodMetadataMap = resolveRestRequestMetadataMap(targetType);
        return dubboTransportedMethodMetadataSet
                .stream()
                .collect(Collectors.toMap(methodMetadata -> methodMetadata, methodMetadata -> {
                            RestMethodMetadata restMethodMetadata = restMethodMetadataMap.get(configKey(targetType, methodMetadata.getMethod()));
                            restMethodMetadata.setMethod(methodMetadata.getMethodMetadata());
                            return restMethodMetadata;
                        }
                ));
    }

    protected Set<DubboTransportedMethodMetadata> resolveDubboTransportedMethodMetadataSet(Class<?> targetType) {
        // The public methods of target interface
        Method[] methods = targetType.getMethods();

        Set<DubboTransportedMethodMetadata> methodMetadataSet = new LinkedHashSet<>();

        for (Method method : methods) {
            DubboTransported dubboTransported = resolveDubboTransported(method);
            if (dubboTransported != null) {
                DubboTransportedMethodMetadata methodMetadata = createDubboTransportedMethodMetadata(method, dubboTransported);
                methodMetadataSet.add(methodMetadata);
            }
        }
        return methodMetadataSet;
    }


    private Map<String, RestMethodMetadata> resolveRestRequestMetadataMap(Class<?> targetType) {
        return contract.parseAndValidatateMetadata(targetType)
                .stream().collect(Collectors.toMap(feign.MethodMetadata::configKey, this::restMethodMetadata));
    }

    private RestMethodMetadata restMethodMetadata(feign.MethodMetadata methodMetadata) {
        return new RestMethodMetadata(methodMetadata);
    }

    private DubboTransportedMethodMetadata createDubboTransportedMethodMetadata(Method method,
                                                                                DubboTransported dubboTransported) {
        DubboTransportedMethodMetadata methodMetadata = new DubboTransportedMethodMetadata(method);
        String protocol = propertyResolver.resolvePlaceholders(dubboTransported.protocol());
        String cluster = propertyResolver.resolvePlaceholders(dubboTransported.cluster());
        methodMetadata.setProtocol(protocol);
        methodMetadata.setCluster(cluster);
        return methodMetadata;
    }

    private DubboTransported resolveDubboTransported(Method method) {
        DubboTransported dubboTransported = AnnotationUtils.findAnnotation(method, DUBBO_TRANSPORTED_CLASS);
        if (dubboTransported == null) { // Attempt to find @DubboTransported in the declaring class
            Class<?> declaringClass = method.getDeclaringClass();
            dubboTransported = AnnotationUtils.findAnnotation(declaringClass, DUBBO_TRANSPORTED_CLASS);
        }
        return dubboTransported;
    }
}
