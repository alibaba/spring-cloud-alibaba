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

package com.alibaba.cloud.dubbo.registry;

import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;

import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * The interface of ServiceInstanceChange event Listener.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 * @see ServiceInstancesChangedEvent
 * @see Ordered
 * @see ApplicationListener
 */
public interface ServiceInstanceChangeListener
		extends ApplicationListener<ServiceInstancesChangedEvent>, Ordered {

}
