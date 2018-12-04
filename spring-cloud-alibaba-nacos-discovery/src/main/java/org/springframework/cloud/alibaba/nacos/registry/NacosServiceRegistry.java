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

package org.springframework.cloud.alibaba.nacos.registry;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.StringUtils;

/**
 * @author xiaojing
 */
public class NacosServiceRegistry implements ServiceRegistry<NacosRegistration> {

	private static Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);

	@Override
	public void register(NacosRegistration registration) {

		if (!registration.isRegisterEnabled()) {
			logger.info("Nacos Registration is disabled...");
			return;
		}
		if (StringUtils.isEmpty(registration.getServiceId())) {
			logger.info("No service to register for nacos client...");
			return;
		}

		NamingService namingService = registration.getNacosNamingService();
		String serviceId = registration.getServiceId();

		Instance instance = new Instance();
		instance.setIp(registration.getHost());
		instance.setPort(registration.getPort());
		instance.setWeight(registration.getRegisterWeight());
		instance.setClusterName(registration.getCluster());
		instance.setMetadata(registration.getMetadata());

		try {
			namingService.registerInstance(serviceId, instance);
			logger.info("nacos registry, {} {}:{} register finished", serviceId,
					instance.getIp(), instance.getPort());
		}
		catch (Exception e) {
			logger.error("nacos registry, {} register failed...{},", serviceId,
					registration.toString(), e);
		}
	}

	@Override
	public void deregister(NacosRegistration registration) {

		logger.info("De-registering from Nacos Server now...");

		if (StringUtils.isEmpty(registration.getServiceId())) {
			logger.info("No dom to de-register for nacos client...");
			return;
		}

		NamingService namingService = registration.getNacosNamingService();
		String serviceId = registration.getServiceId();

		try {
			namingService.deregisterInstance(serviceId, registration.getHost(),
					registration.getPort(), registration.getCluster());
		}
		catch (Exception e) {
			logger.error("ERR_NACOS_DEREGISTER, de-register failed...{},",
					registration.toString(), e);
		}

		logger.info("De-registration finished.");
	}

	@Override
	public void close() {

	}

	@Override
	public void setStatus(NacosRegistration registration, String status) {
		// nacos doesn't support set status of a particular registration.
	}

	@Override
	public <T> T getStatus(NacosRegistration registration) {
		// nacos doesn't support query status of a particular registration.
		return null;
	}

}
