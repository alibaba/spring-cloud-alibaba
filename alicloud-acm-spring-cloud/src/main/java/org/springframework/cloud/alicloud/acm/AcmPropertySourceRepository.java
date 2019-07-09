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

package org.springframework.cloud.alicloud.acm;

import org.springframework.cloud.alicloud.acm.bootstrap.AcmPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author juven.xuxb, 5/17/16.
 */
public class AcmPropertySourceRepository {

	private final ApplicationContext applicationContext;

	public AcmPropertySourceRepository(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * get all acm properties from application context
	 * @return
	 */
	public List<AcmPropertySource> getAll() {
		List<AcmPropertySource> result = new ArrayList<>();
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) applicationContext;
		for (PropertySource p : ctx.getEnvironment().getPropertySources()) {
			if (p instanceof AcmPropertySource) {
				result.add((AcmPropertySource) p);
			}
			else if (p instanceof CompositePropertySource) {
				collectAcmPropertySources((CompositePropertySource) p, result);
			}
		}
		return result;
	}

	private void collectAcmPropertySources(CompositePropertySource composite,
			List<AcmPropertySource> result) {
		for (PropertySource p : composite.getPropertySources()) {
			if (p instanceof AcmPropertySource) {
				result.add((AcmPropertySource) p);
			}
			else if (p instanceof CompositePropertySource) {
				collectAcmPropertySources((CompositePropertySource) p, result);
			}
		}
	}
}
