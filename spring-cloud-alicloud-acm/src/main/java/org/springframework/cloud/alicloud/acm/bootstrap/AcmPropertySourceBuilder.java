/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.acm.bootstrap;

import com.alibaba.edas.acm.ConfigService;
import com.alibaba.edas.acm.exception.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

import java.io.StringReader;
import java.util.*;

/**
 * @author juven.xuxb
 * @author xiaolongzuo
 */
class AcmPropertySourceBuilder {

    private Logger logger = LoggerFactory.getLogger(AcmPropertySourceBuilder.class);

    /**
     * 传入 ACM 的 DataId 和 groupID，获取到解析后的 AcmProperty 对象
     *
     * @param dataId
     * @param diamondGroup
     * @param groupLevel
     * @return
     */
    AcmPropertySource build(String dataId, String diamondGroup, boolean groupLevel) {
        Properties properties = loadDiamondData(dataId, diamondGroup);
        if (properties == null) {
            return null;
        }
        return new AcmPropertySource(dataId, toMap(properties), new Date(), groupLevel);
    }

    private Properties loadDiamondData(String dataId, String diamondGroup) {
        try {
            String data = ConfigService.getConfig(dataId, diamondGroup, 3000L);
            if (StringUtils.isEmpty(data)) {
                return null;
            }
            if (dataId.endsWith(".properties")) {
                Properties properties = new Properties();
                logger.info(String.format("Loading acm data, dataId: '%s', group: '%s'",
                    dataId, diamondGroup));
                properties.load(new StringReader(data));
                return properties;
            } else if (dataId.endsWith(".yaml") || dataId.endsWith(".yml")) {
                YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
                yamlFactory.setResources(new ByteArrayResource(data.getBytes()));
                return yamlFactory.getObject();
            }
        } catch (Exception e) {
            if (e instanceof ConfigException) {
                logger.error("DIAMOND-100500:" + dataId + ", " + e.toString(), e);
            } else {
                logger.error("DIAMOND-100500:" + dataId, e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Properties properties) {
        Map<String, Object> result = new HashMap<>();
        Enumeration<String> keys = (Enumeration<String>)properties.propertyNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Object value = properties.getProperty(key);
            if (value != null) {
                result.put(key, ((String)value).trim());
            } else {
                result.put(key, null);
            }
        }
        return result;
    }
}
