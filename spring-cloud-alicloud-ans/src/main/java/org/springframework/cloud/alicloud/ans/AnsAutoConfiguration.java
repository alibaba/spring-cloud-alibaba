/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alicloud.ans;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.ans.migrate.MigrateOnConditionMissingClass;
import org.springframework.cloud.alicloud.ans.registry.AnsAutoServiceRegistration;
import org.springframework.cloud.alicloud.ans.registry.AnsRegistration;
import org.springframework.cloud.alicloud.ans.registry.AnsServiceRegistry;
import org.springframework.cloud.alicloud.context.ans.AnsProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaolongzuo
 */
@Configuration
@EnableConfigurationProperties
@Conditional(MigrateOnConditionMissingClass.class)
@ConditionalOnClass(name = "org.springframework.boot.web.context.WebServerInitializedEvent")
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@ConditionalOnAnsEnabled
@AutoConfigureAfter(AutoServiceRegistrationAutoConfiguration.class)
public class AnsAutoConfiguration {

	@Bean
	public AnsServiceRegistry ansServiceRegistry() {
		return new AnsServiceRegistry();
	}

	@Bean
	@ConditionalOnBean(AutoServiceRegistrationProperties.class)
	@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
	public AnsRegistration ansRegistration(AnsProperties ansProperties,
			ApplicationContext applicationContext) {
		return new AnsRegistration(ansProperties, applicationContext);
	}

	@Bean
	@ConditionalOnBean(AutoServiceRegistrationProperties.class)
	@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
	public AnsAutoServiceRegistration ansAutoServiceRegistration(
			AnsServiceRegistry registry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			AnsRegistration registration) {
		return new AnsAutoServiceRegistration(registry, autoServiceRegistrationProperties,
				registration);
	}

}
