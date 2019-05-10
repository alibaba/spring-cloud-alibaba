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

package org.springframework.cloud.alibaba.nacos.config.server.environment;

import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.PassthruEnvironmentRepository;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;


/**
 * Nacos environment repository
 * <p>
 * create date: 2019-05-07
 *
 * @author cao.yong
 */

public class NacosEnvironmentRepository implements EnvironmentRepository, Ordered {

    /// private static final String NACOS_CONFIG_NAME_PREFIX = "NACOS:";

    private static final Logger logger = LoggerFactory.getLogger(NacosEnvironmentRepository.class);
    private static final String DEFAULT_PROFILE_ACTIVE = "default";
    private static final String DEFAULT_CONFIG_NAME_SEPARATOR = ",";
    private static final String DEFAULT_CONFIG_NAME_EN_DASH = "-";
    private static final Properties EMPTY_PROPERTIES = new Properties();
    private ConfigurableEnvironment environment;
    private NacosEnvironmentProperties properties;
    private int order = LOWEST_PRECEDENCE;

    public NacosEnvironmentRepository(ConfigurableEnvironment environment, NacosEnvironmentProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }


    @Override
    public Environment findOne(String application, String profile, String label) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(PropertyPlaceholderAutoConfiguration.class);
        ConfigurableEnvironment environment = getEnvironment(application, profile, label);
        builder.environment(environment);
        if (!logger.isDebugEnabled()) {
            // Make the mini-application startup less verbose
            builder.logStartupInfo(false);
        }
        Environment result = createEnvironment(new PassthruEnvironmentRepository(environment).findOne(application, profile, label));
        environment.getPropertySources().stream().forEach(propertySource -> environment.getPropertySources().remove(propertySource.getName()));
        return result;
    }

    private Environment createEnvironment(Environment env) {
        Environment result = new Environment(env.getName(), env.getProfiles(), env.getLabel(), null, null);
        for (PropertySource source : env.getPropertySources()) {
            String name = source.getName();
            if (this.environment.getPropertySources().contains(name)) {
                continue;
            }
            Properties properties = this.loadData(name);
            result.add(new PropertySource(name, properties));
        }
        return result;
    }

    private Properties loadData(String dataId) {
        String data = null;
        try {
            String fileExtension = this.properties.getFileExtension();
            String group = this.properties.getGroup();
            long timeout = this.properties.getTimeout();
            data = this.properties.configServiceInstance().getConfig(dataId, group, timeout);
            if (!StringUtils.isEmpty(data)) {
                String[] fileExtensions = new String[]{"properties", "yaml", "yml"};
                if (fileExtensions[0].equalsIgnoreCase(fileExtension)) {
                    Properties props = new Properties();
                    props.load(new StringReader(data));
                    return props;
                } else if (fileExtensions[1].equalsIgnoreCase(fileExtension)
                        || fileExtensions[2].equalsIgnoreCase(fileExtension)) {
                    YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
                    yamlFactory.setResources(new ByteArrayResource(data.getBytes()));
                    return yamlFactory.getObject();
                }
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Parse data from nacos error,dataId:{}, data:{}", dataId, data, e);
            }
        } catch (NacosException e) {
            logger.error("load data from nacos error,dataId:{},", dataId, e);
        }
        return EMPTY_PROPERTIES;
    }

    private ConfigurableEnvironment getEnvironment(String application, String profile, String label) {
        ConfigurableEnvironment environment = new StandardEnvironment();
        List<String> configNames = getConfigNames(application, profile, label);
        for (String name : configNames) {
            Map<String, Object> map = new HashMap<>(0);
            /// map.put("spring.profiles.active", profile);
            /// map.put("spring.main.web-application-type", "none");
            environment.getPropertySources().addLast(new MapPropertySource(name, map));
        }
        environment.setActiveProfiles(profile);
        return environment;
    }

    private List<String> getConfigNames(String configName, String profile, String label) {
        List<String> configNames = new ArrayList<>(0);
        String[] strNames = configName.split(DEFAULT_CONFIG_NAME_SEPARATOR);
        if (strNames.length == 1) {
            configNames.add(this.composeName(strNames[0], profile, label));
        } else {
            for (String name : strNames) {
                configNames.add(this.composeName(name, profile, label));
            }
        }
        return configNames;
    }

    private String composeName(String name, String profile, String label) {
        StringBuilder builder = new StringBuilder(name);
        if (null != profile && !DEFAULT_PROFILE_ACTIVE.equalsIgnoreCase(profile)) {
            builder.append(DEFAULT_CONFIG_NAME_EN_DASH).append(profile);
        }
        if (null != label) {
            builder.append(DEFAULT_CONFIG_NAME_EN_DASH).append(label);
        }
        builder.append(".").append(this.properties.getFileExtension());
        return builder.toString();
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
