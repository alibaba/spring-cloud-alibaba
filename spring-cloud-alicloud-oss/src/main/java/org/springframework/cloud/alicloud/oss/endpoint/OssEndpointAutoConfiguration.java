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

package org.springframework.cloud.alicloud.oss.endpoint;

import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.context.oss.OssProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS {@link Endpoint} Auto-{@link Configuration}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConditionalOnClass(Endpoint.class)
@EnableConfigurationProperties({ OssProperties.class })
public class OssEndpointAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnEnabledEndpoint("oss")
	public OssEndpoint sentinelEndPoint() {
		return new OssEndpoint();
	}

}