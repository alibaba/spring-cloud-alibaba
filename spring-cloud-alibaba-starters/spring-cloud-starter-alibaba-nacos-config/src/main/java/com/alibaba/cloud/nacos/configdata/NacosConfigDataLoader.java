/*
 * Copyright 2015-2020 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.commons.logging.Log;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigData.Option;
import org.springframework.boot.context.config.ConfigData.Options;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import static com.alibaba.cloud.nacos.configdata.NacosBootstrapper.LoadContext;
import static com.alibaba.cloud.nacos.configdata.NacosBootstrapper.LoaderInterceptor;
import static com.alibaba.cloud.nacos.configdata.NacosConfigDataResource.NacosItemConfig;

/**
 * Implementation of {@link ConfigDataLoader}.
 *
 * <p> Load {@link ConfigData} via {@link NacosConfigDataResource}
 *
 * @author freeman
 */
public class NacosConfigDataLoader implements ConfigDataLoader<NacosConfigDataResource> {

	private static final EnumSet<Option> ALL_OPTIONS = EnumSet.allOf(Option.class);

	private final Log log;

	public NacosConfigDataLoader(Log log) {
		this.log = log;
	}

	@Override
	public ConfigData load(ConfigDataLoaderContext context,
			NacosConfigDataResource resource) {
		if (context.getBootstrapContext().isRegistered(LoaderInterceptor.class)) {
			LoaderInterceptor interceptor = context.getBootstrapContext()
					.get(LoaderInterceptor.class);
			if (interceptor != null) {
				Binder binder = context.getBootstrapContext().get(Binder.class);
				return interceptor
						.apply(new LoadContext(context, resource, binder, this::doLoad));
			}
		}
		return doLoad(context, resource);
	}

	public ConfigData doLoad(ConfigDataLoaderContext context,
			NacosConfigDataResource resource) {
		try {
			ConfigService configService = getBean(context, ConfigService.class);
			NacosConfigProperties properties = getBean(context,
					NacosConfigProperties.class);

			NacosItemConfig config = resource.getConfig();
			// pull config from nacos
			List<PropertySource<?>> propertySources = pullConfig(configService,
					config.getGroup(), config.getDataId(), config.getSuffix(),
					properties.getTimeout());

			NacosPropertySource propertySource = new NacosPropertySource(propertySources,
					config.getGroup(), config.getDataId(), new Date(),
					config.isRefreshEnabled());

			NacosPropertySourceRepository.collectNacosPropertySource(propertySource);

			if (ALL_OPTIONS.size() == 1) {
				// boot 2.4.2 and prior
				return new ConfigData(propertySources);
			}
			else if (ALL_OPTIONS.size() == 2) {
				// boot 2.4.3 and 2.4.4
				return new ConfigData(propertySources, Option.IGNORE_IMPORTS,
						Option.IGNORE_PROFILES);
			}
			else if (ALL_OPTIONS.size() > 2) {
				// boot 2.4.5+
				return new ConfigData(propertySources, source -> {
					List<Option> options = new ArrayList<>();
					options.add(Option.IGNORE_IMPORTS);
					options.add(Option.IGNORE_PROFILES);
					if (StringUtils.hasText(resource.getProfiles())) {
						options.add(Option.PROFILE_SPECIFIC);
					}
					return Options.of(options.toArray(new Option[0]));
				});
			}
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Error getting properties from nacos: " + resource, e);
			}
			if (!resource.isOptional()) {
				throw new ConfigDataResourceNotFoundException(resource, e);
			}
		}
		return null;
	}

	private List<PropertySource<?>> pullConfig(ConfigService configService,
											   String group, String dataId, String suffix, long timeout)
			throws NacosException, IOException {
		String config = configService.getConfig(dataId, group, timeout);
		return NacosDataParserHandler.getInstance().parseNacosData(dataId, config,
				suffix);
	}

	protected <T> T getBean(ConfigDataLoaderContext context, Class<T> type) {
		if (context.getBootstrapContext().isRegistered(type)) {
			return context.getBootstrapContext().get(type);
		}
		return null;
	}

}
