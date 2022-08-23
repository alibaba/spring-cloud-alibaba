package com.alibaba.cloud.governance.auth;

import com.alibaba.cloud.governance.auth.webmvc.AuthWebInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "spring.cloud.governance.auth.enabled",
		matchIfMissing = true)
public class XdsWebAutoConfiguration {

	@Bean
	@ConditionalOnProperty(name = "spring.cloud.governance.auth.enabled")
	public AuthWebInterceptor authWebInterceptor() {
		return new AuthWebInterceptor();
	}

}
