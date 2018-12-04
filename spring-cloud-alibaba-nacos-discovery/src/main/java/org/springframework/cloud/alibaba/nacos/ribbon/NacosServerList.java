/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.nacos.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.nacos.api.naming.pojo.Instance;

/**
 * @author xiaojing
 * @author renhaojun
 */
public class NacosServerList extends AbstractServerList<NacosServer> {

	@Autowired
	private NacosDiscoveryProperties discoveryProperties;

	private String serviceId;

	public NacosServerList() {
	}

	public NacosServerList(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public List<NacosServer> getInitialListOfServers() {
		return getServers();
	}

	@Override
	public List<NacosServer> getUpdatedListOfServers() {
		return getServers();
	}

	private List<NacosServer> getServers() {
		try {
			List<Instance> instances = discoveryProperties.namingServiceInstance()
					.selectInstances(serviceId, true);
			return instancesToServerList(instances);
		}
		catch (Exception e) {
			throw new IllegalStateException(
					"Can not get service instances from nacos, serviceId=" + serviceId,
					e);
		}
	}

	private List<NacosServer> instancesToServerList(List<Instance> instances) {
		List<NacosServer> result = new ArrayList<>(instances.size());
		for (Instance instance : instances) {
			result.add(new NacosServer(instance));
		}

		return result;
	}

	public String getServiceId() {
		return serviceId;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {
		this.serviceId = iClientConfig.getClientName();
	}
}
