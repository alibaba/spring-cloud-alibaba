package org.springframework.cloud.alicloud.sms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.context.sms.SmsConfigProperties;
import org.springframework.cloud.alicloud.sms.ISmsService;
import org.springframework.cloud.alicloud.sms.SmsInitializerEventListener;
import org.springframework.cloud.alicloud.sms.SmsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;

/**
 * @author pbting
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(value = SendSmsRequest.class)
@ConditionalOnProperty(value = "spring.cloud.alibaba.deshao.enable.sms", matchIfMissing = true)
public class SmsAutoConfiguration {

	@Bean
	public SmsServiceImpl smsService(SmsConfigProperties smsConfigProperties) {
		return new SmsServiceImpl(smsConfigProperties);
	}

	@Bean
	public SmsInitializerEventListener smsInitializePostListener(
			SmsConfigProperties msConfigProperties, ISmsService smsService) {
		return new SmsInitializerEventListener(msConfigProperties, smsService);
	}
}