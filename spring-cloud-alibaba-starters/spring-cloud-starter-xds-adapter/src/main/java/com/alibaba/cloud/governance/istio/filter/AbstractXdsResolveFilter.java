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

package com.alibaba.cloud.governance.istio.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public abstract class AbstractXdsResolveFilter<T>
		implements XdsResolveFilter<T>, ApplicationContextAware {

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractXdsResolveFilter.class);

	protected ApplicationContext applicationContext;

	protected static final String ALLOW_ANY = "allow_any";

	protected static final String VIRTUAL_INBOUND = "virtualInbound";

	protected static final String CONNECTION_MANAGER = "envoy.filters.network.http_connection_manager";

	protected static final String RBAC_FILTER = "envoy.filters.http.rbac";

	protected static final String JWT_FILTER = "envoy.filters.http.jwt_authn";

	protected static final String ISTIO_AUTHN = "istio_authn";

	protected static final String REQUEST_AUTH_PRINCIPAL = "request.auth.principal";

	protected static final String REQUEST_AUTH_AUDIENCE = "request.auth.audiences";

	protected static final String REQUEST_AUTH_PRESENTER = "request.auth.presenter";

	protected static final String REQUEST_AUTH_CLAIMS = "request.auth.claims";

	protected static final String HEADER_NAME_AUTHORITY = ":authority";

	protected static final String HEADER_NAME_METHOD = ":method";

	protected static final int MIN_PORT = 0;

	protected static final int MAX_PORT = 65535;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
