/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.repository;

import java.util.HashSet;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public class FilterService implements ApplicationContextAware {

	/**
	 * Feign bean name suffix.
	 */
	public static final String FEIGN_CLIENT_BEAN_SPECIFICATION = ".FeignClientSpecification";

	/**
	 * WebClient bean name suffix.
	 */
	public static final String REACTIVE_CLIENT_BEAN_SPECIFICATION = ".LoadBalancerClientSpecification";

	/**
	 * Feign bean name prefix.
	 */
	public static final String FEIGN_CLIENT_BEAN_DEFAULT = "default.";

	/**
	 * Feign bean name start char.
	 */
	public static final String FEIGN_CLIENT_BEAN_START = "${";

	/**
	 * Feign bean name end char.
	 */
	public static final String FEIGN_CLIENT_BEAN_END = "}";

	/**
	 * Spring bean Container.
	 */
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		FilterService.applicationContext = applicationContext;
	}

	/**
	 * Find service.
	 * @param size params
	 * @return service name set
	 */
	public HashSet<String> getDefinitionService(int size) {

		HashSet<String> serviceSet = new HashSet<>();

		HashSet<String> serviceSet4Feign = getDefinitionFeignService(size);
		HashSet<String> serviceSet4WebClient = getDefinitionWebClientService(size);

		serviceSet.addAll(serviceSet4Feign);
		serviceSet.addAll(serviceSet4WebClient);

		return serviceSet;
	}

	/**
	 * Find the Defined Diversity Service Provider for Feign Client.
	 * @param size params
	 * @return service name set
	 */
	private static HashSet<String> getDefinitionFeignService(int size) {

		return findDefinitionService(size, FEIGN_CLIENT_BEAN_SPECIFICATION);
	}

	/**
	 * Find the Defined Diversity Service Provider For WebClient.
	 * @param size params
	 * @return service name set
	 */
	private static HashSet<String> getDefinitionWebClientService(int size) {

		return findDefinitionService(size, REACTIVE_CLIENT_BEAN_SPECIFICATION);
	}

	private static HashSet<String> findDefinitionService(int size,
			String beanSpecification) {
		String[] allBeanNames = applicationContext.getBeanDefinitionNames();
		HashSet<String> serviceSet = new HashSet<>(size);
		for (String beanName : allBeanNames) {
			if (beanName.contains(beanSpecification)
					&& !beanName.startsWith(beanSpecification)) {
				String feignName = beanName.substring(0,
						beanName.indexOf(beanSpecification));
				if (feignName.startsWith(FEIGN_CLIENT_BEAN_START)) {
					String resolveFeignName = feignName.replace(FEIGN_CLIENT_BEAN_START,
							"");
					resolveFeignName = resolveFeignName.replace(FEIGN_CLIENT_BEAN_END,
							"");
					feignName = resolveFeignName;
				}
				serviceSet.add(feignName);
			}
		}

		return serviceSet;
	}

}
