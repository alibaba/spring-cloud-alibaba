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

package com.alibaba.cloud.governance.auth;

import com.alibaba.cloud.governance.auth.validator.AuthValidator;
import com.alibaba.cloud.governance.auth.webmvc.AuthWebInterceptor;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(AuthenticationAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "spring.cloud.governance.auth.enabled",
		matchIfMissing = true)
public class XdsWebAutoConfiguration {

	@Bean
	public AuthWebInterceptor authWebInterceptor(AuthValidator authValidator) {
		return new AuthWebInterceptor(authValidator);
	}

	@Bean
	public XdsWebMvcConfigurer xdsWebMvcConfigurer() {
		return new XdsWebMvcConfigurer();
	}

}
