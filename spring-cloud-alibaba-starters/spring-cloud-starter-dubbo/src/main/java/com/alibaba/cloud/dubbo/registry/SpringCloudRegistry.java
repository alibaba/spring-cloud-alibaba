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
import org.apache.dubbo.registry.RegistryFactory;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Dubbo {@link RegistryFactory} uses Spring Cloud Service Registration abstraction, whose
 * protocol is "spring-cloud".
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @deprecated It's a legacy and not recommended implementation, being replacing to be
 * {@link DubboCloudRegistry}
 */
@Deprecated
public class SpringCloudRegistry extends AbstractSpringCloudRegistry {

	private final DubboServiceMetadataRepository dubboServiceMetadataRepository;

	public SpringCloudRegistry(URL url, DiscoveryClient discoveryClient,
			DubboServiceMetadataRepository dubboServiceMetadataRepository,
			DubboMetadataServiceProxy dubboMetadataConfigServiceProxy,
			JSONUtils jsonUtils, DubboGenericServiceFactory dubboGenericServiceFactory,
			ConfigurableApplicationContext applicationContext) {
		super(url, discoveryClient, dubboServiceMetadataRepository,
				dubboMetadataConfigServiceProxy, jsonUtils, dubboGenericServiceFactory,
				applicationContext);
		this.dubboServiceMetadataRepository = dubboServiceMetadataRepository;
	}

	@Override
	protected void doRegister0(URL url) {
		dubboServiceMetadataRepository.exportURL(url);
	}

	@Override
	protected void doUnregister0(URL url) {
		dubboServiceMetadataRepository.unexportURL(url);
	}

}
