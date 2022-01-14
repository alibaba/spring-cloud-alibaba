/*
 * Copyright 2013-2020 the original author or authors.
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

package com.alibaba.cloud.nacos.configdata;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import org.apache.commons.logging.Log;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.BootstrapContextClosedEvent;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.*;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;

import static com.alibaba.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;

/**
 * @author freeman
 */
public class NacosConfigDataLocationResolver
		implements ConfigDataLocationResolver<NacosConfigDataResource>, Ordered {

	/**
	 * Prefix for Config Server imports.
	 */
	public static final String PREFIX = "nacos:";

	public static final String DEFAULT_NAMESPACE = StringUtils.EMPTY;

	private final Log log;

	public NacosConfigDataLocationResolver(Log log) {
		this.log = log;
	}

	@Override
	public int getOrder() {
		return -1;
	}

	protected NacosConfigProperties loadProperties(
			ConfigDataLocationResolverContext context) {
		Binder binder = context.getBinder();
		BindHandler bindHandler = getBindHandler(context);

		NacosConfigProperties nacosConfigProperties;
		if (context.getBootstrapContext().isRegistered(NacosConfigProperties.class)) {
			nacosConfigProperties = context.getBootstrapContext()
					.get(NacosConfigProperties.class);
		}
		else {
			nacosConfigProperties = binder
					.bind(NacosConfigProperties.PREFIX,
							Bindable.of(NacosConfigProperties.class), bindHandler)
					.orElseGet(NacosConfigProperties::new);
		}

		return nacosConfigProperties;
	}

	private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
		return context.getBootstrapContext().getOrElse(BindHandler.class, null);
	}

	protected Log getLog() {
		return this.log;
	}

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context,
			ConfigDataLocation location) {
		if (!location.hasPrefix(getPrefix())) {
			return false;
		}
		return context.getBinder()
				.bind(NacosConfigProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
	}

	protected String getPrefix() {
		return PREFIX;
	}

	@Override
	public List<NacosConfigDataResource> resolve(
			ConfigDataLocationResolverContext context, ConfigDataLocation location)
			throws ConfigDataLocationNotFoundException,
			ConfigDataResourceNotFoundException {
		return Collections.emptyList();
	}

	@Override
	public List<NacosConfigDataResource> resolveProfileSpecific(
			ConfigDataLocationResolverContext resolverContext,
			ConfigDataLocation location, Profiles profiles)
			throws ConfigDataLocationNotFoundException {
		String uris = location.getNonPrefixedValue(getPrefix());
		// do not change serverAddr right now
		// in this context, we may use for the default
		NacosConfigProperties properties = loadProperties(resolverContext);

		ConfigurableBootstrapContext bootstrapContext = resolverContext
				.getBootstrapContext();

		bootstrapContext.registerIfAbsent(NacosConfigProperties.class,
				InstanceSupplier.of(properties));

		// Bootstrap context close hook.
		// add NacosConfigProperties to bean factory.
		bootstrapContext.addCloseListener(
				event -> addNacosConfigProperties2BeanFactory(uris, event));

		registerOrUpdateIndexes(uris, properties, bootstrapContext);

		NacosConfigDataResource resource = new NacosConfigDataResource(properties,
				location.isOptional(), profiles, log,
				new NacosItemConfig()
						.setNamespace(Objects.toString(properties.getNamespace(),
								DEFAULT_NAMESPACE))
						.setGroup(groupFor(uris, properties))
						.setDataId(dataIdFor(resolverContext.getBinder(), uris, properties))
						.setSuffix(suffixFor(uris, properties))
						.setRefreshEnabled(refreshEnabledFor(uris, properties)));

		List<NacosConfigDataResource> locations = new ArrayList<>();
		locations.add(resource);

		return locations;
	}

	private void registerOrUpdateIndexes(String uris, NacosConfigProperties properties,
			ConfigurableBootstrapContext bootstrapContext) {
		String namespace = Objects.toString(properties.getNamespace(), DEFAULT_NAMESPACE);
		Properties prop = new Properties();
		prop.put(PropertyKeyConst.NAMESPACE, namespace);
		prop.put(PropertyKeyConst.SERVER_ADDR, serverAddrFor(uris));
		if (bootstrapContext.isRegistered(ConfigServiceIndexes.class)) {
			ConfigServiceIndexes indexes = bootstrapContext
					.get(ConfigServiceIndexes.class);
			if (!indexes.getIndexes().containsKey(namespace)) {
				try {
					indexes.getIndexes().put(namespace,
							ConfigFactory.createConfigService(prop));
				}
				catch (NacosException ignored) {
				}
			}
			return;
		}
		bootstrapContext.register(ConfigServiceIndexes.class, (context) -> {
			try {
				ConfigService configService = ConfigFactory.createConfigService(prop);

				Map<String, ConfigService> indexes = new ConcurrentHashMap<>(4);
				indexes.put(namespace, configService);
				return () -> indexes;
			}
			catch (NacosException e) {
				return () -> new ConcurrentHashMap<>(4);
			}
		});
	}

	private void addNacosConfigProperties2BeanFactory(String uris,
			BootstrapContextClosedEvent event) {
		ConfigurableListableBeanFactory factory = event.getApplicationContext()
				.getBeanFactory();
		NacosConfigProperties nacosConfigProperties = event.getBootstrapContext()
				.get(NacosConfigProperties.class);

		String propertiesBeanDefinitionName = "configDataNacosConfigProperties";
		BeanDefinitionRegistry definitionRegistry = (BeanDefinitionRegistry) factory;
		if (!definitionRegistry.containsBeanDefinition(propertiesBeanDefinitionName)) {
			String serverAddr = serverAddrFor(uris);
			// override serverAddr
			nacosConfigProperties.setServerAddr(serverAddr);
			definitionRegistry.registerBeanDefinition(propertiesBeanDefinitionName,
					BeanDefinitionBuilder
							.genericBeanDefinition(NacosConfigProperties.class, () -> {
								return nacosConfigProperties;
							}).getBeanDefinition());
		}
	}

	private String serverAddrFor(String uris) {
		URI uri = getUri(uris);
		StringBuilder serverAddr = new StringBuilder();
		serverAddr.append(uri.getHost());
		if (uri.getPort() != -1) {
			serverAddr.append(':').append(uri.getPort());
		}
		return serverAddr.toString();
	}

	private URI getUri(String uris) {
		if (!uris.startsWith("http://") && !uris.startsWith("https://")) {
			uris = "http://" + uris;
		}
		URI uri = null;
		try {
			uri = new URI(uris);
		}
		catch (Exception ignored) {
		}
		return uri;
	}

	private String groupFor(String uris, NacosConfigProperties properties) {
		String[] part = split(uris);
		if (part.length >= 1 && !part[0].isEmpty()) {
			return part[0];
		}
		return properties.getGroup();
	}

	private String dataIdFor(Binder binder, String uris,
			NacosConfigProperties properties) {
		String[] part = split(uris);
		if (part.length >= 2 && !part[1].isEmpty()) {
			return part[1];
		}
		if (properties.getName() != null && !properties.getName().isEmpty()) {
			return properties.getName();
		}
		// support {application}-{profile}.{suffix}
		String application = binder.bind("spring.application.name", String.class)
				.orElse("application");
		String profile = binder.bind("spring.profiles.active", String.class).orElse("");
		String suffix = suffixFor(uris, properties);
		if (profile == null || profile.isEmpty()) {
			return application + "." + suffix;
		}
		return application + "-" + profile + "." + suffix;
	}

	private String suffixFor(String uris, NacosConfigProperties properties) {
		String[] part = split(uris);
		String dataId = null;
		if (part.length >= 2 && !part[1].isEmpty()) {
			dataId = part[1];
		}
		else if (properties.getName() != null && !properties.getName().isEmpty()) {
			dataId = properties.getName();
		}
		if (dataId != null && dataId.contains(".")) {
			return dataId.substring(dataId.lastIndexOf('.') + 1);
		}
		return properties.getFileExtension();
	}

	private boolean refreshEnabledFor(String uris, NacosConfigProperties properties) {
		URI uri = getUri(uris);
		if (uri.getQuery() != null && uri.getQuery().contains("refreshEnabled=false")) {
			return false;
		}
		return properties.isRefreshEnabled();
	}

	private String[] split(String uris) {
		URI uri = getUri(uris);
		String path = uri.getPath();
		// notice '/'
		if (path == null || path.length() <= 1) {
			return new String[0];
		}
		path = path.substring(1);
		return path.split("/");
	}

}
