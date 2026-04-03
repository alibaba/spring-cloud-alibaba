/*
 * Copyright 2013-present the original author or authors.
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

import java.util.Map;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.client.config.impl.ConfigChangeHandler;
import org.jspecify.annotations.Nullable;

import static com.alibaba.cloud.nacos.constants.Constants.NACOS_PROPERTIES_TYPE;
import static com.alibaba.cloud.nacos.constants.Constants.NACOS_YAML_TYPE;
import static com.alibaba.cloud.nacos.constants.Constants.NACOS_YML_TYPE;

public abstract class AbstractConfigChangeListener extends AbstractSharedListener implements TargetRefreshable {

	@Nullable String lastContent;

	@Nullable Object target;

	@Override
	public @Nullable Object getTarget() {
		return target;
	}

	@Override
	public void setTarget(@Nullable Object target) {
		this.target = target;
	}

	public AbstractConfigChangeListener(Object target) {
		this.target = target;
	}

	protected void setLastContent(String lastContent) {
		this.lastContent = lastContent;
	}

	@Override
	public void innerReceive(String dataId, String group, String configInfo) {

		Map<String, ConfigChangeItem> data;
		try {
			data = ConfigChangeHandler.getInstance().parseChangeData(lastContent, configInfo, type(dataId));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		ConfigChangeEvent event = new ConfigChangeEvent(data);
		receiveConfigChange(event);
		lastContent = configInfo;
	}

	private String type(String dataId) {
		if (dataId.endsWith(NACOS_YML_TYPE) || dataId.endsWith(NACOS_YAML_TYPE)) {
			return NACOS_YAML_TYPE;
		}
		return NACOS_PROPERTIES_TYPE;
	}

	abstract void receiveConfigChange(ConfigChangeEvent event);
}
