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

package com.alibaba.alicloud.sms.config;

import com.alibaba.alicloud.context.AliCloudProperties;
import com.alibaba.alicloud.context.sms.SmsProperties;
import com.alibaba.alicloud.sms.ISmsService;
import com.alibaba.alicloud.sms.SmsInitializerEventListener;
import com.alibaba.alicloud.sms.SmsServiceImpl;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pbting
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass(SendSmsRequest.class)
@ConditionalOnProperty(value = "spring.cloud.alicloud.sms.enable", matchIfMissing = true)
public class SmsAutoConfiguration {

	@Bean
	public SmsServiceImpl smsService(AliCloudProperties aliCloudProperties,
			SmsProperties smsProperties) {
		return new SmsServiceImpl(aliCloudProperties, smsProperties);
	}

	@Bean
	public SmsInitializerEventListener smsInitializePostListener(
			SmsProperties smsProperties, ISmsService smsService) {
		return new SmsInitializerEventListener(smsProperties, smsService);
	}

}
