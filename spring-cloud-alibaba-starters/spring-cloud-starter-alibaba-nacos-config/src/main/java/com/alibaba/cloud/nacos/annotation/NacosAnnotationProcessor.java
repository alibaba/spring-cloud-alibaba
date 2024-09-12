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

package com.alibaba.cloud.nacos.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.client.config.common.GroupKey;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

public class NacosAnnotationProcessor implements BeanPostProcessor, PriorityOrdered, ApplicationContextAware {

	private NacosConfigManager nacosConfigManager;

	private ApplicationContext applicationContext;

	private final static Logger log = LoggerFactory
			.getLogger(NacosAnnotationProcessor.class);

	@Override
	public int getOrder() {
		return 0;
	}

	private Map<String, AtomicReference<String>> groupKeyCache = new ConcurrentHashMap<>();

	private String getGroupKeyContent(String dataId, String group) throws Exception {
		if (groupKeyCache.containsKey(GroupKey.getKey(dataId, group))) {
			return groupKeyCache.get(GroupKey.getKey(dataId, group)).get();
		}
		synchronized (this) {
			if (!groupKeyCache.containsKey(GroupKey.getKey(dataId, group))) {
				String content = nacosConfigManager.getConfigService().getConfig(dataId, group, 5000);
				groupKeyCache.put(GroupKey.getKey(dataId, group), new AtomicReference<>(content));

				log.info("[Nacos Config] Listening config for annotation: dataId={}, group={}", dataId,
						group);
				nacosConfigManager.getConfigService().addListener(dataId, group, new AbstractListener() {
					@Override
					public void receiveConfigInfo(String s) {
						groupKeyCache.get(GroupKey.getKey(dataId, group)).set(s);
					}

					@Override
					public String toString() {
						return String.format("sca nacos config annotation cache config listener");
					}
				});

			}

			return groupKeyCache.get(GroupKey.getKey(dataId, group)).get();
		}

	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		Class clazz = bean.getClass();
		for (Field field : getBeanFields(clazz)) {
			handleFiledAnnotation(bean, beanName, field);
		}
		for (Method method : getBeanMethods(clazz)) {
			handleMethodAnnotation(bean, beanName, method);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
	}

	private List<Field> getBeanFields(Class clazz) {
		List<Field> res = new ArrayList<>();
		ReflectionUtils.doWithFields(clazz, field -> res.add(field));
		return res;
	}

	private List<Method> getBeanMethods(Class clazz) {
		List<Method> res = new ArrayList<>();
		ReflectionUtils.doWithMethods(clazz, method -> res.add(method));
		return res;
	}

	private void handleFiledAnnotation(Object bean, String beanName, Field field) {
		NacosConfig annotation = AnnotationUtils.getAnnotation(field, NacosConfig.class);
		if (annotation != null) {
			handleFiledNacosConfigAnnotation(annotation, beanName, bean, field);
		}
	}

	private void handleMethodNacosConfigKeysChangeListener(NacosConfigKeysListener annotation, String beanName, Object bean,
			Method method) {
		String dataId = annotation.dataId();
		String group = annotation.group();
		try {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1 || !ConfigChangeEvent.class.isAssignableFrom(parameterTypes[0])) {
				throw new RuntimeException(
						"NacosConfigKeysChangeListener must be marked as a single parameter with ConfigChangeEvent");
			}

			ReflectionUtils.makeAccessible(method);

			// annotation on string.
			nacosConfigManager.getConfigService().addListener(dataId, group,
					new NacosPropertiesKeyListener(wrapArrayToSet(annotation.interestedKeys()),
							wrapArrayToSet(annotation.interestedKeyPrefixes())) {

						@Override
						public void configChanged(ConfigChangeEvent event) {
							ReflectionUtils.invokeMethod(method, bean, event);
						}

						@Override
						public String toString() {
							return String.format("sca nacos config listener on bean method %s", beanName + "@" + bean.hashCode() + "#" + method.getName());
						}
					});
		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> wrapArrayToSet(String... arrayKeys) {
		return new HashSet<>(Arrays.asList(arrayKeys));
	}

	private void handleMethodNacosConfigKeyListener(NacosConfigKeyListener annotation, String beanName, Object bean, Method method) {
		String dataId = annotation.dataId();
		String group = annotation.group();
		String key = annotation.key();
		try {
			Class<?>[] parameterTypes = method.getParameterTypes();

			if (parameterTypes.length != 1 && !ConfigChangeItem.class.isAssignableFrom(parameterTypes[0])) {
				throw new RuntimeException(
						"@NacosConfigKeyListener must be over a method with as a single parameter with ConfigChangeItem");
			}

			ReflectionUtils.makeAccessible(method);

			// annotation on string.
			nacosConfigManager.getConfigService()
					.addListener(dataId, group, new NacosPropertiesKeyListener(wrapArrayToSet(key)) {

						@Override
						public void configChanged(ConfigChangeEvent event) {
							Collection<ConfigChangeItem> changeItems = event.getChangeItems();
							List<ConfigChangeItem> collect = changeItems.stream().filter(a -> a.getKey().equals(key))
									.collect(Collectors.toList());
							if (!collect.isEmpty()) {
								ReflectionUtils.invokeMethod(method, bean, collect.get(0));
							}

						}

						@Override
						public String toString() {
							return String.format("sca nacos config listener on bean method %s", beanName + "@" + bean.hashCode() + "#" + method.getName());
						}
					});

		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private void handleMethodNacosConfigChangeListener(NacosConfigListener annotation, String beanName, Object bean, Method method) {
		String dataId = annotation.dataId();
		String group = annotation.group();
		try {
			Class<?>[] parameterTypes = method.getParameterTypes();

			if (parameterTypes.length != 1 && !ConfigChangeItem.class.isAssignableFrom(parameterTypes[0])) {
				throw new RuntimeException(
						"@NacosConfigListener without key must be over a method with as a single parameter with String,accept full content");
			}

			ReflectionUtils.makeAccessible(method);

			nacosConfigManager.getConfigService().addListener(dataId, group, new AbstractListener() {
				@Override
				public void receiveConfigInfo(String configInfo) {
					ReflectionUtils.invokeMethod(method, bean, configInfo);
				}

				@Override
				public String toString() {
					return String.format("sca nacos config listener on bean method %s", beanName + "@" + bean.hashCode() + "#" + method.getName());
				}
			});

		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private void handleFiledNacosConfigAnnotation(NacosConfig annotation, String beanName, Object bean, Field field) {
		String dataId = annotation.dataId();
		String group = annotation.group();
		String key = annotation.key();
		try {
			ReflectionUtils.makeAccessible(field);

			if (StringUtils.isBlank(key)) {
				handleFiledNacosConfigAnnotationWithoutKey(dataId, group, beanName, bean, field, annotation.defaultValue());
			}
			else {
				handleFiledNacosConfigAnnotationWithKey(dataId, group, key, beanName, bean, field, annotation.defaultValue());
			}

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void handleFiledNacosConfigAnnotationWithoutKey(String dataId, String group, String beanName, Object bean, Field field, String defaultValue) {

		try {
			ReflectionUtils.makeAccessible(field);

			String config = getGroupKeyContent(dataId, group);
			if (config == null) {
				config = defaultValue;
			}
			// annotation on string.
			if (String.class.isAssignableFrom(field.getType())) {
				ReflectionUtils.setField(field, bean, config);
				nacosConfigManager.getConfigService().addListener(dataId, group, new AbstractListener() {
					@Override
					public void receiveConfigInfo(String configInfo) {
						try {
							ReflectionUtils.setField(field, bean, configInfo);
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public String toString() {
						return String.format("sca nacos config listener on bean filed %s", beanName + "@" + bean.hashCode() + "#" + field.getName());
					}
				});
				return;
			}

			if (Properties.class.isAssignableFrom(field.getType())) {

				//properties and yaml config to properties.
				Properties properties = new Properties();
				try {
					if (StringUtils.isNotBlank(config)) {
						properties = PropertiesUtils.convertToProperties(config);
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				ReflectionUtils.setField(field, bean, properties);
				nacosConfigManager.getConfigService().addListener(dataId, group, new AbstractListener() {
					@Override
					public void receiveConfigInfo(String configInfo) {
						try {
							Properties properties = new Properties();
							if (StringUtils.isNotBlank(configInfo)) {
								properties = PropertiesUtils.convertToProperties(configInfo);
							}
							ReflectionUtils.setField(field, bean, properties);
						}
						catch (Throwable e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public String toString() {
						return String.format("sca nacos config properties listener on bean filed %s", beanName + "@" + bean.hashCode() + "#" + field.getName());
					}

				});
				return;
			}

			if (field.getType().isPrimitive()) {
				throw new RuntimeException("Annotation NacosConfig can only maked with String or Object filed.");
			}

			// annotation on object,use json to .
			if (StringUtils.isBlank(config)) {
				config = "{}";
			}
			//yaml and json to object
			Object o = ObjectUtils.convertToObject(config, field.getType());
			ReflectionUtils.setField(field, bean, o);
			nacosConfigManager.getConfigService().addListener(dataId, group, new AbstractListener() {
				@Override
				public void receiveConfigInfo(String configInfo) {
					try {
						if (StringUtils.isBlank(configInfo)) {
							configInfo = "{}";
						}
						Object o = ObjectUtils.convertToObject(configInfo, field.getType());
						ReflectionUtils.setField(field, bean, o);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public String toString() {
					return String.format("sca nacos config object listener on bean filed %s", beanName + "@" + bean.hashCode() + "#" + field.getName());
				}

			});

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void handleFiledNacosConfigAnnotationWithKey(String dataId, String group, String key, String beanName, Object bean,
			Field field, String defaultValue) {
		try {
			ReflectionUtils.makeAccessible(field);

			String config = getDestContent(getGroupKeyContent(dataId, group), key);
			if (config == null) {
				config = defaultValue;
			}
			// annotation on string.
			if (String.class.isAssignableFrom(field.getType())) {
				ReflectionUtils.setField(field, bean, config);
				nacosConfigManager.getConfigService()
						.addListener(dataId, group, new NacosPropertiesKeyListener(wrapArrayToSet(key)) {

							@Override
							public void configChanged(ConfigChangeEvent event) {
								ConfigChangeItem changeItem = event.getChangeItem(key);
								String newConfig = changeItem == null ? null : changeItem.getNewValue();
								ReflectionUtils.setField(field, bean, newConfig);
							}

							@Override
							public String toString() {
								return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, beanName + "@" + bean.hashCode() + "#" + field.getName());
							}

						});

				return;
			}

			if (Properties.class.isAssignableFrom(field.getType())) {

				//properties and yaml config to properties.
				Properties properties = new Properties();
				try {
					if (StringUtils.isNotBlank(config)) {
						properties = PropertiesUtils.convertToProperties(config);
					}
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
				ReflectionUtils.setField(field, bean, properties);
				nacosConfigManager.getConfigService()
						.addListener(dataId, group, new NacosPropertiesKeyListener(wrapArrayToSet(key)) {

							@Override
							public void configChanged(ConfigChangeEvent event) {
								try {
									ConfigChangeItem changeItem = event.getChangeItem(key);
									String newConfig = changeItem == null ? null : changeItem.getNewValue();
									Properties properties = new Properties();

									if (StringUtils.isNotBlank(newConfig)) {
										properties = PropertiesUtils.convertToProperties(newConfig);
									}
									ReflectionUtils.setField(field, bean, properties);
								}
								catch (Throwable e) {
									throw new RuntimeException(e);
								}
							}

							@Override
							public String toString() {
								return String.format("[spring cloud alibaba nacos config key listener for properties , key %s , target %s ] ", key, beanName + "@" + bean.hashCode() + "#" + field.getName());
							}

						});
				return;
			}

			if (field.getType().isPrimitive() && setPrimitiveFiled(field, bean, config)) {

				nacosConfigManager.getConfigService()
						.addListener(dataId, group, new NacosPropertiesKeyListener(wrapArrayToSet(key)) {

							@Override
							public void configChanged(ConfigChangeEvent event) {
								try {
									ConfigChangeItem changeItem = event.getChangeItem(key);
									String newConfig = changeItem == null ? null : changeItem.getNewValue();
									if (StringUtils.isNotBlank(newConfig)) {
										setPrimitiveFiled(field, bean, newConfig);
									}
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}

							@Override
							public String toString() {
								return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, beanName + "@" + bean.hashCode() + "#" + field.getName());
							}
						});
				return;
			}

			// annotation on object,use json to .
			if (StringUtils.isBlank(config)) {
				config = "{}";
			}
			//yaml and json to object
			Object o = ObjectUtils.convertToObject(config, field.getType());
			ReflectionUtils.setField(field, bean, o);
			nacosConfigManager.getConfigService()
					.addListener(dataId, group, new NacosPropertiesKeyListener(wrapArrayToSet(key)) {

						@Override
						public void configChanged(ConfigChangeEvent event) {
							try {
								ConfigChangeItem changeItem = event.getChangeItem(key);
								String newConfig = changeItem == null ? null : changeItem.getNewValue();
								if (StringUtils.isBlank(newConfig)) {
									newConfig = "{}";
								}
								Object o = ObjectUtils.convertToObject(newConfig, field.getType());
								ReflectionUtils.setField(field, bean, o);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
						}

						@Override
						public String toString() {
							return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, beanName + "#" + field.getName());
						}
					});

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean setPrimitiveFiled(Field filed, Object bean, String value) throws Exception {
		if (filed.getType() == int.class) {
			filed.setInt(bean, Integer.parseInt(value));
		}
		else if (filed.getType() == Integer.class) {
			ReflectionUtils.setField(filed, bean, Integer.parseInt(value));
		}
		else if (filed.getType() == long.class) {
			filed.setLong(bean, Long.parseLong(value));
		}
		else if (filed.getType() == Long.class) {
			ReflectionUtils.setField(filed, bean, Long.valueOf(value));
		}
		else if (filed.getType() == boolean.class) {
			filed.setBoolean(bean, Boolean.valueOf(value));
		}
		else if (filed.getType() == Long.class) {
			ReflectionUtils.setField(filed, bean, Long.valueOf(value));
		}
		else if (filed.getType() == double.class) {
			filed.setDouble(bean, Double.valueOf(value));
		}
		else if (filed.getType() == Double.class) {
			ReflectionUtils.setField(filed, bean, Double.valueOf(value));
		}
		else if (filed.getType() == float.class) {
			filed.setFloat(bean, Float.valueOf(value));
		}
		else if (filed.getType() == Float.class) {
			ReflectionUtils.setField(filed, bean, Float.valueOf(value));
		}
		else {
			return false;
		}
		return true;
	}

	private String getDestContent(String content, String key) throws Exception {
		if (StringUtils.isNotBlank(key)) {
			Properties properties = PropertiesUtils.convertToProperties(content);
			return properties.getProperty(key);
		}
		else {
			return content;
		}
	}

	private void handleMethodAnnotation(final Object bean, String beanName, final Method method) {
		NacosConfigKeysListener keysAnnotation = AnnotationUtils.getAnnotation(method, NacosConfigKeysListener.class);
		if (keysAnnotation != null) {
			handleMethodNacosConfigKeysChangeListener(keysAnnotation, beanName, bean, method);
			return;
		}
		NacosConfigListener configAnnotation = AnnotationUtils.getAnnotation(method, NacosConfigListener.class);
		if (configAnnotation != null) {
			handleMethodNacosConfigChangeListener(configAnnotation, beanName, bean, method);
			return;
		}
		NacosConfigKeyListener keyAnnotation = AnnotationUtils.getAnnotation(method, NacosConfigKeyListener.class);
		if (keyAnnotation != null) {
			handleMethodNacosConfigKeyListener(keyAnnotation, beanName, bean, method);
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		nacosConfigManager = this.applicationContext.getBean(NacosConfigManager.class);
	}
}
