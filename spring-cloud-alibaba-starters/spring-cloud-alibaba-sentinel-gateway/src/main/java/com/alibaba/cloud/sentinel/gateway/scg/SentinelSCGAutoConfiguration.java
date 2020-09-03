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

package com.alibaba.cloud.sentinel.gateway.scg;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.alibaba.cloud.sentinel.gateway.ConfigConstants;
import com.alibaba.cloud.sentinel.gateway.FallbackProperties;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.RedirectBlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(GlobalFilter.class)
@ConditionalOnProperty(prefix = ConfigConstants.GATEWAY_PREFIX, name = "enabled",
		havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SentinelGatewayProperties.class)
public class SentinelSCGAutoConfiguration {

	private static final Logger logger = LoggerFactory
			.getLogger(SentinelSCGAutoConfiguration.class);

	private final List<ViewResolver> viewResolvers;

	private final ServerCodecConfigurer serverCodecConfigurer;

	@Autowired
	private Optional<BlockRequestHandler> blockRequestHandlerOptional;

	@Autowired
	private SentinelGatewayProperties gatewayProperties;

	@PostConstruct
	private void init() {
		// blockRequestHandlerOptional has low priority
		blockRequestHandlerOptional.ifPresent(GatewayCallbackManager::setBlockHandler);
		initAppType();
		initFallback();
	}

	public SentinelSCGAutoConfiguration(
			ObjectProvider<List<ViewResolver>> viewResolversProvider,
			ServerCodecConfigurer serverCodecConfigurer) {
		this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
		this.serverCodecConfigurer = serverCodecConfigurer;
	}

	private void initAppType() {
		System.setProperty(SentinelConfig.APP_TYPE_PROP_KEY,
				ConfigConstants.APP_TYPE_SCG_GATEWAY);
	}

	private void initFallback() {
		FallbackProperties fallbackProperties = gatewayProperties.getFallback();
		if (fallbackProperties == null
				|| StringUtil.isBlank(fallbackProperties.getMode())) {
			return;
		}
		if (ConfigConstants.FALLBACK_MSG_RESPONSE.equals(fallbackProperties.getMode())) {
			if (StringUtil.isNotBlank(fallbackProperties.getResponseBody())) {
				GatewayCallbackManager.setBlockHandler((exchange, t) -> ServerResponse
						.status(fallbackProperties.getResponseStatus())
						.contentType(
								MediaType.valueOf(fallbackProperties.getContentType()))
						.body(fromValue(fallbackProperties.getResponseBody())));
				logger.info(
						"[Sentinel SpringCloudGateway] using AnonymousBlockRequestHandler, responseStatus: "
								+ fallbackProperties.getResponseStatus()
								+ ", responseBody: "
								+ fallbackProperties.getResponseBody());
			}
		}
		String redirectUrl = fallbackProperties.getRedirect();
		if (ConfigConstants.FALLBACK_REDIRECT.equals(fallbackProperties.getMode())
				&& StringUtil.isNotBlank(redirectUrl)) {
			GatewayCallbackManager
					.setBlockHandler(new RedirectBlockRequestHandler(redirectUrl));
			logger.info(
					"[Sentinel SpringCloudGateway] using RedirectBlockRequestHandler, redirectUrl: "
							+ redirectUrl);
		}
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@ConditionalOnMissingBean
	public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
		// Register the block exception handler for Spring Cloud Gateway.
		logger.info(
				"[Sentinel SpringCloudGateway] register SentinelGatewayBlockExceptionHandler");
		return new SentinelGatewayBlockExceptionHandler(viewResolvers,
				serverCodecConfigurer);
	}

	@Bean
	@Order(-1)
	@ConditionalOnMissingBean
	public SentinelGatewayFilter sentinelGatewayFilter() {
		logger.info(
				"[Sentinel SpringCloudGateway] register SentinelGatewayFilter with order: {}",
				gatewayProperties.getOrder());
		return new SentinelGatewayFilter(gatewayProperties.getOrder());
	}

}
