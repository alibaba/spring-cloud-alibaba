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

package com.alibaba.cloud.nacos.annotation;

import java.util.Set;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.common.utils.CollectionUtils;

public abstract class NacosPropertiesKeyListener extends AbstractConfigChangeListener {

	Set<String> interestedKeys;

	Set<String> interestedKeyPrefixes;

	NacosPropertiesKeyListener(Object target) {
		super(target);
	}

	NacosPropertiesKeyListener(Object target, Set<String> interestedKeys) {
		this(target);
		this.interestedKeys = interestedKeys;
	}

	public NacosPropertiesKeyListener(Object target, Set<String> interestedKeys, Set<String> interestedKeyPrefixes) {
		this(target);
		this.interestedKeys = interestedKeys;
		this.interestedKeyPrefixes = interestedKeyPrefixes;
	}

	@Override
	public final void receiveConfigChange(ConfigChangeEvent event) {
		if (CollectionUtils.isNotEmpty(interestedKeys) || CollectionUtils.isNotEmpty(interestedKeyPrefixes)) {
			boolean foundInterested = false;
			for (ConfigChangeItem changeItem : event.getChangeItems()) {
				if (interestedKeys != null && interestedKeys.contains(changeItem.getKey())) {
					foundInterested = true;
					break;
				}
				if (interestedKeyPrefixes != null) {
					for (String prefix : interestedKeyPrefixes) {
						if (changeItem.getKey().startsWith(prefix)) {
							foundInterested = true;
							break;
						}
					}
				}
			}
			if (!foundInterested) {
				return;
			}
		}
		configChanged(event);
	}

	@Override
	public String toString() {
		return "NacosPropertiesKeyListener{" + "interestedKeys=" + interestedKeys + ", interestedKeyPrefixes="
				+ interestedKeyPrefixes + '}' + "@" + hashCode();
	}

	public abstract void configChanged(ConfigChangeEvent event);
}
