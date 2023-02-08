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

import com.alibaba.cloud.governance.auth.listener.AuthListener;
import com.alibaba.cloud.governance.auth.repository.AuthRepository;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
@Configuration(proxyBeanMethods = false)
// We need to auto config the class before spring cloud alibaba istio module, to prevent
// event publisher hang permanently.
@AutoConfigureOrder(AuthenticationAutoConfiguration.AUTH_AUTO_CONFIG_ORDER)

public class AuthenticationAutoConfiguration {

	/**
	 * Order of auth auto config.
	 */
	public static final int AUTH_AUTO_CONFIG_ORDER = 9;

	@Bean
	@ConditionalOnMissingBean
	public AuthRepository authRepository() {
		return new AuthRepository();
	}

	@Bean
	public AuthListener authListener(AuthRepository authRepository) {
		return new AuthListener(authRepository);
	}

}
