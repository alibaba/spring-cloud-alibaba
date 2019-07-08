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
package org.springframework.cloud.alibaba.dubbo.actuate.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cloud.alibaba.dubbo.service.DubboMetadataService;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * Dubbo Rest Metadata {@link Endpoint}
 */
@Endpoint(id = "dubborestmetadata")
public class DubboRestMetadataEndpoint {

    @Autowired
    private DubboMetadataService dubboMetadataService;

    @ReadOperation(produces = APPLICATION_JSON_UTF8_VALUE)
    public String get() {
        return dubboMetadataService.getServiceRestMetadata();
    }
}
