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

package com.alibaba.cloud.nacos.configdata;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import org.apache.commons.logging.Log;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;

import static com.alibaba.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;

/**
 * Implementation of {@link ConfigDataLocationResolver}, load Nacos
 * {@link ConfigDataResource}.
 *
 * @author freeman
 * @since 2021.0.1.0
 */
public class NacosConfigDataLocationResolver
		implements ConfigDataLocationResolver<NacosConfigDataResource>, Ordered {
	/**
	 * Prefix for Config Server imports.
	 */
	public static final String PREFIX = "nacos:";

	private final Log log;

	// support params

	private static final String GROUP = "group";

	private static final String REFRESH_ENABLED = "refreshEnabled";

	private static final String PREFERENCE = "preference";

	public NacosConfigDataLocationResolver(DeferredLogFactory logFactory) {
		this.log = logFactory.getLog(getClass());
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
					.bind("spring.cloud.nacos", Bindable.of(NacosConfigProperties.class),
							bindHandler)
					.map(properties -> binder
							.bind(NacosConfigProperties.PREFIX,
									Bindable.ofInstance(properties), bindHandler)
							.orElse(properties))
					.orElseGet(() -> binder
							.bind(NacosConfigProperties.PREFIX,
									Bindable.of(NacosConfigProperties.class), bindHandler)
							.orElseGet(NacosConfigProperties::new));
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

		registerConfigManager(properties, bootstrapContext);

		return loadConfigDataResources(location, profiles, properties);
	}

	private List<NacosConfigDataResource> loadConfigDataResources(
			ConfigDataLocation location, Profiles profiles,
			NacosConfigProperties properties) {
		List<NacosConfigDataResource> result = new ArrayList<>();
		URI uri = getUri(location, properties);

		if (StringUtils.isBlank(dataIdFor(uri))) {
			throw new IllegalArgumentException("dataId must be specified");
		}

		NacosConfigDataResource resource = new NacosConfigDataResource(properties,
				location.isOptional(), profiles, log,
				new NacosItemConfig().setGroup(groupFor(uri, properties))
						.setDataId(dataIdFor(uri)).setSuffix(suffixFor(uri, properties))
						.setRefreshEnabled(refreshEnabledFor(uri, properties))
						.setPreference(preferenceFor(uri)));
		result.add(resource);

		return result;
	}

	private String preferenceFor(URI uri) {
		return getQueryMap(uri).get(PREFERENCE);
	}

	private URI getUri(ConfigDataLocation location, NacosConfigProperties properties) {
		String path = location.getNonPrefixedValue(getPrefix());
		if (StringUtils.isBlank(path)) {
			path = "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String uri = properties.getServerAddr() + path;
		return getUri(uri);
	}

	private void registerConfigManager(NacosConfigProperties properties,
			ConfigurableBootstrapContext bootstrapContext) {
		if (!bootstrapContext.isRegistered(NacosConfigManager.class)) {
			bootstrapContext.register(NacosConfigManager.class,
					InstanceSupplier.of(new NacosConfigManager(properties)));
		}
	}

	private URI getUri(String uris) {
		if (!uris.startsWith("http://") && !uris.startsWith("https://")) {
			uris = "http://" + uris;
		}
		URI uri;
		try {
			uri = new URI(uris);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("illegal URI: " + uris);
		}
		return uri;
	}

	private String groupFor(URI uri, NacosConfigProperties properties) {
		Map<String, String> queryMap = getQueryMap(uri);
		return queryMap.containsKey(GROUP) ? queryMap.get(GROUP) : properties.getGroup();
	}

	private Map<String, String> getQueryMap(URI uri) {
		String query = uri.getQuery();
		if (StringUtils.isBlank(query)) {
			return Collections.emptyMap();
		}
		Map<String, String> result = new HashMap<>(4);
		for (String entry : query.split("&")) {
			String[] kv = entry.split("=");
			if (kv.length == 2) {
				result.put(kv[0], kv[1]);
			}
		}
		return result;
	}

	private String suffixFor(URI uri, NacosConfigProperties properties) {
		String dataId = dataIdFor(uri);
		if (dataId != null && dataId.contains(".")) {
			return dataId.substring(dataId.lastIndexOf('.') + 1);
		}
		return properties.getFileExtension();
	}

	private boolean refreshEnabledFor(URI uri, NacosConfigProperties properties) {
		Map<String, String> queryMap = getQueryMap(uri);
		return queryMap.containsKey(REFRESH_ENABLED)
				? Boolean.parseBoolean(queryMap.get(REFRESH_ENABLED))
				: properties.isRefreshEnabled();
	}

	private String dataIdFor(URI uri) {
		String path = uri.getPath();
		// notice '/'
		if (path == null || path.length() <= 1) {
			return StringUtils.EMPTY;
		}
		String[] parts = path.substring(1).split("/");
		if (parts.length != 1) {
			throw new IllegalArgumentException("illegal dataId");
		}
		return parts[0];
	}

}
