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

import feign.RequestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Request Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class RequestMetadata {

    private String method;

    private String url;

    private Map<String, Collection<String>> queries;

    private Map<String, Collection<String>> headers;

    public RequestMetadata() {
    }

    public RequestMetadata(RequestTemplate requestTemplate) {
        this.method = requestTemplate.method();
        this.url = requestTemplate.url();
        this.queries = requestTemplate.queries();
        this.headers = requestTemplate.headers();
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

    public Map<String, Collection<String>> getQueries() {
        return queries;
    }

    public void setQueries(Map<String, Collection<String>> queries) {
        this.queries = queries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMetadata that = (RequestMetadata) o;
        return Objects.equals(method, that.method) &&
                Objects.equals(url, that.url) &&
                Objects.equals(queries, that.queries) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, url, queries, headers);
    }
}
