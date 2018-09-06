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

package org.springframework.cloud.alibaba.sentinel.util;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * {@link PropertySources} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class PropertySourcesUtils {

    /**
     * Get Sub {@link Properties}
     *
     * @param propertySources {@link PropertySource} Iterable
     * @param prefix          the prefix of property name
     * @return Map
     * @see Properties
     */
    public static Map<String, Object> getSubProperties(Iterable<PropertySource<?>> propertySources, String prefix) {

        Map<String, Object> subProperties = new LinkedHashMap<String, Object>();

        String normalizedPrefix = normalizePrefix(prefix);

        for (PropertySource<?> source : propertySources) {
            if (source instanceof EnumerablePropertySource) {
                for (String name : ((EnumerablePropertySource<?>) source).getPropertyNames()) {
                    if (!subProperties.containsKey(name) && name.startsWith(normalizedPrefix)) {
                        String subName = name.substring(normalizedPrefix.length());
                        if (!subProperties.containsKey(subName)) { // take first one
                            Object value = source.getProperty(name);
                            subProperties.put(subName, value);
                        }
                    }
                }
            }
        }

        return subProperties;

    }

    /**
     * Normalize the prefix
     *
     * @param prefix the prefix
     * @return the prefix
     */
    public static String normalizePrefix(String prefix) {
        return prefix.endsWith(".") ? prefix : prefix + ".";
    }
}
