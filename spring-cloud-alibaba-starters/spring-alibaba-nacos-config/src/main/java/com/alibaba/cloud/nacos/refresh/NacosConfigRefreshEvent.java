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

package com.alibaba.cloud.nacos.refresh;

import org.springframework.context.ApplicationEvent;

public class NacosConfigRefreshEvent extends ApplicationEvent {
	String dataId;
	String group;

	private Object event;

	private String eventDesc;

	public NacosConfigRefreshEvent(Object source, Object event, String eventDesc) {
		super(source);
		this.event = event;
		this.eventDesc = eventDesc;
	}

	public Object getEvent() {
		return this.event;
	}

	public String getEventDesc() {
		return this.eventDesc;
	}

	public String getDataId() {
		return dataId;
	}

	void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getGroup() {
		return group;
	}

	void setGroup(String group) {
		this.group = group;
	}
}
