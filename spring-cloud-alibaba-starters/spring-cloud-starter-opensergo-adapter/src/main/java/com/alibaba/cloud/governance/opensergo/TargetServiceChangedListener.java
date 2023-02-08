/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.opensergo;

import com.alibaba.cloud.commons.governance.event.TargetServiceChangedEvent;

import org.springframework.context.ApplicationListener;

/**
 * Subscribe OpenSergo configuration when provider service changed.
 *
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 * @since 2.2.10-RC1
 */
public class TargetServiceChangedListener
		implements ApplicationListener<TargetServiceChangedEvent> {

	private OpenSergoTrafficExchanger openSergoTrafficExchanger;

	private OpenSergoConfigProperties openSergoConfigProperties;

	public TargetServiceChangedListener(
			OpenSergoConfigProperties openSergoConfigProperties,
			OpenSergoTrafficExchanger openSergoTrafficExchanger) {
		this.openSergoConfigProperties = openSergoConfigProperties;
		this.openSergoTrafficExchanger = openSergoTrafficExchanger;
	}

	@Override
	public void onApplicationEvent(TargetServiceChangedEvent targetServiceChangedEvent) {
		Object source = targetServiceChangedEvent.getSource();
		if (source instanceof String) {
			String targetService = (String) targetServiceChangedEvent.getSource();
			openSergoTrafficExchanger.subscribeTrafficRouterConfig(
					openSergoConfigProperties.getNamespace(), targetService);
		}
	}

}
