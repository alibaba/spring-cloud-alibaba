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

package com.alibaba.cloud.appactive.consumer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import io.appactive.support.lang.CollectionUtils;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
@Configuration
public class ConsumerAutoConfig {

	private static final Logger logger = LogUtil.getLogger();

	@Autowired
	ApplicationContext context;

	@Autowired(required = false)
	RestTemplate restTemplate;

	@Autowired
	private ObjectFactory<HttpMessageConverters> messageConverters;

	@Bean
	@ConditionalOnMissingBean
	public Decoder appActiveFeignDecoder() {
		return new OptionalDecoder(
				new ResponseEntityDecoder(new SpringDecoder(this.messageConverters)));
	}

	@Bean
	public BeanPostProcessor feignDecoderPostProcessor() {
		return new FeignDecoderPostProcessor(context);
	}

	@Bean
	public RequestInterceptor routerIdTransmissionRequestInterceptor() {
		return new RouterIdTransmissionRequestInterceptor();
	}

	@PostConstruct
	public void init() {
		if (restTemplate != null) {
			List<ClientHttpRequestInterceptor> interceptors = restTemplate
					.getInterceptors();
			if (CollectionUtils.isEmpty(interceptors)) {
				interceptors = new ArrayList<>();
			}
			interceptors.add(new ReqResInterceptor());
			logger.info(
					"ConsumerAutoConfig adding interceptor for restTemplate[{}]......",
					restTemplate.getClass());
			restTemplate.setInterceptors(interceptors);
		}
	}

}
