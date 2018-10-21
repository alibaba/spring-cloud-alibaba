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

package org.springframework.cloud.alibaba.sentinel.datasource;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;

/**
 * {@link ReadableDataSource} Loader
 *
 * @author fangjian
 */
public class DataSourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLoader.class);

	private final static String PROPERTIES_RESOURCE_LOCATION = "META-INF/sentinel-datasource.properties";

	private final static String ALL_PROPERTIES_RESOURCES_LOCATION = CLASSPATH_ALL_URL_PREFIX
			+ PROPERTIES_RESOURCE_LOCATION;

	private final static ConcurrentMap<String, Class<? extends ReadableDataSource>> dataSourceClassesCache = new ConcurrentHashMap<String, Class<? extends ReadableDataSource>>(
			4);

	static void loadAllDataSourceClassesCache() {
		Map<String, Class<? extends ReadableDataSource>> dataSourceClassesMap = loadAllDataSourceClassesCache(
				ALL_PROPERTIES_RESOURCES_LOCATION);

		dataSourceClassesCache.putAll(dataSourceClassesMap);
	}

	static Map<String, Class<? extends ReadableDataSource>> loadAllDataSourceClassesCache(
			String resourcesLocation) {

		Map<String, Class<? extends ReadableDataSource>> dataSourcesMap = new HashMap<String, Class<? extends ReadableDataSource>>(
				4);

		ClassLoader classLoader = DataSourceLoader.class.getClassLoader();

		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		try {

			Resource[] resources = resolver.getResources(resourcesLocation);

			for (Resource resource : resources) {
				if (resource.exists()) {
					Properties properties = PropertiesLoaderUtils
							.loadProperties(resource);
					for (Map.Entry<Object, Object> entry : properties.entrySet()) {

						String type = (String) entry.getKey();
						String className = (String) entry.getValue();

						if (!ClassUtils.isPresent(className, classLoader)) {
							if (logger.isDebugEnabled()) {
								logger.debug(
										"Sentinel DataSource implementation [ type : "
												+ type + ": , class : " + className
												+ " , url : " + resource.getURL()
												+ "] was not present in current classpath , "
												+ "thus loading will be ignored , please add dependency if required !");
							}
							continue;
						}

						Assert.isTrue(!dataSourcesMap.containsKey(type),
								"The duplicated type[" + type
										+ "] of SentinelDataSource were found in "
										+ "resource [" + resource.getURL() + "]");

						Class<?> dataSourceClass = ClassUtils.resolveClassName(className,
								classLoader);
						Assert.isAssignable(ReadableDataSource.class, dataSourceClass);

						dataSourcesMap.put(type,
								(Class<? extends ReadableDataSource>) dataSourceClass);

						if (logger.isDebugEnabled()) {
							logger.debug("Sentinel DataSource implementation [ type : "
									+ type + ": , class : " + className
									+ "] was loaded.");
						}
					}
				}
			}

		}
		catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}

		return dataSourcesMap;
	}

	public static Class<? extends ReadableDataSource> loadClass(String type)
			throws IllegalArgumentException {

		Class<? extends ReadableDataSource> dataSourceClass = dataSourceClassesCache.get(type);

		if (dataSourceClass == null) {
			if (dataSourceClassesCache.isEmpty()) {
				loadAllDataSourceClassesCache();
				dataSourceClass = dataSourceClassesCache.get(type);
			}
		}

		if (dataSourceClass == null) {
			throw new IllegalArgumentException(
					"Sentinel DataSource implementation [ type : " + type
							+ " ] can't be found!");
		}

		return dataSourceClass;

	}

}
