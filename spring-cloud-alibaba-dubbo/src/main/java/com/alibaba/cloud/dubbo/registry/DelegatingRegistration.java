/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.dubbo.registry;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * The {@link Registration} of Dubbo uses an external of {@link ServiceInstance} instance
 * as the delegate.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class DelegatingRegistration implements Registration {

	private final ServiceInstance delegate;

	DelegatingRegistration(ServiceInstance delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getServiceId() {
		return delegate.getServiceId();
	}

	@Override
	public String getHost() {
		return delegate.getHost();
	}

	@Override
	public int getPort() {
		return delegate.getPort();
	}

	@Override
	public boolean isSecure() {
		return delegate.isSecure();
	}

	@Override
	public URI getUri() {
		return delegate.getUri();
	}

	@Override
	public Map<String, String> getMetadata() {
		return delegate.getMetadata();
	}

	@Override
	public String getScheme() {
		return delegate.getScheme();
	}

}
