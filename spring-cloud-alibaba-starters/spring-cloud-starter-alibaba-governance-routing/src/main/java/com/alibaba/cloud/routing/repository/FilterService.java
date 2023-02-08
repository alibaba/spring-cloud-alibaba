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
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public HashSet<String> getDefinitionFeignService(int size) {
		String[] allBeanNames = applicationContext.getBeanDefinitionNames();
		HashSet<String> serviceSet = new HashSet<>(size);
		for (String beanName : allBeanNames) {
			if (beanName.contains(FEIGN_CLIENT_BEAN_SPECIFICATION)
					&& !beanName.startsWith(FEIGN_CLIENT_BEAN_DEFAULT)) {
				String feignName = beanName.substring(0,
						beanName.indexOf(FEIGN_CLIENT_BEAN_SPECIFICATION));
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
