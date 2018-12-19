/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel.datasource;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;

/**
 * {@link NacosDataSource} now is not support ak、sk，namespace and endpoint. This class may
 * be delete when {@link NacosDataSource} support commercialized
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourceWithAuthorization<T> extends AbstractDataSource<String, T> {

	private static final int DEFAULT_TIMEOUT = 3000;

	private final ExecutorService pool = new ThreadPoolExecutor(1, 1, 0,
			TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1),
			new NamedThreadFactory("sentinel-nacos-auth-ds-update"),
			new ThreadPoolExecutor.DiscardOldestPolicy());

	private final Listener configListener;
	private final Properties properties;
	private final String dataId;
	private final String groupId;

	private ConfigService configService = null;

	public NacosDataSourceWithAuthorization(final Properties properties,
			final String groupId, final String dataId, Converter<String, T> parser) {
		super(parser);
		if (StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
			throw new IllegalArgumentException(String
					.format("Bad argument: groupId=[%s], dataId=[%s]", groupId, dataId));
		}
		this.groupId = groupId;
		this.dataId = dataId;
		this.properties = properties;
		this.configListener = new Listener() {
			@Override
			public Executor getExecutor() {
				return pool;
			}

			@Override
			public void receiveConfigInfo(final String configInfo) {
				RecordLog.info(String.format(
						"[NacosDataSourceWithAuthorization] New property value received for %s",
						properties.toString()));
				T newValue = NacosDataSourceWithAuthorization.this.parser
						.convert(configInfo);
				// Update the new value to the property.
				getProperty().updateValue(newValue);
			}
		};
		initNacosListener();
		loadInitialConfig();
	}

	private void loadInitialConfig() {
		try {
			T newValue = loadConfig();
			if (newValue == null) {
				RecordLog.warn(
						"[NacosDataSourceWithAuthorization] WARN: initial config is null, you may have to check your data source");
			}
			getProperty().updateValue(newValue);
		}
		catch (Exception ex) {
			RecordLog.warn(
					"[NacosDataSourceWithAuthorization] Error when loading initial config",
					ex);
		}
	}

	private void initNacosListener() {
		try {
			this.configService = NacosFactory.createConfigService(properties);
			// Add config listener.
			configService.addListener(dataId, groupId, configListener);
		}
		catch (Exception e) {
			RecordLog.warn(
					"[NacosDataSourceWithAuthorization] Error occurred when initializing Nacos data source",
					e);
			e.printStackTrace();
		}
	}

	@Override
	public String readSource() throws Exception {
		if (configService == null) {
			throw new IllegalStateException(
					"Nacos config service has not been initialized or error occurred");
		}
		return configService.getConfig(dataId, groupId, DEFAULT_TIMEOUT);
	}

	@Override
	public void close() {
		if (configService != null) {
			configService.removeListener(dataId, groupId, configListener);
		}
		pool.shutdownNow();
	}
}
