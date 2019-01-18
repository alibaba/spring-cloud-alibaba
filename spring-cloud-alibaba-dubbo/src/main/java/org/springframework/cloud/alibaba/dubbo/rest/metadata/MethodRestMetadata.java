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
package org.springframework.cloud.alibaba.dubbo.rest.metadata;

import java.util.Collection;
import java.util.Map;

/**
 * TODO
 */
public class MethodRestMetadata {

    private String configKey;

    private String method;

    private String url;

    private Map<String, Collection<String>> headers;

    private Map<Integer, Collection<String>> indexToName;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Collection<String>> headers) {
        this.headers = headers;
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

        MethodRestMetadata that = (MethodRestMetadata) o;

        if (configKey != null ? !configKey.equals(that.configKey) : that.configKey != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (headers != null ? !headers.equals(that.headers) : that.headers != null) return false;
        return indexToName != null ? indexToName.equals(that.indexToName) : that.indexToName == null;
    }

    @Override
    public int hashCode() {
        int result = configKey != null ? configKey.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        result = 31 * result + (indexToName != null ? indexToName.hashCode() : 0);
        return result;
    }
}
