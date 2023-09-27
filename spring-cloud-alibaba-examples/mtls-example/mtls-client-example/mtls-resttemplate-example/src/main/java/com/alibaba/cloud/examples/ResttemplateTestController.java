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

package com.alibaba.cloud.examples;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.mtls.client.MtlsClientSSLContext;
import org.apache.http.impl.client.HttpClientBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ResttemplateTestController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AbstractCertManager abstractCertManager;

	@Autowired
	private MtlsClientSSLContext mtlsSSLContext;

	@GetMapping("/resttemplate/getMvc")
	public String getMvc(HttpServletRequest httpServletRequest) {
		return restTemplate.getForObject("https://mtls-mvc-example/mvc/get",
				String.class);
	}

	@GetMapping("/resttemplate/getWebflux")
	public String getWebflux(HttpServletRequest httpServletRequest) {
		return restTemplate.getForObject("https://mtls-webflux-example/webflux/get",
				String.class);
	}

	@GetMapping("/checkpreload")
	public String checkpreload() {
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(
				HttpClientBuilder.create().setSSLContext(mtlsSSLContext.getSslContext())
						.setSSLHostnameVerifier(mtlsSSLContext.getHostnameVerifier())
						.build()));
		return "success";
	}

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
