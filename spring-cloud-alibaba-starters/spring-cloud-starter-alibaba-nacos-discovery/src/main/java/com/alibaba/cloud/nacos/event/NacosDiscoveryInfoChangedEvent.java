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

package com.alibaba.cloud.nacos.event;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;

import org.springframework.context.ApplicationEvent;

/**
 * @author yuhuangbin
 */
public class NacosDiscoveryInfoChangedEvent extends ApplicationEvent {

	public NacosDiscoveryInfoChangedEvent(
			NacosDiscoveryProperties nacosDiscoveryProperties) {
		super(nacosDiscoveryProperties);
	}

	@Override
	public NacosDiscoveryProperties getSource() {
		return (NacosDiscoveryProperties) super.getSource();
	}

}
