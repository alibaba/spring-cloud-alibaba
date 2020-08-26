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
package com.alibaba.cloud.dubbo.gateway.standard;

import com.alibaba.cloud.dubbo.autoconfigure.DubboMetadataAutoConfiguration;
import com.alibaba.cloud.dubbo.autoconfigure.DubboServiceAutoConfiguration;
import com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayExecutor;
import com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayProperties;
import com.alibaba.cloud.dubbo.gateway.autoconfigure.DubboCloudGatewayAutoConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.support.DefaultServerCodecConfigurer;

/**
 * The Auto-{@link Configuration} of Dubbo Spring Cloud Gateway
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(GlobalFilter.class)
@ConditionalOnProperty(prefix = "dubbo.cloud.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({ DubboServiceAutoConfiguration.class,
		DubboMetadataAutoConfiguration.class, DubboCloudGatewayAutoConfiguration.class })
@EnableConfigurationProperties(DubboCloudGatewayProperties.class)
public class DubboSpringCloudGatewayAutoConfiguration {

	private final Log logger = LogFactory.getLog(getClass());

	@Bean
	@ConditionalOnMissingBean(ServerCodecConfigurer.class)
	public ServerCodecConfigurer serverCodecConfigurer() {
		return new DefaultServerCodecConfigurer();
	}

	@Bean
	public DubboSpringCloudGatewayFilter dubboSpringCloudGatewayFilter(
			DubboCloudGatewayExecutor dubboCloudGatewayExecutor) {
		return new DubboSpringCloudGatewayFilter(dubboCloudGatewayExecutor);
	}

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(r -> r.path("/**").uri("http://localhost:9090").id("first"))
				.build();
	}

}
