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
package org.springframework.cloud.alibaba.dubbo.http.matcher;

import org.springframework.cloud.alibaba.dubbo.metadata.RequestMetadata;

import static org.springframework.cloud.alibaba.dubbo.http.util.HttpUtils.toNameAndValues;

/**
 * {@link RequestMetadata} {@link HttpRequestMatcher} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class RequestMetadataMatcher extends CompositeHttpRequestMatcher {

    public RequestMetadataMatcher(RequestMetadata metadata) {
        super(
                // method
                new HttpRequestMethodsMatcher(metadata.getMethod()),
                // url
                new HttpRequestPathMatcher(metadata.getPath()),
                // params
                new HttpRequestParamsMatcher(toNameAndValues(metadata.getParams())),
                // headers
                new HttpRequestHeadersMatcher(toNameAndValues(metadata.getHeaders())),
                // consumes
                new HttpRequestConsumersMatcher(metadata.getConsumes().toArray(new String[0])),
                // produces
                new HttpRequestProducesMatcher(metadata.getProduces().toArray(new String[0]))
        );
    }
}
