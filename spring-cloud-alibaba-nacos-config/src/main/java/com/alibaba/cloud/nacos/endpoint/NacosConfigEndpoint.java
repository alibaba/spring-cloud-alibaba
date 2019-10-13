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

package com.alibaba.cloud.nacos.endpoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * Endpoint for Nacos, contains config data and refresh history.
 *
 * @author xiaojing
 */
@Endpoint(id = "nacos-config")
public class NacosConfigEndpoint {

	private final NacosConfigProperties properties;

	private final NacosRefreshHistory refreshHistory;

	private ThreadLocal<DateFormat> dateFormat = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

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
