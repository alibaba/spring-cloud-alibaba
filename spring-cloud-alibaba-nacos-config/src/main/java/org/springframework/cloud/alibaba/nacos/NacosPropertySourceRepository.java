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

package org.springframework.cloud.alibaba.nacos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.alibaba.nacos.client.NacosPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author xiaojing
 */
public class NacosPropertySourceRepository {

	private final ApplicationContext applicationContext;

	public NacosPropertySourceRepository(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * @return all nacos properties from application context
	 */
	public List<NacosPropertySource> getAll() {
		List<NacosPropertySource> result = new ArrayList<>();
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) applicationContext;
		for (PropertySource p : ctx.getEnvironment().getPropertySources()) {
			if (p instanceof NacosPropertySource) {
				result.add((NacosPropertySource) p);
			}
			else if (p instanceof CompositePropertySource) {
				collectNacosPropertySources((CompositePropertySource) p, result);
			}
		}
		return result;
	}

	private void collectNacosPropertySources(CompositePropertySource composite,
											 List<NacosPropertySource> result) {
		for (PropertySource p : composite.getPropertySources()) {
			if (p instanceof NacosPropertySource) {
				result.add((NacosPropertySource) p);
			}
			else if (p instanceof CompositePropertySource) {
				collectNacosPropertySources((CompositePropertySource) p, result);
			}
		}
	}
}
