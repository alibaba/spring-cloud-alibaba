/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alicloud.scx.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.cloud.alicloud.context.scx.ScxProperties;

/**
 * @author xiaolongzuo
 */
public class ScxEndpoint extends AbstractEndpoint<Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScxEndpoint.class);

	private ScxProperties scxProperties;

	private EdasProperties edasProperties;

	public ScxEndpoint(EdasProperties edasProperties, ScxProperties scxProperties) {
		super("scx", false);
		this.edasProperties = edasProperties;
		this.scxProperties = scxProperties;
	}

	/**
	 * @return scx endpoint
	 */
	@Override
	public Map<String, Object> invoke() {
		Map<String, Object> scxEndpoint = new HashMap<>();
		LOGGER.info("SCX endpoint invoke, scxProperties is {}", scxProperties);
		scxEndpoint.put("namespace",
				edasProperties == null ? "" : edasProperties.getNamespace());
		scxEndpoint.put("scxProperties", scxProperties);
		return scxEndpoint;
	}

}
