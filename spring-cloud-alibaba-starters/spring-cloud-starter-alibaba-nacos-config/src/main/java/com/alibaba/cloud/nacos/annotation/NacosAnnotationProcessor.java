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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.client.config.common.GroupKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.MethodMetadata;
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

	private Map<String, TargetRefreshable> targetListenerMap = new ConcurrentHashMap<>();
	private Map<String, AtomicReference<String>> groupKeyCache = new ConcurrentHashMap<>();

	private String getGroupKeyContent(String dataId, String group) throws Exception {
		if (groupKeyCache.containsKey(GroupKey.getKey(dataId, group))) {
			return groupKeyCache.get(GroupKey.getKey(dataId, group)).get();
		}
		synchronized (this) {
			if (!groupKeyCache.containsKey(GroupKey.getKey(dataId, group))) {
				String content = getNacosConfigManager().getConfigService().getConfig(dataId, group, 5000);
				groupKeyCache.put(GroupKey.getKey(dataId, group), new AtomicReference<>(content));

				log.info("[Nacos Config] Listening config for annotation: dataId={}, group={}", dataId,
						group);
				getNacosConfigManager().getConfigService().addListener(dataId, group, new AbstractListener() {
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
		BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
		Class clazz = bean.getClass();

		NacosConfig annotationBean = AnnotationUtils.findAnnotation(clazz, NacosConfig.class);
		if (annotationBean != null) {
			handleBeanNacosConfigAnnotation(annotationBean.dataId(), annotationBean.group(), annotationBean.key(), beanName, bean, annotationBean.defaultValue());
			return bean;
		}

		for (Field field : getBeanFields(clazz)) {
			handleFiledAnnotation(bean, beanName, field);
		}
		for (Method method : getBeanMethods(clazz)) {
			handleMethodAnnotation(bean, beanName, method);
		}
		return bean;
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

	private void handleBeanNacosConfigAnnotation(String dataId, String group, String key, String beanName, Object bean,
			String defaultValue) {
		try {
			String config = getDestContent(getGroupKeyContent(dataId, group), key);
			if (!org.springframework.util.StringUtils.hasText(config)) {
				config = defaultValue;
			}
			//Init bean properties.
			if (org.springframework.util.StringUtils.hasText(config)) {
				Object targetObject = convertContentToTargetType(config, bean.getClass());
				//yaml and json to object
				BeanUtils.copyProperties(targetObject, bean, getNullPropertyNames(targetObject));
			}
			String refreshTargetKey = beanName + "#instance#";
			TargetRefreshable currentTarget = targetListenerMap.get(refreshTargetKey);
			if (currentTarget != null) {
				log.info("[Nacos Config] reset {} listener from  {} to {} ", refreshTargetKey,
						currentTarget.getTarget(), bean);
				targetListenerMap.get(refreshTargetKey).setTarget(bean);
				return;
			}
			log.info("[Nacos Config] register {} listener on {} ", refreshTargetKey,
					bean);
			TargetRefreshable listener = null;
			if (org.springframework.util.StringUtils.hasText(key)) {
				listener = new NacosPropertiesKeyListener(bean, wrapArrayToSet(key)) {
					@Override
					public void configChanged(ConfigChangeEvent event) {
						try {
							ConfigChangeItem changeItem = event.getChangeItem(key);
							String newConfig = changeItem == null ? null : changeItem.getNewValue();
							if (!org.springframework.util.StringUtils.hasText(newConfig)) {
								newConfig = defaultValue;
							}
							if (org.springframework.util.StringUtils.hasText(newConfig)) {
								Object targetObject = convertContentToTargetType(newConfig, getTarget().getClass());
								//yaml and json to object
								BeanUtils.copyProperties(targetObject, getTarget(), getNullPropertyNames(targetObject));
							}
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config instance key listener , key %s , target %s ] ", key, bean);
					}
				};
			}
			else {
				listener = new NacosConfigRefreshableListener(bean) {
					@Override
					public void receiveConfigInfo(String configInfo) {
						if (!org.springframework.util.StringUtils.hasText(configInfo)) {
							configInfo = defaultValue;
						}
						if (org.springframework.util.StringUtils.hasText(configInfo)) {
							Object targetObject = convertContentToTargetType(configInfo, bean.getClass());
							//yaml and json to object
							BeanUtils.copyProperties(targetObject, getTarget());
						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config instance  listener , key %s , target %s ] ", key, bean);
					}
				};
			}
			getNacosConfigManager().getConfigService()
					.addListener(dataId, group, listener);
			targetListenerMap.put(refreshTargetKey, listener);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
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

			String refreshTargetKey = beanName + "#method#" + methodSignature(method);
			TargetRefreshable currentTarget = targetListenerMap.get(refreshTargetKey);
			if (currentTarget != null) {
				log.info("[Nacos Config] reset {} listener from  {} to {} ", refreshTargetKey,
						currentTarget.getTarget(), bean);
				targetListenerMap.get(refreshTargetKey).setTarget(bean);
				return;
			}

			log.info("[Nacos Config] register {} listener on {} ", refreshTargetKey,
					bean);
			// annotation on string.
			NacosPropertiesKeyListener nacosPropertiesKeyListener = new NacosPropertiesKeyListener(bean, wrapArrayToSet(annotation.interestedKeys()),
					wrapArrayToSet(annotation.interestedKeyPrefixes())) {

				@Override
				public void configChanged(ConfigChangeEvent event) {
					ReflectionUtils.invokeMethod(method, this.getTarget(), event);
				}

				@Override
				public String toString() {
					return String.format("sca nacos config listener on bean method %s", bean + "#" + methodSignature(method));
				}
			};
			nacosPropertiesKeyListener.setLastContent(getGroupKeyContent(dataId, group));
			getNacosConfigManager().getConfigService().addListener(dataId, group,
					nacosPropertiesKeyListener);
			targetListenerMap.put(refreshTargetKey, nacosPropertiesKeyListener);
		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> wrapArrayToSet(String... arrayKeys) {
		return new HashSet<>(Arrays.asList(arrayKeys));
	}

	private String methodSignature(Method method) {
		StringBuilder signature = new StringBuilder(method.getName() + "(");
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			signature.append(parameterTypes[i].getSimpleName());
			if (i < parameterTypes.length - 1) {
				signature.append(", ");
			}
		}

		signature.append(")");
		return signature.toString();
	}

	private void handleMethodNacosConfigListener(NacosConfigListener annotation, String beanName, Object bean, Method method) {
		String dataId = annotation.dataId();
		String group = annotation.group();
		String key = annotation.key();
		try {
			Type[] parameterTypes = method.getGenericParameterTypes();
			if (parameterTypes.length != 1) {
				throw new RuntimeException(
						"@NacosConfigListener  must be over a method with  a single parameter");
			}

			String configInfo = getGroupKeyContent(dataId, group);
			String refreshTargetKey = beanName + "#method#" + methodSignature(method);
			TargetRefreshable currentTarget = targetListenerMap.get(refreshTargetKey);
			if (currentTarget != null) {
				log.info("[Nacos Config] reset {} listener from  {} to {} ", refreshTargetKey,
						currentTarget.getTarget(), bean);
				targetListenerMap.get(refreshTargetKey).setTarget(bean);
				return;
			}

			log.info("[Nacos Config] register {} listener on {} ", refreshTargetKey,
					bean);

			TargetRefreshable listener = null;
			if (org.springframework.util.StringUtils.hasText(key)) {
				listener = new NacosPropertiesKeyListener(bean, wrapArrayToSet(key)) {

					@Override
					public void configChanged(ConfigChangeEvent event) {
						try {
							ConfigChangeItem changeItem = event.getChangeItem(key);
							String newConfig = changeItem == null ? null : changeItem.getNewValue();

							if (org.springframework.util.StringUtils.hasText(newConfig)) {
								if (invokePrimitiveMethod(method, getTarget(), newConfig)) {
									return;
								}

								Object targetObject = convertContentToTargetType(newConfig, parameterTypes[0]);
								ReflectionUtils.invokeMethod(method, getTarget(), targetObject);
							}
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, bean + "#" + methodSignature(method));
					}
				};
				((AbstractConfigChangeListener) listener).fillContext(dataId, group);
				if (!annotation.initNotify()) {
					((AbstractConfigChangeListener) listener).setLastContent(configInfo);
				}
			}
			else {
				listener = new NacosConfigRefreshableListener(bean) {

					@Override
					public void receiveConfigInfo(String configInfo) {
						if (org.springframework.util.StringUtils.hasText(configInfo)) {
							try {
								if (invokePrimitiveMethod(method, getTarget(), configInfo)) {
									return;
								}
								Object targetObject = convertContentToTargetType(configInfo, parameterTypes[0]);
								ReflectionUtils.invokeMethod(method, getTarget(), targetObject);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}

						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config  listener ,  target %s ] ", bean + "#" + methodSignature(method));
					}
				};
			}

			getNacosConfigManager().getConfigService().addListener(dataId, group, listener);
			targetListenerMap.put(refreshTargetKey, listener);
			if (annotation.initNotify() && org.springframework.util.StringUtils.hasText(configInfo)) {
				try {
					log.info("[Nacos Config] init notify listener of {}  on {} start...", refreshTargetKey,
							bean);
					listener.receiveConfigInfo(configInfo);
					log.info("[Nacos Config] init notify listener of {}  on {} finished ", refreshTargetKey,
							bean);
				}
				catch (Throwable throwable) {
					log.warn("[Nacos Config] init notify listener error", throwable);
					throw throwable;
				}
			}
		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}


	Object convertContentToTargetType(String rawContent, Type type) {

		if (String.class.getCanonicalName().equals(type.getTypeName())) {
			return rawContent;
		}

		if (Properties.class.getCanonicalName().equals(type.getTypeName())) {
			//properties and yaml config to properties.
			Properties properties = new Properties();
			try {
				if (org.springframework.util.StringUtils.hasText(rawContent)) {
					properties = PropertiesUtils.convertToProperties(rawContent);
				}
			}
			catch (Throwable throwable) {
				throw new RuntimeException(throwable);
			}
			return properties;
		}
		return ObjectUtils.convertToObject(rawContent, type);
	}

	private void handleFiledNacosConfigAnnotation(NacosConfig annotation, String beanName, Object bean, Field field) {
		String dataId = annotation.dataId();
		String group = annotation.group();
		String key = annotation.key();
		try {
			ReflectionUtils.makeAccessible(field);
			handleFiledNacosConfigAnnotationInner(dataId, group, key, beanName, bean, field, annotation.defaultValue());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void handleFiledNacosConfigAnnotationInner(String dataId, String group, String key, String beanName, Object bean,
			Field field, String defaultValue) {
		try {
			String config = getDestContent(getGroupKeyContent(dataId, group), key);
			if (!org.springframework.util.StringUtils.hasText(config)) {
				config = defaultValue;
			}

			//primitive type
			if (handPrimitiveFiled(field, dataId, group, config, key, defaultValue, beanName, bean)) {
				return;
			}

			//for other type.
			if (org.springframework.util.StringUtils.hasText(config)) {
				Object targetObject = convertContentToTargetType(config, field.getGenericType());
				//yaml and json to object
				ReflectionUtils.setField(field, bean, targetObject);
			}

			String refreshTargetKey = beanName + "#filed#" + field.getName();
			TargetRefreshable currentTarget = targetListenerMap.get(refreshTargetKey);
			if (currentTarget != null) {
				log.info("[Nacos Config] reset {} listener from  {} to {} ", refreshTargetKey,
						currentTarget.getTarget(), bean);
				targetListenerMap.get(refreshTargetKey).setTarget(bean);
				return;
			}

			log.info("[Nacos Config] register {} listener on {} ", refreshTargetKey,
					bean);
			TargetRefreshable listener = null;
			if (org.springframework.util.StringUtils.hasText(key)) {
				listener = new NacosPropertiesKeyListener(bean, wrapArrayToSet(key)) {

					@Override
					public void configChanged(ConfigChangeEvent event) {
						try {
							ConfigChangeItem changeItem = event.getChangeItem(key);
							String newConfig = changeItem == null ? null : changeItem.getNewValue();
							if (!org.springframework.util.StringUtils.hasText(newConfig)) {
								newConfig = defaultValue;
							}
							if (org.springframework.util.StringUtils.hasText(newConfig)) {
								Object targetObject = convertContentToTargetType(newConfig, field.getGenericType());
								ReflectionUtils.setField(field, getTarget(), targetObject);
							}
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, bean + "#" + field.getName());
					}
				};
			}
			else {
				listener = new NacosConfigRefreshableListener(bean) {

					@Override
					public void receiveConfigInfo(String configInfo) {
						if (!org.springframework.util.StringUtils.hasText(configInfo)) {
							configInfo = defaultValue;
						}
						if (org.springframework.util.StringUtils.hasText(configInfo)) {
							Object targetObject = convertContentToTargetType(configInfo, field.getGenericType());
							ReflectionUtils.setField(field, getTarget(), targetObject);
						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, bean + "#" + field.getName());
					}
				};
			}

			getNacosConfigManager().getConfigService()
					.addListener(dataId, group, listener);
			targetListenerMap.put(refreshTargetKey, listener);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean handPrimitiveFiled(Field field, String dataId, String group, String config, String key, String defaultValue, String beanName, Object bean) throws Exception {
		if (field.getType().isPrimitive()) {

			if (org.springframework.util.StringUtils.hasText(config)) {
				try {
					setPrimitiveFiled(field, bean, config);
				}
				catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				}
			}

			String refreshTargetKey = beanName + "#filed#" + field.getName();
			TargetRefreshable currentTarget = targetListenerMap.get(refreshTargetKey);
			if (currentTarget != null) {
				log.info("[Nacos Config] reset {} listener from  {} to {} ", refreshTargetKey,
						currentTarget.getTarget(), bean);
				targetListenerMap.get(refreshTargetKey).setTarget(bean);
				return true;
			}

			log.info("[Nacos Config] register {} listener on {} ", refreshTargetKey,
					bean);
			TargetRefreshable listener = null;
			if (org.springframework.util.StringUtils.hasText(key)) {
				listener = new NacosPropertiesKeyListener(bean, wrapArrayToSet(key)) {

					@Override
					public void configChanged(ConfigChangeEvent event) {
						try {
							ConfigChangeItem changeItem = event.getChangeItem(key);
							String newConfig = changeItem == null ? null : changeItem.getNewValue();
							if (!org.springframework.util.StringUtils.hasText(newConfig)) {
								newConfig = defaultValue;
							}
							if (org.springframework.util.StringUtils.hasText(newConfig)) {
								setPrimitiveFiled(field, getTarget(), newConfig);
							}
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, bean + "#" + field.getName());
					}
				};
			}
			else {
				listener = new NacosConfigRefreshableListener(bean) {

					@Override
					public void receiveConfigInfo(String configInfo) {
						if (!org.springframework.util.StringUtils.hasText(configInfo)) {
							configInfo = defaultValue;
						}
						if (org.springframework.util.StringUtils.hasText(configInfo)) {
							try {
								setPrimitiveFiled(field, getTarget(), configInfo);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}

					@Override
					public String toString() {
						return String.format("[spring cloud alibaba nacos config key listener , key %s , target %s ] ", key, bean + "#" + field.getName());
					}
				};
			}

			getNacosConfigManager().getConfigService()
					.addListener(dataId, group, listener);
			targetListenerMap.put(refreshTargetKey, listener);
			return true;
		}
		return false;
	}

	private boolean setPrimitiveFiled(Field filed, Object bean, String value) throws Exception {
		if (filed.getType() == int.class) {
			filed.setInt(bean, Integer.parseInt(value));
		}
		else if (filed.getType() == Integer.class) {
			ReflectionUtils.setField(filed, bean, Integer.valueOf(value));
		}
		else if (filed.getType() == long.class) {
			filed.setLong(bean, Long.parseLong(value));
		}
		else if (filed.getType() == Long.class) {
			ReflectionUtils.setField(filed, bean, Long.valueOf(value));
		}
		else if (filed.getType() == boolean.class) {
			filed.setBoolean(bean, Boolean.parseBoolean(value));
		}
		else if (filed.getType() == Boolean.class) {
			ReflectionUtils.setField(filed, bean, Boolean.valueOf(value));
		}
		else if (filed.getType() == double.class) {
			filed.setDouble(bean, Double.parseDouble(value));
		}
		else if (filed.getType() == Double.class) {
			ReflectionUtils.setField(filed, bean, Double.valueOf(value));
		}
		else if (filed.getType() == float.class) {
			filed.setFloat(bean, Float.parseFloat(value));
		}
		else if (filed.getType() == Float.class) {
			ReflectionUtils.setField(filed, bean, Float.valueOf(value));
		}
		else {
			return false;
		}
		return true;
	}

	private boolean invokePrimitiveMethod(Method method, Object bean, String value) throws Exception {
		Class<?> parameterType = method.getParameterTypes()[0];
		if (parameterType == int.class) {
			ReflectionUtils.invokeMethod(method, bean, Integer.parseInt(value));
		}
		else if (parameterType == Integer.class) {
			ReflectionUtils.invokeMethod(method, bean, Integer.valueOf(value));
		}
		else if (parameterType == long.class) {
			ReflectionUtils.invokeMethod(method, bean, Long.parseLong(value));
		}
		else if (parameterType == Long.class) {
			ReflectionUtils.invokeMethod(method, bean, Long.valueOf(value));
		}
		else if (parameterType == boolean.class) {
			ReflectionUtils.invokeMethod(method, bean, Boolean.parseBoolean(value));
		}
		else if (parameterType == Boolean.class) {
			ReflectionUtils.invokeMethod(method, bean, Boolean.valueOf(value));
		}
		else if (parameterType == double.class) {
			ReflectionUtils.invokeMethod(method, bean, Double.parseDouble(value));
		}
		else if (parameterType == Double.class) {
			ReflectionUtils.invokeMethod(method, bean, Double.valueOf(value));
		}
		else if (parameterType == float.class) {
			ReflectionUtils.invokeMethod(method, bean, Float.parseFloat(value));
		}
		else if (parameterType == Float.class) {
			ReflectionUtils.invokeMethod(method, bean, Float.valueOf(value));
		}
		else {
			return false;
		}
		return true;
	}

	private String getDestContent(String content, String key) throws Exception {
		if (org.springframework.util.StringUtils.hasText(key)) {
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
			ReflectionUtils.makeAccessible(method);
			handleMethodNacosConfigKeysChangeListener(keysAnnotation, beanName, bean, method);
			return;
		}
		NacosConfigListener configAnnotation = AnnotationUtils.getAnnotation(method, NacosConfigListener.class);
		if (configAnnotation != null) {
			ReflectionUtils.makeAccessible(method);
			handleMethodNacosConfigListener(configAnnotation, beanName, bean, method);
			return;
		}

		if (!applicationContext.containsBeanDefinition(beanName)) {
			return;
		}
		BeanDefinition beanDefinition = ((GenericApplicationContext) applicationContext).getBeanDefinition(beanName);
		if (beanDefinition instanceof AnnotatedBeanDefinition) {
			MethodMetadata factoryMethodMetadata = (((AnnotatedBeanDefinition) beanDefinition).getFactoryMethodMetadata());
			if (factoryMethodMetadata != null) {
				MergedAnnotations annotations = factoryMethodMetadata.getAnnotations();
				if (annotations != null && annotations.isPresent(NacosConfig.class)) {
					MergedAnnotation<NacosConfig> nacosConfigMergedAnnotation = annotations.get(NacosConfig.class);
					Map<String, Object> stringObjectMap = nacosConfigMergedAnnotation.asMap();
					String dataId = (String) stringObjectMap.get("dataId");
					String group = (String) stringObjectMap.get("group");
					String key = (String) stringObjectMap.get("key");
					String defaultValue = (String) stringObjectMap.get("defaultValue");
					handleBeanNacosConfigAnnotation(dataId, group, key, beanName, bean, defaultValue);
				}
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private NacosConfigManager getNacosConfigManager() {
		if (this.nacosConfigManager == null) {
			nacosConfigManager = this.applicationContext.getBean(NacosConfigManager.class);
		}
		return nacosConfigManager;
	}

	private static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		PropertyDescriptor[] pds = src.getPropertyDescriptors();
		Set<String> nullPropertyNames = new HashSet<>();
		for (PropertyDescriptor pd : pds) {
			String propertyName = pd.getName();
			try {
				Object propertyValue = src.getPropertyValue(propertyName);
				if (propertyValue == null) {
					nullPropertyNames.add(propertyName);
				}
			}
			catch (NotReadablePropertyException e) {
				//ignore
				nullPropertyNames.add(propertyName);
			}
		}
		return nullPropertyNames.toArray(new String[0]);
	}
}
