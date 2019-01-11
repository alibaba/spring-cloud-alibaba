package org.springframework.cloud.alicloud.context.sms;

import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * @author pbting
 */
public class SmsConfigRegistration {

	private Environment environment;

	private SmsConfigProperties smsConfigProperties;

	public SmsConfigRegistration(Environment environment,
								 SmsConfigProperties smsConfigProperties) {
		this.environment = environment;
		this.smsConfigProperties = smsConfigProperties;
	}

	@PostConstruct
	public void initSmsConfigRegistration() {
		smsConfigProperties.overiideFromEnv(environment);
	}
}