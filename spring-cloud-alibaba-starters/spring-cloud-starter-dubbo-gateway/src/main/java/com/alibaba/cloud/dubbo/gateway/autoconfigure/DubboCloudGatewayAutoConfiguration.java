/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.dubbo.gateway.autoconfigure;

import com.alibaba.cloud.dubbo.autoconfigure.DubboMetadataAutoConfiguration;
import com.alibaba.cloud.dubbo.autoconfigure.DubboServiceAutoConfiguration;
import com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayExecutor;
import com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayProperties;
import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "dubbo.cloud.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({ DubboServiceAutoConfiguration.class,
		DubboMetadataAutoConfiguration.class })
@EnableConfigurationProperties(DubboCloudGatewayProperties.class)
public class DubboCloudGatewayAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(DubboCloudGatewayExecutor.class)
	public DubboCloudGatewayExecutor dubboCloudGatewayExecutor(
			DubboServiceMetadataRepository repository,
			DubboGenericServiceFactory serviceFactory,
			DubboGenericServiceExecutionContextFactory contextFactory,
			DubboCloudGatewayProperties dubboCloudGatewayProperties,
			ObjectProvider<ConversionService> conversionServices) {
		return new DubboCloudGatewayExecutor(repository, serviceFactory, contextFactory,
				dubboCloudGatewayProperties, conversionServices);
	}
}
