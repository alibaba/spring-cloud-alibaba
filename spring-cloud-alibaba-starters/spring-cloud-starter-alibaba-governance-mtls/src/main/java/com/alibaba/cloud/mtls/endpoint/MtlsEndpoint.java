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

package com.alibaba.cloud.mtls.endpoint;

import com.alibaba.cloud.commons.governance.tls.ServerTlsModeHolder;
import com.alibaba.cloud.mtls.server.ApplicationRestarter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

/**
 * @author musi
 */

@Endpoint(id = "sds")
public class MtlsEndpoint {

	@Autowired
	private ApplicationRestarter restarter;

	@WriteOperation
	public String updateTlsMode(boolean isTls) {
		if (ServerTlsModeHolder.canModeUpdate(isTls)) {
			ServerTlsModeHolder.setTlsMode(isTls);
			restarter.restart();
		}
		return "update tls mode to " + isTls;
	}

}
