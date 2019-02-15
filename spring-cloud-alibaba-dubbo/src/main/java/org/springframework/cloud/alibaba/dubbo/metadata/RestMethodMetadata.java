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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Method Request Metadata
 */
public class RestMethodMetadata {

    private MethodMetadata method;

    private RequestMetadata request;

    private Map<Integer, Collection<String>> indexToName;

    public MethodMetadata getMethod() {
        return method;
    }

    public void setMethod(MethodMetadata method) {
        this.method = method;
    }

    public RequestMetadata getRequest() {
        return request;
    }

    public void setRequest(RequestMetadata request) {
        this.request = request;
    }

    public Map<Integer, Collection<String>> getIndexToName() {
        return indexToName;
    }

    public void setIndexToName(Map<Integer, Collection<String>> indexToName) {
        this.indexToName = indexToName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestMethodMetadata that = (RestMethodMetadata) o;
        return Objects.equals(method, that.method) &&
                Objects.equals(request, that.request) &&
                Objects.equals(indexToName, that.indexToName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, request, indexToName);
    }
}
