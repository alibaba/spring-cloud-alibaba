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

package com.alibaba.cloud.nacos.registry.retry;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.common.timer.Timeout;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

public class FailedRegisteredTask extends AbstractRetryTask {

	private static final String NAME = "retry register";

	private static final int DEFAULT_RETRY_TIMES = 3;

	private static final long DEFAULT_RETRY_PERIOD = 5000;

	private NacosDiscoveryProperties properties;

	public FailedRegisteredTask(NacosRegistration registration,
			NacosServiceRegistry registry, NacosDiscoveryProperties properties) {
		this(registration, registry, NAME);
		this.properties = properties;
	}

	public FailedRegisteredTask(NacosRegistration registration,
			NacosServiceRegistry registry, String taskName) {
		super(registration, registry, NAME);
	}

	@Override
	protected void doRetry(Registration registration, ServiceRegistry registry,
			Timeout timeout) throws Exception {
		((NacosServiceRegistry) registry).doRegister(registration);
		((NacosServiceRegistry) registry).removeFailedRegisteredTask(registration);
	}

	@Override
	protected int getRetryTimes() {

		int retryTimes = DEFAULT_RETRY_TIMES;
		if (properties != null && properties.getRetryTimes() != null) {
			retryTimes = properties.getRetryTimes().intValue();
		}
		return retryTimes;
	}

	@Override
	protected long getRetryPeriod() {
		long retryPeriod = DEFAULT_RETRY_PERIOD;
		if (properties != null && properties.getRetryPeriod() != null) {
			retryPeriod = properties.getRetryPeriod().longValue();
		}
		return retryPeriod;
	}

}
