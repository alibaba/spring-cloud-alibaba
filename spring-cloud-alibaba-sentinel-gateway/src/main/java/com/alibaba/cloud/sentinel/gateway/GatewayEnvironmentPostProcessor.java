package com.alibaba.cloud.sentinel.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhh on 2019/8/17.
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
