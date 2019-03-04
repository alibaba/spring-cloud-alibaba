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

package org.springframework.cloud.alicloud.ans.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author xiaolongzuo
 * @author pbting
 */
public class AnsAutoServiceRegistration
		extends AbstractAutoServiceRegistration<AnsRegistration> {
	private static final Logger log = LoggerFactory
			.getLogger(AnsAutoServiceRegistration.class);

	private AnsRegistration registration;

	public AnsAutoServiceRegistration(ServiceRegistry<AnsRegistration> serviceRegistry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			AnsRegistration registration) {
		super(serviceRegistry, autoServiceRegistrationProperties);
		this.registration = registration;
	}

	@Deprecated
	public void setPort(int port) {
		getPort().set(port);
	}

	@Override
	protected AnsRegistration getRegistration() {
		if (this.registration.getPort() < 0 && this.getPort().get() > 0) {
			this.registration.setPort(this.getPort().get());
		}
		Assert.isTrue(this.registration.getPort() > 0, "service.port has not been set");
		return this.registration;
	}

	@Override
	protected AnsRegistration getManagementRegistration() {
		return null;
	}

	@Override
	protected void register() {
		if (!this.registration.getAnsProperties().isRegisterEnabled()) {
			log.debug("Registration disabled.");
			return;
		}
		if (this.registration.getPort() < 0) {
			this.registration.setPort(getPort().get());
		}
		super.register();
	}

	@Override
	protected void registerManagement() {
		if (!this.registration.getAnsProperties().isRegisterEnabled()) {
			return;
		}
		super.registerManagement();

	}

	@Override
	protected Object getConfiguration() {
		return this.registration.getAnsProperties();
	}

	@Override
	protected boolean isEnabled() {
		return this.registration.getAnsProperties().isRegisterEnabled();
	}

	@Override
	@SuppressWarnings("deprecation")
	protected String getAppName() {
		String appName = registration.getServiceId();
		return StringUtils.isEmpty(appName) ? super.getAppName() : appName;
	}

}
