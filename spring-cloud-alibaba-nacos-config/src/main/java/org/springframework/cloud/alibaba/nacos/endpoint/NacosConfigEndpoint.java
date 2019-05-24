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

package org.springframework.cloud.alibaba.nacos.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.cloud.alibaba.nacos.NacosPropertySourceRepository;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySource;
import org.springframework.cloud.alibaba.nacos.refresh.NacosRefreshHistory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoint for Nacos, contains config data and refresh history
 * @author xiaojing
 */
@Endpoint(id = "nacos-config")
public class NacosConfigEndpoint {

	private final NacosConfigProperties properties;

	private final NacosRefreshHistory refreshHistory;

	private ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>(){
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	public NacosConfigEndpoint(NacosConfigProperties properties,
			NacosRefreshHistory refreshHistory) {
		this.properties = properties;
		this.refreshHistory = refreshHistory;
	}

	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>(16);
		result.put("NacosConfigProperties", properties);

		List<NacosPropertySource> all = NacosPropertySourceRepository.getAll();

		List<Map<String, Object>> sources = new ArrayList<>();
		for (NacosPropertySource ps : all) {
			Map<String, Object> source = new HashMap<>(16);
			source.put("dataId", ps.getDataId());
			source.put("lastSynced", dateFormat.get().format(ps.getTimestamp()));
			sources.add(source);
		}
		result.put("Sources", sources);
		result.put("RefreshHistory", refreshHistory.getRecords());

		return result;
	}
}
