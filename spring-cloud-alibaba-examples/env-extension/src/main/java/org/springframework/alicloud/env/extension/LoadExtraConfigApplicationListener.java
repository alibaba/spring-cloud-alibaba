/*
 * Copyright (C) 2019 the original author or authors.
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
package org.springframework.alicloud.env.extension;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author pbting
 * @date 2019-01-09 9:00 PM
 */
public class LoadExtraConfigApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        SpringApplication springApplication = event.getSpringApplication();
        Class clazz = springApplication.getMainApplicationClass();
        if (!clazz.isAnnotationPresent(ImportExtraConfig.class)) {
            return;
        }
        ImportExtraConfig annotation = (ImportExtraConfig) clazz
                .getAnnotation(ImportExtraConfig.class);

        String[] extraConfig = annotation.name();

        if (extraConfig == null || extraConfig.length == 0) {
            return;
        }

        for (String config : extraConfig) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(config));
                event.getEnvironment().getPropertySources()
                        .addFirst(new PropertiesPropertySource(config, properties));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}