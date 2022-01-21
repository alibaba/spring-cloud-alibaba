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

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.common.utils.StringUtils;
import org.apache.commons.logging.Log;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.*;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import static com.alibaba.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;

/**
 * Implementation of {@link ConfigDataLocationResolver}, load Nacos {@link ConfigDataResource}.
 *
 * @author freeman
 */
public class NacosConfigDataLocationResolver
		implements ConfigDataLocationResolver<NacosConfigDataResource>, Ordered {
	/**
	 * Prefix for Config Server imports.
	 */
	public static final String PREFIX = "nacos:";

	private final Log log;

	// support params

	public static final String GROUP = "group";

	public static final String REFRESH_ENABLED = "refreshEnabled";

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
			// this NacosConfigProperties will disappear after the configData is imported.
			// won't appear in the main container.
			nacosConfigProperties.setEnvironment(prepareEnvironment(binder));
			nacosConfigProperties.init();
		}

		return nacosConfigProperties;
	}

	private StandardEnvironment prepareEnvironment(Binder binder) {
		// bind `spring.cloud.nacos.xxx` to Environment
		StandardEnvironment environment = new StandardEnvironment();
		Map<String, Object> kvMap = new HashMap<>();
		binder.bind("spring.cloud.nacos", Map.class)
				.orElse(Collections.emptyMap())
				.forEach((k, v) -> kvMap.put("spring.cloud.nacos." + k, v));
		MapPropertySource propertySource = new MapPropertySource("nacosCommonProperties", kvMap);
		environment.getPropertySources().addLast(propertySource);
		return environment;
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

		registerConfigService(properties, bootstrapContext);

		return loadConfigDataResources(location, profiles, properties);
	}

	private List<NacosConfigDataResource> loadConfigDataResources(
			ConfigDataLocation location, Profiles profiles,
			NacosConfigProperties properties) {
		List<NacosConfigDataResource> result = new ArrayList<>();
		String uris = getUri(location, properties);

		if (StringUtils.isBlank(dataIdFor(uris))) {
			throw new IllegalArgumentException("dataId must be specified");
		}

		NacosConfigDataResource resource = new NacosConfigDataResource(properties,
				location.isOptional(), profiles, log,
				new NacosItemConfig().setGroup(groupFor(uris, properties))
						.setDataId(dataIdFor(uris)).setSuffix(suffixFor(uris, properties))
						.setRefreshEnabled(refreshEnabledFor(uris, properties)));
		result.add(resource);

		return result;
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

	private void registerConfigService(NacosConfigProperties properties,
									   ConfigurableBootstrapContext bootstrapContext) {
		if (!bootstrapContext.isRegistered(ConfigService.class)) {
			Optional.ofNullable(new NacosConfigManager(properties).getConfigService())
					.ifPresent(configService -> bootstrapContext.register(
							ConfigService.class, InstanceSupplier.of(configService)));
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
		Map<String, String> queryMap = getQueryMap(uris);
		return queryMap.containsKey(GROUP)
				? queryMap.get(GROUP)
				: properties.getGroup();
	}

	private Map<String, String> getQueryMap(String uris) {
		String query = getUri(uris).getQuery();
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

	private String suffixFor(String uris, NacosConfigProperties properties) {
		String dataId = dataIdFor(uris);
		if (dataId != null && dataId.contains(".")) {
			return dataId.substring(dataId.lastIndexOf('.') + 1);
		}
		return properties.getFileExtension();
	}

	private boolean refreshEnabledFor(String uris, NacosConfigProperties properties) {
		Map<String, String> queryMap = getQueryMap(uris);
		return queryMap.containsKey(REFRESH_ENABLED)
				? Boolean.parseBoolean(queryMap.get(REFRESH_ENABLED))
				: properties.isRefreshEnabled();
	}

	private String dataIdFor(String uris) {
		URI uri = getUri(uris);
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
