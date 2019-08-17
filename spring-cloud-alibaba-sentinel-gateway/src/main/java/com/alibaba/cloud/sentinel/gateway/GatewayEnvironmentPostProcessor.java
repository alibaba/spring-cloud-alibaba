package com.alibaba.cloud.sentinel.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * Created by zhh on 2019/8/17.
 */
public class GatewayEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private final static String SENTINEL_FILTER_ENABLED = "spring.cloud.sentinel.filter.enabled";
    private final static String FILTERS_DISABLED = "filters_disabled";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication springApplication) {
        Properties properties = new Properties();
        properties.setProperty(SENTINEL_FILTER_ENABLED, "false");
        PropertiesPropertySource propertySource = new PropertiesPropertySource(FILTERS_DISABLED, properties);
        environment.getPropertySources().addLast(propertySource);
    }
}
