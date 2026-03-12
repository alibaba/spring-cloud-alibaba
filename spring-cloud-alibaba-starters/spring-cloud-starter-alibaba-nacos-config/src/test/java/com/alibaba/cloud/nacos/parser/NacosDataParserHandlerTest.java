/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.nacos.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * NacosDataParserHandlerTest.
 *
 * @author songyanhui
 */
public class NacosDataParserHandlerTest {

    private NacosDataParserHandler nacosDataParserHandler;

    @BeforeEach
    void setUp() {
        nacosDataParserHandler = NacosDataParserHandler.getInstance();
    }

    @Test
    void parseNacosData_EmptyConfigValue_ReturnsEmptyList() throws IOException {
        String configName = "test";
        String configValue = "";
        String extension = "properties";

        List<PropertySource<?>> propertySources = nacosDataParserHandler.parseNacosData(configName, configValue, extension);

        assertTrue(propertySources.isEmpty());
    }

    @Test
    void parseNacosData_ValidPropertiesExtension_ReturnsPropertySource() throws IOException {
        String configName = "test";
        String configValue = "key=value";
        String extension = "properties";

        PropertySourceLoader mockLoader = mock(PropertySourceLoader.class);
        when(mockLoader.load(anyString(), any())).thenReturn(Collections.singletonList(mock(PropertySource.class)));

        // Mock the SpringFactoriesLoader to return our mocked loader
        try (MockedStatic<SpringFactoriesLoader> mocked = mockStatic(SpringFactoriesLoader.class)) {
            mocked.when(() -> SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader()))
                    .thenReturn(Collections.singletonList(mockLoader));

            List<PropertySource<?>> propertySources = nacosDataParserHandler.parseNacosData(configName, configValue, extension);

            assertFalse(propertySources.isEmpty());
        }
    }

    @Test
    void parseNacosData_ValidYamlExtension_ReturnsPropertySource() throws IOException {
        String configName = "test";
        String configValue = "key: value";
        String extension = "yaml";

        PropertySourceLoader mockLoader = mock(PropertySourceLoader.class);
        when(mockLoader.load(anyString(), any())).thenReturn(Collections.singletonList(mock(PropertySource.class)));

        // Mock the SpringFactoriesLoader to return our mocked loader
        try (MockedStatic<SpringFactoriesLoader> mocked = mockStatic(SpringFactoriesLoader.class)) {
            mocked.when(() -> SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader()))
                    .thenReturn(Collections.singletonList(mockLoader));

            List<PropertySource<?>> propertySources = nacosDataParserHandler.parseNacosData(configName, configValue, extension);

            assertFalse(propertySources.isEmpty());
        }
    }

    @Test
    void parseNacosData_EmptyExtension_DetectsFromConfigName() throws IOException {
        String configName = "test.properties";
        String configValue = "key=value";
        String extension = "";

        PropertySourceLoader mockLoader = mock(PropertySourceLoader.class);
        when(mockLoader.load(anyString(), any())).thenReturn(Collections.singletonList(mock(PropertySource.class)));

        // Mock the SpringFactoriesLoader to return our mocked loader
        try (MockedStatic<SpringFactoriesLoader> mocked = mockStatic(SpringFactoriesLoader.class)) {
            mocked.when(() -> SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader()))
                    .thenReturn(Collections.singletonList(mockLoader));

            List<PropertySource<?>> propertySources = nacosDataParserHandler.parseNacosData(configName, configValue, extension);

            assertFalse(propertySources.isEmpty());
        }
    }

    @Test
    void parseNacosData_InvalidExtension_NoMatchingLoader() throws IOException {
        String configName = "test";
        String configValue = "key=value";
        String extension = "invalid";

        List<PropertySource<?>> propertySources = nacosDataParserHandler.parseNacosData(configName, configValue, extension);

        assertTrue(propertySources.isEmpty());
    }
}
