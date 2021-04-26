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

package com.alibaba.cloud.dubbo.servicediscovery;

import java.util.Collections;
import java.util.Set;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.AbstractServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.client.event.listener.ServiceInstancesChangedListener;

/**
 * Spring Cloud Service Discvoery has no function at all.
 *
 * @author <a href="ppzzyy11:q15138956968@gmail.com">ppzzyy11</a>
 */
public class SpringCloudServiceDiscovery extends AbstractServiceDiscovery {

	public SpringCloudServiceDiscovery() {
	}

	@Override
	public void doRegister(ServiceInstance serviceInstance) {
	}

	@Override
	public void doUpdate(ServiceInstance serviceInstance) {
	}

	@Override
	public void initialize(URL registryURL) throws Exception {
	}

	@Override
	public void destroy() throws Exception {
	}

	@Override
	public void unregister(ServiceInstance serviceInstance) throws RuntimeException {
	}

	@Override
	public Set<String> getServices() {
		return Collections.emptySet();

	}

	@Override
	public URL getUrl() {
		return new URL("", "", 0);
	}

	@Override
	public void addServiceInstancesChangedListener(
			ServiceInstancesChangedListener listener)
			throws NullPointerException, IllegalArgumentException {
	}

}
