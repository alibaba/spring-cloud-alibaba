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
package org.springframework.cloud.alicloud.context.sms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(name = "com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest")
@ConditionalOnProperty(value = "spring.cloud.alibaba.deshao.enable.sms", matchIfMissing = true)
public class SmsContextAutoConfiguration {

	@Bean
	public SmsConfigProperties smsConfigProperties(
			AliCloudProperties aliCloudProperties) {

		return new SmsConfigProperties(aliCloudProperties);
	}

	@Bean
	public SmsConfigRegistration smsConfigRegistration(Environment environment,
			SmsConfigProperties smsConfigProperties) {

		return new SmsConfigRegistration(environment, smsConfigProperties);
	}
}