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
package org.springframework.cloud.alibaba.nacos.config.server.environment;

import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.service.PersistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import static com.alibaba.nacos.config.server.constant.Constants.DEFAULT_GROUP;

/**
 * Nacos {@link EnvironmentRepository}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.0
 */
public class NacosEnvironmentRepository implements EnvironmentRepository {

    @Autowired
    private PersistService persistService;

    @Override
    public Environment findOne(String application, String profile, String label) {

        String dataId = application + "-" + profile + ".properties";

        ConfigInfo configInfo = persistService.findConfigInfo(dataId, DEFAULT_GROUP, label);

        return createEnvironment(configInfo, application, profile);
    }

    private Environment createEnvironment(ConfigInfo configInfo, String application, String profile) {

        Environment environment = new Environment(application, profile);

        Properties properties = createProperties(configInfo);

        String propertySourceName = String.format("Nacos[application : %s , profile : %s]", application, profile);

        PropertySource propertySource = new PropertySource(propertySourceName, properties);

        environment.add(propertySource);

        return environment;
    }

    private Properties createProperties(ConfigInfo configInfo) {
        Properties properties = new Properties();
        String content = configInfo == null ? null : configInfo.getContent();
        if (StringUtils.hasText(content)) {
            try {
                properties.load(new StringReader(content));
            } catch (IOException e) {
                throw new IllegalStateException("The format of content is a properties");
            }
        }
        return properties;
    }

    private static String[] of(String... values) {
        return values;
    }
}
