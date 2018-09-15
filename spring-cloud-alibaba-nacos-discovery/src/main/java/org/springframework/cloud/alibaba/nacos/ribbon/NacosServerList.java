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

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.registry.NacosRegistration;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.nacos.api.naming.pojo.Instance;

/**
 * @author xiaojing
 */
public class NacosServerList implements ServerList<Server> {

	@Autowired
	private NacosRegistration registration;

	private String service;

	public NacosServerList() {
	}

	public NacosServerList(String service) {
		this.service = service;
	}

	@Override
	public List<Server> getInitialListOfServers() {
		try {
			List<Instance> instances = registration.getNacosNamingService().selectInstances(service,true);
			return hostsToServerList(instances);
		}
		catch (Exception e) {
			throw new IllegalStateException("Can not get nacos hosts, service=" + service, e);
		}
	}

	@Override
	public List<Server> getUpdatedListOfServers() {
		return getInitialListOfServers();
	}

	private Server hostToServer(Instance instance) {
		Server server = new Server(instance.getIp(), instance.getPort());
		return server;
	}

	private List<Server> hostsToServerList(List<Instance> instances) {
		List<Server> result = new ArrayList<Server>(instances.size());
		for (Instance instance : instances) {
			if (instance.isHealthy()) {
				result.add(hostToServer(instance));
			}
		}

		return result;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

}
