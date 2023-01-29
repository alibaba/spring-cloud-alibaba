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

package com.alibaba.cloud.kubernetes.config.core;

import java.util.Collections;
import java.util.Map;

import com.alibaba.cloud.kubernetes.config.util.Pair;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * {@link PropertySource} that contains a single key-value pair.
 *
 * @author Freeman
 */
public class SinglePairPropertySource extends MapPropertySource {

	public SinglePairPropertySource(String propertySourceName, String key, Object value) {
		super(propertySourceName, Collections.singletonMap(key, value));
	}

	public Pair<String, Object> getSinglePair() {
		Map<String, Object> source = getSource();
		Map.Entry<String, Object> entry = source.entrySet().iterator().next();
		return Pair.of(entry.getKey(), entry.getValue());
	}
}
