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

package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.alibaba.cloud.circuitbreaker.sentinel.SentinelConfigBuilder;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.circuitbreaker.AbstractCircuitBreakerFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Sentinel circuit breaker config change listener.
 *
 * @author freeman
 * @since 2021.0.1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CircuitBreakerRuleChangeListener implements ApplicationContextAware,
		ApplicationListener<RefreshScopeRefreshedEvent>, SmartInitializingSingleton {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CircuitBreakerRuleChangeListener.class);

	private SentinelFeignClientProperties properties;
	/**
	 * properties backup, prevent rules from being updated every time the container is
	 * refreshed.
	 */
	private SentinelFeignClientProperties propertiesBackup;
	private AbstractCircuitBreakerFactory circuitBreakerFactory;
	private ApplicationContext applicationContext;

	@Override
	public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
		ensureReady();

		// No need to update the rules
		if (Objects.equals(properties, propertiesBackup)) {
			return;
		}

		clearRules();

		// rebind
		configureDefault();
		configureCustom();

		updateBackup();

		LOGGER.info("Sentinel circuit beaker rules refreshed: \n"
				+ prettyPrint(properties.getRules()));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.propertiesBackup = applicationContext
				.getBean(SentinelFeignClientProperties.class).copy();
	}

	private void ensureReady() {
		// Do not inject these beans directly,
		// as it will cause the bean to be initialized prematurely,
		// and we don't want to change the initialization order of the beans
		if (circuitBreakerFactory == null) {
			String[] names = applicationContext
					.getBeanNamesForType(AbstractCircuitBreakerFactory.class);
			if (names.length >= 1) {
				this.circuitBreakerFactory = applicationContext.getBean(names[0],
						AbstractCircuitBreakerFactory.class);
			}
		}
		if (properties == null) {
			this.properties = applicationContext
					.getBean(SentinelFeignClientProperties.class);
		}
	}

	private void clearRules() {
		clearCircuitBreakerFactory();
		clearFeignClientRulesInDegradeManager();
	}

	private void configureDefault() {
		configureDefault(properties, circuitBreakerFactory);
	}

	private void configureCustom() {
		configureCustom(properties, circuitBreakerFactory);
	}

	private void clearCircuitBreakerFactory() {
		Optional.ofNullable(getConfigurations(circuitBreakerFactory))
				.ifPresent(Map::clear);
	}

	private void clearFeignClientRulesInDegradeManager() {
		// first, clear all manually configured feign clients and methods.
		propertiesBackup.getRules().keySet().stream()
				.filter(key -> !Objects.equals(key, propertiesBackup.getDefaultRule()))
				.forEach(resource -> Optional
						.ofNullable(DegradeRuleManager.getRulesOfResource(resource))
						.ifPresent(Set::clear));

		// Find all feign clients, clear the corresponding rules
		// NOTE: feign client name cannot be the same as the general resource name !!!
		Arrays.stream(applicationContext.getBeanNamesForAnnotation(FeignClient.class))
				// A little trick, FeignClient bean name is full class name.
				// Simple exclusions, such as its subclass.
				.filter(beanName -> beanName.contains(".")).map(beanName -> {
					try {
						return Class.forName(beanName);
					}
					catch (ClassNotFoundException ignore) {
						// definitely not a feign client, just ignore
						return null;
					}
				}).filter(Objects::nonNull).forEach(clazz -> {
					FeignClient anno = clazz.getAnnotation(FeignClient.class);
					if (anno == null || AnnotationUtils.getValue(anno) == null) {
						return;
					}
					String feignClientName = AnnotationUtils.getValue(anno).toString();
					Optional.ofNullable(
							DegradeRuleManager.getRulesOfResource(feignClientName))
							.ifPresent(Set::clear);
				});
	}

	private void updateBackup() {
		this.propertiesBackup = this.properties.copy();
	}

	private String prettyPrint(Object o) {
		try {
			return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
					.writeValueAsString(o);
		}
		catch (JsonProcessingException e) {
			LOGGER.error("JSON serialization err.", e);
			return "__JSON format err__";
		}
	}

	// static method

	public static void configureCustom(SentinelFeignClientProperties properties,
			AbstractCircuitBreakerFactory factory) {
		properties.getRules().forEach((resourceName, degradeRules) -> {
			if (!Objects.equals(properties.getDefaultRule(), resourceName)) {
				factory.configure(builder -> ((SentinelConfigBuilder) builder)
						.rules(properties.getRules().getOrDefault(resourceName,
								new ArrayList<>())),
						resourceName);
			}
		});
	}

	public static void configureDefault(SentinelFeignClientProperties properties,
			AbstractCircuitBreakerFactory factory) {
		List<DegradeRule> defaultConfigurations = properties.getRules()
				.getOrDefault(properties.getDefaultRule(), new ArrayList<>());
		factory.configureDefault(
				resourceName -> new SentinelConfigBuilder(resourceName.toString())
						.entryType(EntryType.OUT).rules(defaultConfigurations).build());
	}

	public static Map getConfigurations(
			AbstractCircuitBreakerFactory circuitBreakerFactory) {
		try {
			Method method = AbstractCircuitBreakerFactory.class
					.getDeclaredMethod("getConfigurations");
			method.setAccessible(true);
			return (Map) method.invoke(circuitBreakerFactory);
		}
		catch (Exception ignored) {
		}
		return Collections.emptyMap();
	}

}
