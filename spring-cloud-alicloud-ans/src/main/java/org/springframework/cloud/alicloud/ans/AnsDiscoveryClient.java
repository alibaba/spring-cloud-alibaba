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

package org.springframework.cloud.alicloud.ans;

import java.util.*;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.alibaba.ans.core.NamingService;
import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;

/**
 * @author xiaolongzuo
 */
public class AnsDiscoveryClient implements DiscoveryClient {

	public static final String DESCRIPTION = "Spring Cloud ANS Discovery Client";

	@Override
	public String description() {
		return DESCRIPTION;
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		try {
			List<Host> hosts = NamingService.getHosts(serviceId);
			return hostToServiceInstanceList(hosts, serviceId);
		}
		catch (Exception e) {
			throw new RuntimeException(
					"Can not get hosts from ans server. serviceId: " + serviceId, e);
		}
	}

	private static ServiceInstance hostToServiceInstance(Host host, String serviceId) {
		AnsServiceInstance ansServiceInstance = new AnsServiceInstance();
		ansServiceInstance.setHost(host.getIp());
		ansServiceInstance.setPort(host.getPort());
		ansServiceInstance.setServiceId(serviceId);
		Map<String, String> metadata = new HashMap<String, String>(5);
		metadata.put("appUseType", host.getAppUseType());
		metadata.put("site", host.getSite());
		metadata.put("unit", host.getUnit());
		metadata.put("doubleWeight", "" + host.getDoubleWeight());
		metadata.put("weight", "" + host.getWeight());
		ansServiceInstance.setMetadata(metadata);

		return ansServiceInstance;
	}

	private static List<ServiceInstance> hostToServiceInstanceList(List<Host> hosts,
			String serviceId) {
		List<ServiceInstance> result = new ArrayList<ServiceInstance>(hosts.size());
		for (Host host : hosts) {
			result.add(hostToServiceInstance(host, serviceId));
		}
		return result;
	}

	@Override
	public List<String> getServices() {

		Set<String> doms = NamingService.getDomsSubscribed();
		List<String> result = new LinkedList<>();
		for (String service : doms) {
			result.add(service);
		}
		return result;
	}

}
