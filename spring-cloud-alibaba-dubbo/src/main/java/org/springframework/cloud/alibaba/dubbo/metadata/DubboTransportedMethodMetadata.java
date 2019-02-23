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
package org.springframework.cloud.alibaba.dubbo.metadata;

import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * {@link MethodMetadata} annotated {@link DubboTransported @DubboTransported}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboTransportedMethodMetadata {

    private final DubboTransportedMetadata dubboTransportedMetadata;

    private final MethodMetadata methodMetadata;

    public DubboTransportedMethodMetadata(Method method) {
        this.methodMetadata = new MethodMetadata(method);
        this.dubboTransportedMetadata = new DubboTransportedMetadata();
    }

    public String getProtocol() {
        return dubboTransportedMetadata.getProtocol();
    }

    public void setProtocol(String protocol) {
        dubboTransportedMetadata.setProtocol(protocol);
    }

    public String getCluster() {
        return dubboTransportedMetadata.getCluster();
    }

    public void setCluster(String cluster) {
        dubboTransportedMetadata.setCluster(cluster);
    }

    public String getName() {
        return methodMetadata.getName();
    }

    public void setName(String name) {
        methodMetadata.setName(name);
    }

    public String getReturnType() {
        return methodMetadata.getReturnType();
    }

    public void setReturnType(String returnType) {
        methodMetadata.setReturnType(returnType);
    }

    public List<MethodParameterMetadata> getParams() {
        return methodMetadata.getParams();
    }

    public void setParams(List<MethodParameterMetadata> params) {
        methodMetadata.setParams(params);
    }

    public Method getMethod() {
        return methodMetadata.getMethod();
    }

    public DubboTransportedMetadata getDubboTransportedMetadata() {
        return dubboTransportedMetadata;
    }

    public MethodMetadata getMethodMetadata() {
        return methodMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DubboTransportedMethodMetadata)) return false;
        DubboTransportedMethodMetadata that = (DubboTransportedMethodMetadata) o;
        return Objects.equals(dubboTransportedMetadata, that.dubboTransportedMetadata) &&
                Objects.equals(methodMetadata, that.methodMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dubboTransportedMetadata, methodMetadata);
    }
}
