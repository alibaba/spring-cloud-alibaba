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

package com.alibaba.cloud.dubbo.bootstrap.event;

import com.alibaba.cloud.dubbo.bootstrap.DubboBootstrapWrapper;

import org.springframework.context.ApplicationEvent;

/**
 * Dubbo microsytem start finish event, every thing is ready.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class DubboBootstrapStartedEvent extends ApplicationEvent {

	/**
	 * Create a new {@code DubboBootstrapStartedEvent}.
	 * @param source the object on which the event initially occurred or with which the
	 * event is associated (never {@code null})
	 */
	public DubboBootstrapStartedEvent(DubboBootstrapWrapper source) {
		super(source);
	}

	@Override
	public DubboBootstrapWrapper getSource() {
		return (DubboBootstrapWrapper) super.getSource();
	}

}
