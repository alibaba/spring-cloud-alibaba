package com.alibaba.cloud.governance.auth;

import com.alibaba.cloud.governance.auth.webmvc.AuthWebInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

public class XdsWebMvcConfigurer implements WebMvcConfigurer {

	@Autowired
	private Optional<AuthWebInterceptor> authWebInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (!authWebInterceptor.isPresent()) {
			return;
		}
		registry.addInterceptor(authWebInterceptor.get());
	}

}
