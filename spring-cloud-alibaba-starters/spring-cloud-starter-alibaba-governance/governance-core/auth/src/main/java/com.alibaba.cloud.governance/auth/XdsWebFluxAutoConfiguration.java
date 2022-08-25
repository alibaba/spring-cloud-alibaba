package com.alibaba.cloud.governance.auth;

import com.alibaba.cloud.governance.auth.webflux.AuthWebFluxFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(name = "spring.cloud.governance.auth.enabled",
		matchIfMissing = true)
public class XdsWebFluxAutoConfiguration {

	@Bean
	@ConditionalOnProperty(name = "spring.cloud.governance.auth.enabled",
			matchIfMissing = true)
	public AuthWebFluxFilter authWebFluxFilter() {
		return new AuthWebFluxFilter();
	}

}
