/*
 * Copyright (C) 2019 the original author or authors.
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
/*
 * Copyright (C) 2019 the original author or authors.
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

package org.springframework.cloud.alibaba.fescar.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @author xiaojing
 */

@Configuration
public class FescarRestTemplateAutoConfiguration {

	@Bean
	public FescarRestTemplateInterceptor fescarRestTemplateInterceptor() {
		return new FescarRestTemplateInterceptor();
	}

	@Autowired(required = false)
	private Collection<RestTemplate> restTemplates;

	@Autowired
	private FescarRestTemplateInterceptor fescarRestTemplateInterceptor;

	@PostConstruct
	public void init() {
		if (this.restTemplates != null) {
			for (RestTemplate restTemplate : restTemplates) {
				List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>(
						restTemplate.getInterceptors());
				interceptors.add(this.fescarRestTemplateInterceptor);
				restTemplate.setInterceptors(interceptors);
			}
		}
	}

}
