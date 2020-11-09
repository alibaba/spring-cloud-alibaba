/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.sentinel.custom;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.datasource.config.AbstractDataSourceProperties;
import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.converter.XmlConverter;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Sentinel {@link ReadableDataSource} Handler Handle the configurations of
 * 'spring.cloud.sentinel.datasource'.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see SentinelProperties#datasource
 * @see JsonConverter
 * @see XmlConverter
 */
public class SentinelDataSourceHandler implements SmartInitializingSingleton {

	private static final Logger log = LoggerFactory
			.getLogger(SentinelDataSourceHandler.class);

	private List<String> dataTypeList = Arrays.asList("json", "xml");

	private final String DATA_TYPE_FIELD = "dataType";

	private final String CUSTOM_DATA_TYPE = "custom";

	private final String CONVERTER_CLASS_FIELD = "converterClass";

	private final DefaultListableBeanFactory beanFactory;

	private final SentinelProperties sentinelProperties;

	private final Environment env;

	public SentinelDataSourceHandler(DefaultListableBeanFactory beanFactory,
			SentinelProperties sentinelProperties, Environment env) {
		this.beanFactory = beanFactory;
		this.sentinelProperties = sentinelProperties;
		this.env = env;
	}

	@Override
	public void afterSingletonsInstantiated() {
		sentinelProperties.getDatasource()
				.forEach((dataSourceName, dataSourceProperties) -> {
					try {
						List<String> validFields = dataSourceProperties.getValidField();
						if (validFields.size() != 1) {
							log.error("[Sentinel Starter] DataSource " + dataSourceName
									+ " multi datasource active and won't loaded: "
									+ dataSourceProperties.getValidField());
							return;
						}
						AbstractDataSourceProperties abstractDataSourceProperties = dataSourceProperties
								.getValidDataSourceProperties();
						abstractDataSourceProperties.setEnv(env);
						abstractDataSourceProperties.preCheck(dataSourceName);
						registerBean(abstractDataSourceProperties, dataSourceName
								+ "-sentinel-" + validFields.get(0) + "-datasource");
					}
					catch (Exception e) {
						log.error("[Sentinel Starter] DataSource " + dataSourceName
								+ " build error: " + e.getMessage(), e);
					}
				});
	}

	private void registerBean(final AbstractDataSourceProperties dataSourceProperties,
			String dataSourceName) {

		Map<String, Object> propertyMap = Arrays
				.stream(dataSourceProperties.getClass().getDeclaredFields())
				.collect(HashMap::new, (m, v) -> {
					try {
						v.setAccessible(true);
						if(!v.isSynthetic()){
						    m.put(v.getName(), v.get(dataSourceProperties));
						}
					}
					catch (IllegalAccessException e) {
						log.error("[Sentinel Starter] DataSource " + dataSourceName
								+ " field: " + v.getName() + " invoke error");
						throw new RuntimeException(
								"[Sentinel Starter] DataSource " + dataSourceName
										+ " field: " + v.getName() + " invoke error",
								e);
					}
				}, HashMap::putAll);
		propertyMap.put(CONVERTER_CLASS_FIELD, dataSourceProperties.getConverterClass());
		propertyMap.put(DATA_TYPE_FIELD, dataSourceProperties.getDataType());

		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(dataSourceProperties.getFactoryBeanName());

		propertyMap.forEach((propertyName, propertyValue) -> {
			Field field = ReflectionUtils.findField(dataSourceProperties.getClass(),
					propertyName);
			if (null == field) {
				return;
			}
			if (DATA_TYPE_FIELD.equals(propertyName)) {
				String dataType = StringUtils.trimAllWhitespace(propertyValue.toString());
				if (CUSTOM_DATA_TYPE.equals(dataType)) {
					try {
						if (StringUtils
								.isEmpty(dataSourceProperties.getConverterClass())) {
							throw new RuntimeException("[Sentinel Starter] DataSource "
									+ dataSourceName
									+ "dataType is custom, please set converter-class "
									+ "property");
						}
						// construct custom Converter with 'converterClass'
						// configuration and register
						String customConvertBeanName = "sentinel-"
								+ dataSourceProperties.getConverterClass();
						if (!this.beanFactory.containsBean(customConvertBeanName)) {
							this.beanFactory.registerBeanDefinition(customConvertBeanName,
									BeanDefinitionBuilder
											.genericBeanDefinition(
													Class.forName(dataSourceProperties
															.getConverterClass()))
											.getBeanDefinition());
						}
						builder.addPropertyReference("converter", customConvertBeanName);
					}
					catch (ClassNotFoundException e) {
						log.error("[Sentinel Starter] DataSource " + dataSourceName
								+ " handle "
								+ dataSourceProperties.getClass().getSimpleName()
								+ " error, class name: "
								+ dataSourceProperties.getConverterClass());
						throw new RuntimeException("[Sentinel Starter] DataSource "
								+ dataSourceName + " handle "
								+ dataSourceProperties.getClass().getSimpleName()
								+ " error, class name: "
								+ dataSourceProperties.getConverterClass(), e);
					}
				}
				else {
					if (!dataTypeList.contains(
							StringUtils.trimAllWhitespace(propertyValue.toString()))) {
						throw new RuntimeException("[Sentinel Starter] DataSource "
								+ dataSourceName + " dataType: " + propertyValue
								+ " is not support now. please using these types: "
								+ dataTypeList.toString());
					}
					// converter type now support xml or json.
					// The bean name of these converters wrapped by
					// 'sentinel-{converterType}-{ruleType}-converter'
					builder.addPropertyReference("converter",
							"sentinel-" + propertyValue.toString() + "-"
									+ dataSourceProperties.getRuleType().getName()
									+ "-converter");
				}
			}
			else if (CONVERTER_CLASS_FIELD.equals(propertyName)) {
				return;
			}
			else {
				// wired properties
				Optional.ofNullable(propertyValue)
						.ifPresent(v -> builder.addPropertyValue(propertyName, v));
			}
		});

		this.beanFactory.registerBeanDefinition(dataSourceName,
				builder.getBeanDefinition());
		// init in Spring
		AbstractDataSource newDataSource = (AbstractDataSource) this.beanFactory
				.getBean(dataSourceName);

		// register property in RuleManager
		dataSourceProperties.postRegister(newDataSource);
	}

}
