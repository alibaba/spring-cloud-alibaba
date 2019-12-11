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

package com.alibaba.cloud.sidecar;

import java.net.URI;
import java.util.Map;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author www.itmuch.com
 */
public class SidecarHealthIndicator extends AbstractHealthIndicator {

	private final SidecarProperties sidecarProperties;

	private final RestTemplate restTemplate;

	public SidecarHealthIndicator(SidecarProperties sidecarProperties,
			RestTemplate restTemplate) {
		this.sidecarProperties = sidecarProperties;
		this.restTemplate = restTemplate;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		try {
			URI uri = this.sidecarProperties.getHealthCheckUrl();
			if (uri == null) {
				builder.up();
				return;
			}

			ResponseEntity<Map<String, Object>> exchange = this.restTemplate.exchange(uri,
					HttpMethod.GET, null,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});

			Map<String, Object> map = exchange.getBody();

			if (map == null) {
				this.getWarning(builder);
				return;
			}
			Object status = map.get("status");
			if (status instanceof String) {
				builder.status(status.toString());
			}
			else {
				this.getWarning(builder);
			}
		}
		catch (Exception e) {
			builder.down().withDetail("error", e.getMessage());
		}
	}

	private void getWarning(Health.Builder builder) {
		builder.unknown().withDetail("warning", "no status field in response");
	}

}
