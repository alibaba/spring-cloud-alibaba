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

package com.alibaba.cloud.nacos.refresh;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationEvent;

public class NacosConfigRefreshEvent extends ApplicationEvent {

	@Nullable String dataId;

	@Nullable String group;

	private @Nullable Object event;

	private String eventDesc;

	public NacosConfigRefreshEvent(Object source, @Nullable Object event, String eventDesc) {
		super(source);
		this.event = event;
		this.eventDesc = eventDesc;
	}

	public @Nullable Object getEvent() {
		return this.event;
	}

	public String getEventDesc() {
		return this.eventDesc;
	}

	public @Nullable String getDataId() {
		return dataId;
	}

	void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public @Nullable String getGroup() {
		return group;
	}

	void setGroup(String group) {
		this.group = group;
	}
}
