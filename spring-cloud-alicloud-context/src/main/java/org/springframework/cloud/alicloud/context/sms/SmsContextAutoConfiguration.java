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