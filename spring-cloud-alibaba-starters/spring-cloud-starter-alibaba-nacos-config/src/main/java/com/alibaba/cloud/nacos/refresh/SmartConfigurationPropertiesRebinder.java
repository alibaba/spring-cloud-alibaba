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

package com.alibaba.cloud.nacos.refresh;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.properties.ConfigurationPropertiesBeans;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import static com.alibaba.cloud.nacos.refresh.RefreshBehavior.ALL_BEANS;

/**
 * Extend {@link ConfigurationPropertiesRebinder}.
 * <p>
 * Spring team doesn't seem to support single {@link ConfigurationPropertiesBean} refresh.
 * <p>
 * SmartConfigurationPropertiesRebinder can refresh specific
 * {@link ConfigurationPropertiesBean} base on the change keys.
 * <p>
 * <strong> NOTE: We still use Spring's default behavior (full refresh) as default
 * behavior, This feature can be considered an advanced feature, it may not be as stable
 * as the default behavior. </strong>
 *
 * @author freeman
 * @since 2021.0.1.1
 */
public class SmartConfigurationPropertiesRebinder
		extends ConfigurationPropertiesRebinder {

	private Map<String, ConfigurationPropertiesBean> beanMap;

	private ApplicationContext applicationContext;

	private RefreshBehavior refreshBehavior;

	public SmartConfigurationPropertiesRebinder(ConfigurationPropertiesBeans beans) {
		super(beans);
		fillBeanMap(beans);
	}

	@SuppressWarnings("unchecked")
	private void fillBeanMap(ConfigurationPropertiesBeans beans) {
		this.beanMap = new HashMap<>();
		Field field = ReflectionUtils.findField(beans.getClass(), "beans");
		if (field != null) {
			field.setAccessible(true);
			this.beanMap.putAll((Map<String, ConfigurationPropertiesBean>) Optional
					.ofNullable(ReflectionUtils.getField(field, beans))
					.orElse(Collections.emptyMap()));
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		super.setApplicationContext(applicationContext);
		this.applicationContext = applicationContext;
		this.refreshBehavior = this.applicationContext.getEnvironment().getProperty(
				"spring.cloud.nacos.config.refresh-behavior", RefreshBehavior.class,
				ALL_BEANS);
	}

	@Override
	public void onApplicationEvent(EnvironmentChangeEvent event) {
		if (this.applicationContext.equals(event.getSource())
				// Backwards compatible
				|| event.getKeys().equals(event.getSource())) {
			switch (refreshBehavior) {
			case SPECIFIC_BEAN -> rebindSpecificBean(event);
			default -> rebind();
			}
		}
	}

	private void rebindSpecificBean(EnvironmentChangeEvent event) {
		Set<String> refreshedSet = new HashSet<>();
		beanMap.forEach((name, bean) -> event.getKeys().forEach(changeKey -> {
			String prefix = AnnotationUtils.getValue(bean.getAnnotation()).toString();
			// prevent multiple refresh one ConfigurationPropertiesBean.
			if (changeKey.startsWith(prefix) && refreshedSet.add(name)) {
				rebind(name);
			}
		}));
	}

}
