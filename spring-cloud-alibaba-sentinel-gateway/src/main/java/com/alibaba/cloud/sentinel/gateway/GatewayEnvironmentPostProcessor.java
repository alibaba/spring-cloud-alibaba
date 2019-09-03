/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.sentinel.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuhonghan
 */
public class GatewayEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private final static String SENTINEL_FILTER_ENABLED = "spring.cloud.sentinel.filter.enabled";
    private final static String PROPERTY_SOURCE_NAME = "defaultProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication springApplication) {
        addDefaultPropertySource(environment);
    }

    private void addDefaultPropertySource(ConfigurableEnvironment environment) {

        Map<String, Object> map = new HashMap<String, Object>();

        configureDefaultProperties(map);

        addOrReplace(environment.getPropertySources(), map);
    }

    private void configureDefaultProperties(Map<String, Object> source) {
        // Required Properties
        source.put(SENTINEL_FILTER_ENABLED, "false");
    }

    private void addOrReplace(MutablePropertySources propertySources,
                              Map<String, Object> map) {
        MapPropertySource target = null;
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
            if (source instanceof MapPropertySource) {
                target = (MapPropertySource) source;
                for (String key : map.keySet()) {
                    if (!target.containsProperty(key)) {
                        target.getSource().put(key, map.get(key));
                    }
                }
            }
        }
        if (target == null) {
            target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
        }
        if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
            propertySources.addLast(target);
        }
    }


}
