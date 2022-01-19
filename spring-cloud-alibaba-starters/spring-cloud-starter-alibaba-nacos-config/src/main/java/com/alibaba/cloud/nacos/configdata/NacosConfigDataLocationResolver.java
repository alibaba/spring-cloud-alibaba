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
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import org.apache.commons.logging.Log;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.*;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.StandardEnvironment;

import static com.alibaba.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;

/**
 * @author freeman
 */
public class NacosConfigDataLocationResolver
		implements ConfigDataLocationResolver<NacosConfigDataResource>, Ordered {

	private static final String HYPHEN = "-";

	private static final String DOT = ".";
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
			// Avoid NPE when call `assembleConfigServiceProperties` method.
			// Early stage of the main container, set environment directly.
			nacosConfigProperties.setEnvironment(new StandardEnvironment());
			nacosConfigProperties.init();
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
		NacosConfigProperties properties = loadProperties(resolverContext);

		ConfigurableBootstrapContext bootstrapContext = resolverContext
				.getBootstrapContext();

		bootstrapContext.registerIfAbsent(NacosConfigProperties.class,
				InstanceSupplier.of(properties));

		registerOrUpdateIndexes(properties, bootstrapContext);

		// Retain the processing logic of the old version.
		// Make sure to upgrade smoothly.
		return loadConfigDataResources(resolverContext, location, profiles, properties);
	}

	private List<NacosConfigDataResource> loadConfigDataResources(
			ConfigDataLocationResolverContext resolverContext,
			ConfigDataLocation location, Profiles profiles,
			NacosConfigProperties properties) {
		List<NacosConfigDataResource> result = new ArrayList<>();
		String uris = getUri(location, properties);

		if (StringUtils.isNotBlank(dataIdFor(uris))) {
			// Can be considered as an extension-config.
			NacosConfigDataResource resource = loadResource(properties,
					location.isOptional(), profiles, uris, dataIdFor(uris));
			result.add(resource);
		}
		else {
			String prefix = dataIdPrefixFor(resolverContext.getBinder(), properties);
			NacosConfigDataResource resource = loadResource(properties,
					location.isOptional(), profiles, uris, prefix);
			result.add(resource);

			resource = loadResource(properties, location.isOptional(), profiles, uris,
					prefix + DOT + properties.getFileExtension());
			result.add(resource);

			for (String profile : profiles.getActive()) {
				String dataId = prefix + HYPHEN + profile + DOT
						+ properties.getFileExtension();
				resource = loadResource(properties, location.isOptional(), profiles, uris,
						dataId);
				result.add(resource);
			}
		}
		return result;
	}

	private NacosConfigDataResource loadResource(NacosConfigProperties properties,
			boolean optional, Profiles profiles, String uris, String dataId) {
		return new NacosConfigDataResource(properties, optional, profiles,
				log,
				new NacosItemConfig()
						.setNamespace(Objects.toString(properties.getNamespace(),
								DEFAULT_NAMESPACE))
						.setGroup(groupFor(uris, properties)).setDataId(dataId)
						.setSuffix(suffixFor(uris, properties))
						.setRefreshEnabled(refreshEnabledFor(uris, properties)));
	}

	private String getUri(ConfigDataLocation location, NacosConfigProperties properties) {
		String path = location.getNonPrefixedValue(getPrefix());
		if (StringUtils.isBlank(path)) {
			path = "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return properties.getServerAddr() + path;
	}

	private void registerOrUpdateIndexes(NacosConfigProperties properties,
										 ConfigurableBootstrapContext bootstrapContext) {
		String namespace = Objects.toString(properties.getNamespace(), DEFAULT_NAMESPACE);
		ConfigService configService;
		try {
			configService = ConfigFactory.createConfigService(properties.assembleConfigServiceProperties());
		} catch (NacosException e) {
			return;
		}
		if (bootstrapContext.isRegistered(ConfigServiceIndexes.class)) {
			ConfigServiceIndexes indexes = bootstrapContext
					.get(ConfigServiceIndexes.class);
			indexes.getIndexes().putIfAbsent(namespace, configService);
		} else {
			bootstrapContext.register(ConfigServiceIndexes.class, (context) -> {
				Map<String, ConfigService> indexes = new ConcurrentHashMap<>(4);
				indexes.put(namespace, configService);
				return () -> indexes;
			});
		}
	}

	private URI getUri(String uris) {
		if (!uris.startsWith("http://") && !uris.startsWith("https://")) {
			uris = "http://" + uris;
		}
		URI uri;
		try {
			uri = new URI(uris);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("illegal URI: " + uris);
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

	private String dataIdPrefixFor(Binder binder, NacosConfigProperties properties) {
		String dataIdPrefix = properties.getPrefix();
		if (StringUtils.isEmpty(dataIdPrefix)) {
			dataIdPrefix = properties.getName();
		}
		if (StringUtils.isEmpty(dataIdPrefix)) {
			dataIdPrefix = binder.bind("spring.application.name", String.class).get();
		}
		return dataIdPrefix;
	}

	private String dataIdFor(String uris) {
		String[] part = split(uris);
		if (part.length >= 2 && !part[1].isEmpty()) {
			return part[1];
		}
		return null;
	}


	private String suffixFor(String uris, NacosConfigProperties properties) {
		String[] part = split(uris);
		String dataId = null;
		if (part.length >= 2 && !part[1].isEmpty()) {
			dataId = part[1];
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
