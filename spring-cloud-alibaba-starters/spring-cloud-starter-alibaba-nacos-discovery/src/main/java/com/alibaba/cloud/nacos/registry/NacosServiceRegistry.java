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

package com.alibaba.cloud.nacos.registry;

import java.util.List;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.StringUtils;

import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * @author xiaojing
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @author <a href="mailto:78552423@qq.com">eshun</a>
 */
public class NacosServiceRegistry implements ServiceRegistry<Registration> {

	private static final Logger log = LoggerFactory.getLogger(NacosServiceRegistry.class);

	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	public NacosServiceRegistry(NacosDiscoveryProperties nacosDiscoveryProperties) {
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
	}

	@Override
	public void register(Registration registration) {

		if (StringUtils.isEmpty(registration.getServiceId())) {
			log.warn("No service to register for nacos client...");
			return;
		}

		NamingService namingService = namingService();
		String serviceId = registration.getServiceId();
		String group = nacosDiscoveryProperties.getGroup();

		Instance instance = getNacosInstanceFromRegistration(registration);

		try {
			namingService.registerInstance(serviceId, group, instance);
			log.info("nacos registry, {} {} {}:{} register finished", group, serviceId,
					instance.getIp(), instance.getPort());
		}
		catch (Exception e) {
			log.error("nacos registry, {} register failed...{},", serviceId,
					registration.toString(), e);
			// rethrow a RuntimeException if the registration is failed.
			// issue : https://github.com/alibaba/spring-cloud-alibaba/issues/1132
			rethrowRuntimeException(e);
		}
	}

	@Override
	public void deregister(Registration registration) {

		log.info("De-registering from Nacos Server now...");

		if (StringUtils.isEmpty(registration.getServiceId())) {
			log.warn("No dom to de-register for nacos client...");
			return;
		}

		NamingService namingService = namingService();
		String serviceId = registration.getServiceId();
		String group = nacosDiscoveryProperties.getGroup();

		try {
			namingService.deregisterInstance(serviceId, group, registration.getHost(),
					registration.getPort(), nacosDiscoveryProperties.getClusterName());
		}
		catch (Exception e) {
			log.error("ERR_NACOS_DEREGISTER, de-register failed...{},",
					registration.toString(), e);
		}

		log.info("De-registration finished.");
	}

	@Override
	public void close() {

	}

	@Override
	public void setStatus(Registration registration, String status) {

		if (!status.equalsIgnoreCase("UP") && !status.equalsIgnoreCase("DOWN")) {
			log.warn("can't support status {},please choose UP or DOWN", status);
			return;
		}

		String serviceId = registration.getServiceId();

		Instance instance = getNacosInstanceFromRegistration(registration);

		if (status.equalsIgnoreCase("DOWN")) {
			instance.setEnabled(false);
		}
		else {
			instance.setEnabled(true);
		}

		try {
			nacosDiscoveryProperties.namingMaintainServiceInstance()
					.updateInstance(serviceId, instance);
		}
		catch (Exception e) {
			throw new RuntimeException("update nacos instance status fail", e);
		}

	}

	@Override
	public Object getStatus(Registration registration) {

		String serviceName = registration.getServiceId();
		try {
			List<Instance> instances = nacosDiscoveryProperties.namingServiceInstance()
					.getAllInstances(serviceName);
			for (Instance instance : instances) {
				if (instance.getIp().equalsIgnoreCase(nacosDiscoveryProperties.getIp())
						&& instance.getPort() == nacosDiscoveryProperties.getPort()) {
					return instance.isEnabled() ? "UP" : "DOWN";
				}
			}
		}
		catch (Exception e) {
			log.error("get all instance of {} error,", serviceName, e);
		}
		return null;
	}

	private Instance getNacosInstanceFromRegistration(Registration registration) {
		Instance instance = new Instance();
		instance.setIp(registration.getHost());
		instance.setPort(registration.getPort());
		instance.setWeight(nacosDiscoveryProperties.getWeight());
		instance.setClusterName(nacosDiscoveryProperties.getClusterName());
		instance.setEnabled(nacosDiscoveryProperties.isInstanceEnabled());
		instance.setMetadata(registration.getMetadata());
		instance.setEphemeral(nacosDiscoveryProperties.isEphemeral());
		return instance;
	}

	private NamingService namingService() {
		return nacosDiscoveryProperties.namingServiceInstance();
	}

}
