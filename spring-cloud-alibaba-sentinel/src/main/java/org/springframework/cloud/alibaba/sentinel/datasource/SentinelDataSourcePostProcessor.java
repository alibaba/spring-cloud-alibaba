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

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cloud.alibaba.sentinel.SentinelConstants;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelDataSource;
import org.springframework.cloud.alibaba.sentinel.util.PropertySourcesUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.datasource.Converter;

/**
 * {@link SentinelDataSource @SentinelDataSource} Post Processor
 *
 * @author fangjian
 * @see com.alibaba.csp.sentinel.datasource.ReadableDataSource
 * @see SentinelDataSource
 */
public class SentinelDataSourcePostProcessor
		extends InstantiationAwareBeanPostProcessorAdapter
		implements MergedBeanDefinitionPostProcessor {

	private static final Logger logger = LoggerFactory
			.getLogger(SentinelDataSourcePostProcessor.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ConfigurableEnvironment environment;

	private final Map<String, List<SentinelDataSourceField>> dataSourceFieldCache = new ConcurrentHashMap<>(
			64);

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition,
			Class<?> beanType, String beanName) {
		// find all fields using by @SentinelDataSource annotation
		ReflectionUtils.doWithFields(beanType, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field)
					throws IllegalArgumentException, IllegalAccessException {
				SentinelDataSource annotation = getAnnotation(field,
						SentinelDataSource.class);
				if (annotation != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (logger.isWarnEnabled()) {
							logger.warn(
									"@SentinelDataSource annotation is not supported on static fields: "
											+ field);
						}
						return;
					}
					if (dataSourceFieldCache.containsKey(beanName)) {
						dataSourceFieldCache.get(beanName)
								.add(new SentinelDataSourceField(annotation, field));
					}
					else {
						List<SentinelDataSourceField> list = new ArrayList<>();
						list.add(new SentinelDataSourceField(annotation, field));
						dataSourceFieldCache.put(beanName, list);
					}
				}
			}
		});
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs,
			PropertyDescriptor[] pds, Object bean, String beanName)
			throws BeanCreationException {
		if (dataSourceFieldCache.containsKey(beanName)) {
			List<SentinelDataSourceField> sentinelDataSourceFields = dataSourceFieldCache
					.get(beanName);
			sentinelDataSourceFields.forEach(sentinelDataSourceField -> {
				try {
					// construct DataSource field annotated by @SentinelDataSource
					Field field = sentinelDataSourceField.getField();
					ReflectionUtils.makeAccessible(field);
					String dataSourceBeanName = constructDataSource(
							sentinelDataSourceField.getSentinelDataSource());
					field.set(bean, applicationContext.getBean(dataSourceBeanName));
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		return pvs;
	}

	private String constructDataSource(SentinelDataSource annotation) {
		String prefix = annotation.value();
		if (StringUtils.isEmpty(prefix)) {
			prefix = SentinelConstants.PROPERTY_DATASOURCE_PREFIX;
		}
		Map<String, Object> propertyMap = PropertySourcesUtils
				.getSubProperties(environment.getPropertySources(), prefix);
		String alias = propertyMap.get("type").toString();
		Class dataSourceClass = DataSourceLoader.loadClass(alias);

		String beanName = StringUtils.isEmpty(annotation.name())
				? StringUtils.uncapitalize(dataSourceClass.getSimpleName()) + "_" + prefix
				: annotation.name();
		if (applicationContext.containsBean(beanName)) {
			return beanName;
		}

		Class targetClass = null;
		// if alias exists in SentinelDataSourceRegistry, wired properties into
		// FactoryBean
		if (SentinelDataSourceRegistry.checkFactoryBean(alias)) {
			targetClass = SentinelDataSourceRegistry.getFactoryBean(alias);
		}
		else {
			// if alias not exists in SentinelDataSourceRegistry, wired properties into
			// raw class
			targetClass = dataSourceClass;
		}

		registerDataSource(beanName, targetClass, propertyMap);

		return beanName;
	}

	private void registerDataSource(String beanName, Class targetClass,
			Map<String, Object> propertyMap) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(targetClass);
		for (String propertyName : propertyMap.keySet()) {
			Field field = ReflectionUtils.findField(targetClass, propertyName);
			if (field != null) {
				if (field.getType().isAssignableFrom(Converter.class)) {
					// Converter get from ApplicationContext
					builder.addPropertyReference(propertyName,
							propertyMap.get(propertyName).toString());
				}
				else {
					// wired properties
					builder.addPropertyValue(propertyName, propertyMap.get(propertyName));
				}
			}
		}

		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext
				.getAutowireCapableBeanFactory();
		beanFactory.registerBeanDefinition(beanName, builder.getBeanDefinition());
	}

	class SentinelDataSourceField {
		private SentinelDataSource sentinelDataSource;
		private Field field;

		public SentinelDataSourceField(SentinelDataSource sentinelDataSource,
				Field field) {
			this.sentinelDataSource = sentinelDataSource;
			this.field = field;
		}

		public SentinelDataSource getSentinelDataSource() {
			return sentinelDataSource;
		}

		public void setSentinelDataSource(SentinelDataSource sentinelDataSource) {
			this.sentinelDataSource = sentinelDataSource;
		}

		public Field getField() {
			return field;
		}

		public void setField(Field field) {
			this.field = field;
		}
	}

}
