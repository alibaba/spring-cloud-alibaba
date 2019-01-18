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
package org.springframework.cloud.alibaba.dubbo.util;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;

import javax.annotation.PostConstruct;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;

/**
 * TODO
 */
public class MetadataConfigUtils {

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    private ConfigService configService;

    @PostConstruct
    public void init() {
        this.configService = nacosConfigProperties.configServiceInstance();
    }

    /**
     * Get the data Id of service rest metadata
     * TODO JavaDoc
     */
    private static String getServiceRestMetadataDataId(String serviceName) {
        return serviceName + "-rest-metadata.json";
    }

    public void publishServiceRestMetadata(String serviceName, String restMetadataJSON)
            throws NacosException {
        String dataId = getServiceRestMetadataDataId(serviceName);
        configService.publishConfig(dataId, DEFAULT_GROUP, restMetadataJSON);
    }

    public String getServiceRestMetadata(String serviceName) throws NacosException {
        String dataId = getServiceRestMetadataDataId(serviceName);
        return configService.getConfig(dataId, DEFAULT_GROUP, 1000 * 3);
    }
}
