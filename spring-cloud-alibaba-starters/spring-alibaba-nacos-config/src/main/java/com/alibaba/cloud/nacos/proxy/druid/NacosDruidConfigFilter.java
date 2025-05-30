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

package com.alibaba.cloud.nacos.proxy.druid;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.utils.StringUtils;
import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;

public class NacosDruidConfigFilter extends FilterAdapter {

	private final static String groupMatched = "nacos-datasource";

	private final Set<String> idempotentControl = new HashSet<>();

	private final String proxyDataId;

	public NacosDruidConfigFilter(String proxyDataId) {
		this.proxyDataId = proxyDataId;
	}

	@Override
	public void init(final DataSourceProxy dataSourceProxy) {

		if (!(dataSourceProxy instanceof DruidDataSource)) {
			return;
		}
		String name = StringUtils.isNotBlank(dataSourceProxy.getName()) ? dataSourceProxy.getName()
				: String.valueOf(dataSourceProxy.hashCode());
		if (idempotentControl.contains(name)) {
			return;
		}

		DruidDataSource druidDataSource = ((DruidDataSource) dataSourceProxy);
		ConfigService configService = NacosConfigManager.getInstance().getConfigService();

		try {
			String druidProperties = configService.getConfig(proxyDataId, groupMatched, 3000L);
			Properties propertiesNew = convert(druidProperties);
			druidDataSource.configFromProperties(propertiesNew);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		//register listeners
		try {
			configService.addListener(proxyDataId, groupMatched, new AbstractListener() {
				@Override
				public void receiveConfigInfo(String configInfo) {
					try {
						Properties propertiesNew = convert(configInfo);

						//refresh
						((DruidDataSource) dataSourceProxy).configFromProperties(propertiesNew);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
		catch (NacosException e) {
			throw new RuntimeException(e);
		}
		idempotentControl.add(name);
	}

	private static final String druidPrefix = "spring.datasource.druid.";

	private static final String datasourcePrefix = "spring.datasource.";

	private static Properties convert(String config) throws Exception {

		Properties properties = new Properties();
		properties.load(new StringReader(config));

		Properties propertiesNew = new Properties();
		Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Object, Object> entry = iterator.next();
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();

			if (key.startsWith(druidPrefix)) {
				propertiesNew.put(key.replace(druidPrefix, "druid."), value);
			}
			else if (key.startsWith(datasourcePrefix)) {
				propertiesNew.put(key.replace(datasourcePrefix, "druid."), value);
			}
		}

		return propertiesNew;
	}
}
