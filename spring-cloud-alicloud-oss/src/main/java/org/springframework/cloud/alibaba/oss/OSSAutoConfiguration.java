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

package org.springframework.cloud.alibaba.oss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alibaba.oss.resource.OSSStorageProtocolResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

/**
 * OSS Auto {@link Configuration}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
@ConditionalOnClass(OSS.class)
@ConditionalOnProperty(name = OSSConstants.ENABLED, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OSSProperties.class)
public class OSSAutoConfiguration {

	private static final Logger logger = LoggerFactory
			.getLogger(OSSAutoConfiguration.class);

	@ConditionalOnMissingBean
	@Bean
	public OSS ossClient(OSSProperties ossProperties) {
		logger.info("construct OSS because it is missing");
		return new OSSClientBuilder().build(ossProperties.getEndpoint(),
				ossProperties.getAccessKeyId(), ossProperties.getSecretAccessKey(),
				ossProperties.getSecurityToken(), ossProperties.getConfiguration());
	}

	@ConditionalOnMissingBean
	@Bean
	public OSSStorageProtocolResolver ossStorageProtocolResolver() {
		return new OSSStorageProtocolResolver();
	}

}
