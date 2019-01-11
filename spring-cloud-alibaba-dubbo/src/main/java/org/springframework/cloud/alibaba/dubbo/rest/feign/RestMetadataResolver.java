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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestTemplate;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The JSON resolver for {@link MethodMetadata}
 */
public class RestMetadataResolver {

    private static final String METHOD_PROPERTY_NAME = "method";
    private static final String URL_PROPERTY_NAME = "url";
    private static final String HEADERS_PROPERTY_NAME = "headers";

    private final ObjectMapper objectMapper;

    public RestMetadataResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String resolve(MethodMetadata methodMetadata) {
        String jsonContent = null;
        Map<String, Object> metadata = new LinkedHashMap<>();
        RequestTemplate requestTemplate = methodMetadata.template();
        Request request = requestTemplate.request();
        metadata.put(METHOD_PROPERTY_NAME, request.method());
        metadata.put(URL_PROPERTY_NAME, request.url());
        metadata.put(HEADERS_PROPERTY_NAME, request.headers());
        try {
            jsonContent = objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        return jsonContent;
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
}