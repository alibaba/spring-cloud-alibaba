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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.cloud.alicloud.context.scx.ScxProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author xiaolongzuo
 */
@ConditionalOnWebApplication
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.AbstractEndpoint")
public class ScxEndpointAutoConfiguration {

	@Bean
	public ScxEndpoint scxEndpoint(EdasProperties edasProperties,
			ScxProperties scxProperties) {
		return new ScxEndpoint(edasProperties, scxProperties);
	}
}
