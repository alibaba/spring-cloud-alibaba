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

package com.alibaba.cloud.nacos.refresh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder;
import org.springframework.cloud.context.refresh.LegacyContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * On application start up, NacosContextRefresher add nacos listeners to all application
 * level dataIds, when there is a change in the data, listeners will refresh
 * configurations.
 *
 * @author juven.xuxb
 * @author pbting
 * @author keray
 */
public class NacosContextRefresher extends LegacyContextRefresher
		implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

	private final static Logger log = LoggerFactory
			.getLogger(NacosContextRefresher.class);

	private static final AtomicLong REFRESH_COUNT = new AtomicLong(0);

	private NacosConfigProperties nacosConfigProperties;

	private final boolean isRefreshEnabled;

	private final NacosRefreshHistory nacosRefreshHistory;

	private final ConfigService configService;

	private final ConfigurationPropertiesRebinder rebinder;

	private ApplicationContext applicationContext;

	private final AtomicBoolean ready = new AtomicBoolean(false);

	private final Map<String, Listener> listenerMap = new ConcurrentHashMap<>(16);

	public NacosContextRefresher(
			NacosConfigManager nacosConfigManager,
			NacosRefreshHistory refreshHistory,
			ConfigurableApplicationContext context,
			ConfigurationPropertiesRebinder rebinder) {
		super(context, new RefreshScope());
		this.nacosConfigProperties = nacosConfigManager.getNacosConfigProperties();
		this.nacosRefreshHistory = refreshHistory;
		this.configService = nacosConfigManager.getConfigService();
		this.isRefreshEnabled = this.nacosConfigProperties.isRefreshEnabled();
		this.rebinder = rebinder;
	}

	/**
	 * recommend to use
	 * {@link NacosContextRefresher#NacosContextRefresher(NacosConfigManager, NacosRefreshHistory, ConfigurableApplicationContext, ConfigurationPropertiesRebinder)}.
	 * @param refreshProperties refreshProperties
	 * @param refreshHistory refreshHistory
	 * @param configService configService
	 */
	@Deprecated
	public NacosContextRefresher(
			NacosRefreshProperties refreshProperties,
			NacosRefreshHistory refreshHistory,
			ConfigurableApplicationContext context,
			ConfigurationPropertiesRebinder rebinder,
			ConfigService configService) {
		super(context, new RefreshScope());
		this.isRefreshEnabled = refreshProperties.isEnabled();
		this.nacosRefreshHistory = refreshHistory;
		this.rebinder = rebinder;
		this.configService = configService;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		// many Spring context
		if (this.ready.compareAndSet(false, true)) {
			this.registerNacosListenersForApplications();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * register Nacos Listeners.
	 */
	private void registerNacosListenersForApplications() {
		if (isRefreshEnabled()) {
			for (NacosPropertySource propertySource : NacosPropertySourceRepository
					.getAll()) {
				if (!propertySource.isRefreshable()) {
					continue;
				}
				String dataId = propertySource.getDataId();
				registerNacosListener(propertySource.getGroup(), dataId);
			}
		}
	}

	private void registerNacosListener(final String groupKey, final String dataKey) {
		String key = NacosPropertySourceRepository.getMapKey(dataKey, groupKey);
		Listener listener = listenerMap.computeIfAbsent(key, lst -> new EnvironmentSingleRefreshSharedListener(this, applicationContext));
		try {
			configService.addListener(dataKey, groupKey, listener);
		}
		catch (NacosException e) {
			log.warn(String.format(
					"register fail for nacos listener ,dataId=[%s],group=[%s]", dataKey,
					groupKey), e);
		}
	}

	public NacosConfigProperties getNacosConfigProperties() {
		return nacosConfigProperties;
	}

	public NacosContextRefresher setNacosConfigProperties(
			NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigProperties = nacosConfigProperties;
		return this;
	}

	public boolean isRefreshEnabled() {
		if (null == nacosConfigProperties) {
			return isRefreshEnabled;
		}
		// Compatible with older configurations
		if (nacosConfigProperties.isRefreshEnabled() && !isRefreshEnabled) {
			return false;
		}
		return isRefreshEnabled;
	}

	public static long getRefreshCount() {
		return REFRESH_COUNT.get();
	}

	public static void refreshCountIncrement() {
		REFRESH_COUNT.incrementAndGet();
	}


	public synchronized Set<String> refreshEnvironment() {
		Map<String, Object> before = extract(this.getContext().getEnvironment().getPropertySources());
		updateEnvironment();
		return changes(before, extract(this.getContext().getEnvironment().getPropertySources())).keySet();
	}


	public void refresh(String name) {
		if (StringUtils.hasText(name)) {
			rebinder.rebind(name);
		}
	}


	private Map<String, Object> changes(Map<String, Object> before, Map<String, Object> after) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (String key : before.keySet()) {
			if (!after.containsKey(key)) {
				result.put(key, null);
			}
			else if (!equal(before.get(key), after.get(key))) {
				result.put(key, after.get(key));
			}
		}
		for (String key : after.keySet()) {
			if (!before.containsKey(key)) {
				result.put(key, after.get(key));
			}
		}
		return result;
	}

	private boolean equal(Object one, Object two) {
		if (one == null && two == null) {
			return true;
		}
		if (one == null || two == null) {
			return false;
		}
		return one.equals(two);
	}

	private Map<String, Object> extract(MutablePropertySources propertySources) {
		Map<String, Object> result = new HashMap<String, Object>();
		List<PropertySource<?>> sources = new ArrayList<PropertySource<?>>();
		for (PropertySource<?> source : propertySources) {
			sources.add(0, source);
		}
		for (PropertySource<?> source : sources) {
			if (!this.standardSources.contains(source.getName())) {
				extract(source, result);
			}
		}
		return result;
	}

	private void extract(PropertySource<?> parent, Map<String, Object> result) {
		if (parent instanceof CompositePropertySource) {
			try {
				List<PropertySource<?>> sources = new ArrayList<PropertySource<?>>();
				for (PropertySource<?> source : ((CompositePropertySource) parent).getPropertySources()) {
					sources.add(0, source);
				}
				for (PropertySource<?> source : sources) {
					extract(source, result);
				}
			}
			catch (Exception ignored) {
			}
		}
		else if (parent instanceof EnumerablePropertySource) {
			for (String key : ((EnumerablePropertySource<?>) parent).getPropertyNames()) {
				result.put(key, parent.getProperty(key));
			}
		}
	}


	static class EnvironmentSingleRefreshSharedListener extends AbstractSharedListener {

		private final NacosContextRefresher nacosContextRefresher;

		private final ApplicationContext applicationContext;

		EnvironmentSingleRefreshSharedListener(NacosContextRefresher nacosContextRefresher, ApplicationContext applicationContext) {
			this.nacosContextRefresher = nacosContextRefresher;
			this.applicationContext = applicationContext;
		}

		@Override
		public synchronized void innerReceive(String dataId, String group, String configInfo) {
			refreshCountIncrement();
			nacosContextRefresher.nacosRefreshHistory.addRefreshRecord(dataId, group, configInfo);
			Set<String> keys = nacosContextRefresher.refreshEnvironment();
			Set<String> changeBeanNames = new HashSet<>();
			for (String key:keys) {
				changeBeanNames.add(keyFindBeanName(key));
			}
			for (String beanName:changeBeanNames) {
				nacosContextRefresher.refresh(beanName);
			}
			if (log.isDebugEnabled()) {
				log.debug(String.format(
						"Refresh Nacos config group=%s,dataId=%s,configInfo=%s, beans=%s",
						group, dataId, configInfo, String.join(",", changeBeanNames)));
			}
		}

		private String keyFindBeanName(String changeKey) {
			Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ConfigurationProperties.class);
			for (Map.Entry<String, Object> entry: beans.entrySet()) {
				Object beanInstance = entry.getValue();
				ConfigurationProperties properties = AnnotationUtils.findAnnotation(beanInstance.getClass(), ConfigurationProperties.class);
				if (properties == null) {
					continue;
				}
				String value =  StringUtils.hasLength(properties.value()) ? properties.value() : properties.prefix();
				if (!StringUtils.hasLength(value)) {
					continue;
				}
				if (changeKey.startsWith(value)) {
					return entry.getKey();
				}
			}
			return null;
		}
	}

}
