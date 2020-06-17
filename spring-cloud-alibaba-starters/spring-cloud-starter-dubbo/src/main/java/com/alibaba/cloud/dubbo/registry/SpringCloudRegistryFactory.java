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

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import com.alibaba.cloud.dubbo.service.DubboMetadataServiceProxy;
import com.alibaba.cloud.dubbo.util.JSONUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import static java.lang.System.getProperty;

/**
 * Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose
 * protocol is "spring-cloud".
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RegistryFactory
 * @see SpringCloudRegistry
 */
public class SpringCloudRegistryFactory extends AbstractRegistryFactory {

	/**
	 * Spring Cloud Protocol.
	 */
	public static String PROTOCOL = "spring-cloud";

	/**
	 * Spring Cloud Address.
	 */
	public static String ADDRESS = "localhost";

	private static String SERVICES_LOOKUP_SCHEDULER_THREAD_NAME_PREFIX = getProperty(
			"dubbo.services.lookup.scheduler.thread.name.prefix ",
			"dubbo-services-lookup-");

	private static ConfigurableApplicationContext applicationContext;

	private DiscoveryClient discoveryClient;

	private DubboServiceMetadataRepository dubboServiceMetadataRepository;

	private DubboMetadataServiceProxy dubboMetadataConfigServiceProxy;

	private JSONUtils jsonUtils;

	private DubboGenericServiceFactory dubboGenericServiceFactory;

	public SpringCloudRegistryFactory() {
	}

	public static void setApplicationContext(
			ConfigurableApplicationContext applicationContext) {
		SpringCloudRegistryFactory.applicationContext = applicationContext;
	}

	protected void init() {
		this.discoveryClient = applicationContext.getBean(DiscoveryClient.class);
		this.dubboServiceMetadataRepository = applicationContext
				.getBean(DubboServiceMetadataRepository.class);
		this.dubboMetadataConfigServiceProxy = applicationContext
				.getBean(DubboMetadataServiceProxy.class);
		this.jsonUtils = applicationContext.getBean(JSONUtils.class);
		this.dubboGenericServiceFactory = applicationContext
				.getBean(DubboGenericServiceFactory.class);
	}

	@Override
	public Registry createRegistry(URL url) {
		init();
		return new SpringCloudRegistry(url, discoveryClient,
				dubboServiceMetadataRepository, dubboMetadataConfigServiceProxy,
				jsonUtils, dubboGenericServiceFactory, applicationContext);
	}

}
