/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.seata.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @author ZhangZhi
 */
public class SeataRestTemplateInterceptorAfterPropertiesSet implements InitializingBean {

	@Autowired(required = false)
	private Collection<RestTemplate> restTemplates;

	@Autowired
	private SeataRestTemplateInterceptor seataRestTemplateInterceptor;

	@Override
	public void afterPropertiesSet() {
		if (this.restTemplates != null) {
			for (RestTemplate restTemplate : restTemplates) {
				List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(
						restTemplate.getInterceptors());
				interceptors.add(this.seataRestTemplateInterceptor);
				restTemplate.setInterceptors(interceptors);
			}
		}
	}

}
