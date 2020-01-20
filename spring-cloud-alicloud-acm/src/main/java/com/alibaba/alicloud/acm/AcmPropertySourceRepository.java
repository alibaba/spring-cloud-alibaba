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

package com.alibaba.alicloud.acm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.alicloud.acm.bootstrap.AcmPropertySource;

import org.springframework.core.env.PropertySource;

/**
 * @author juven.xuxb, 5/17/16.
 * @author yuhuangbin
 */
public class AcmPropertySourceRepository {

	private Map<String, AcmPropertySource> acmPropertySourceMap = new ConcurrentHashMap<>();

	/**
	 * get all acm properties from AcmPropertySourceRepository.
	 * @return list of acm propertysource
	 */
	public List<AcmPropertySource> allAcmPropertySource() {
		List<AcmPropertySource> result = new ArrayList<>();
		result.addAll(this.acmPropertySourceMap.values());
		return result;
	}

	public void collectAcmPropertySource(
			Collection<PropertySource<?>> acmPropertySources) {
		acmPropertySources.forEach(propertySource -> {
			if (propertySource.getClass().isAssignableFrom(AcmPropertySource.class)) {
				AcmPropertySource acmPropertySource = (AcmPropertySource) propertySource;
				this.acmPropertySourceMap.put(getMapKey(acmPropertySource.getDataId(),
						acmPropertySource.getGroup()), acmPropertySource);
			}
		});
	}

	public String getMapKey(String dataId, String group) {
		return String.join(",", dataId, group);
	}

}
