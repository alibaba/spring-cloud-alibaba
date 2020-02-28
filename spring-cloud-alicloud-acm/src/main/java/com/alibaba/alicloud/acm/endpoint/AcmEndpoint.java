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

package com.alibaba.alicloud.acm.endpoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.alicloud.acm.AcmPropertySourceRepository;
import com.alibaba.alicloud.acm.bootstrap.AcmPropertySource;
import com.alibaba.alicloud.acm.refresh.AcmRefreshHistory;
import com.alibaba.alicloud.context.acm.AcmProperties;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * Created on 01/10/2017.
 *
 * @author juven.xuxb
 */
@Endpoint(id = "acm")
public class AcmEndpoint {

	private final AcmProperties properties;

	private final AcmRefreshHistory refreshHistory;

	private final AcmPropertySourceRepository acmPropertySourceRepository;

	private ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};

	public AcmEndpoint(AcmProperties properties, AcmRefreshHistory refreshHistory,
			AcmPropertySourceRepository acmPropertySourceRepository) {
		this.properties = properties;
		this.refreshHistory = refreshHistory;
		this.acmPropertySourceRepository = acmPropertySourceRepository;
	}

	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>();
		result.put("config", properties);

		Map<String, Object> runtime = new HashMap<>();
		List<AcmPropertySource> all = acmPropertySourceRepository.allAcmPropertySource();

		List<Map<String, Object>> sources = new ArrayList<>();
		for (AcmPropertySource ps : all) {
			Map<String, Object> source = new HashMap<>();
			source.put("dataId", ps.getDataId());
			source.put("lastSynced", dateFormat.get().format(ps.getTimestamp()));
			sources.add(source);
		}
		runtime.put("sources", sources);
		runtime.put("refreshHistory", refreshHistory.getRecords());

		result.put("runtime", runtime);
		return result;
	}

}
