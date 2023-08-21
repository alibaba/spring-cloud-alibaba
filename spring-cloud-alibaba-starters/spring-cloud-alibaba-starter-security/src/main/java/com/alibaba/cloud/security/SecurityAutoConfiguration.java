/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.security;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import com.alibaba.cloud.security.trust.TlsModeListener;
import com.alibaba.cloud.security.trust.TrustSslStoreProvider;
import com.alibaba.cloud.security.trust.auth.SentinelTrustInterceptor;
import com.alibaba.cloud.security.trust.auth.TrustWebMvcConfigurer;
import com.alibaba.cloud.security.trust.rest.ClientRequestFactoryProvider;
import com.alibaba.cloud.security.trust.rest.TrustRestBeanPostProcessor;
import com.alibaba.cloud.security.trust.rest.TrustRestTemplateCallback;
import com.alibaba.cloud.security.trust.tomcat.TrustTomcatConnectCustomizer;
import com.alibaba.csp.sentinel.datasource.xds.XdsDataSource;
import com.alibaba.csp.sentinel.datasource.xds.config.XdsConfigProperties;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.AsymCryptoType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.HashType;
import com.alibaba.csp.sentinel.datasource.xds.constant.type.JwtPolicyType;
import com.alibaba.csp.sentinel.trust.TrustManager;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author <a href="liwj418@foxmail.com">lwj</a>
 */
@EnableConfigurationProperties(SecurityConfigProperties.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = SecurityConfigProperties.PREFIX + ".open", matchIfMissing = true)
@AutoConfigureOrder(100)
public class SecurityAutoConfiguration {


	@Bean
	public XdsConfigProperties xdsConfigProperties(SecurityConfigProperties securityConfigProperties) {
		XdsConfigProperties config = XdsConfigProperties.getFromXdsPropertiesEnv();

		Optional.ofNullable(securityConfigProperties.getHost()).ifPresent(c -> config.setHost(c));
		Optional.ofNullable(securityConfigProperties.getPort()).ifPresent(c -> config.setPort(c));
		Optional.ofNullable(securityConfigProperties.getJwtPolicy()).ifPresent(c -> {
			JwtPolicyType jwtPolicy = JwtPolicyType.getByKey(c);
			if (null != jwtPolicy) {
				config.setJwtPolicy(jwtPolicy);
			}
		});

		Optional.ofNullable(securityConfigProperties.getIstiodToken()).ifPresent(c -> config.setIstiodToken(c));
		Optional.ofNullable(securityConfigProperties.getCaAddr()).ifPresent(c -> config.setCaAddr(c));
		Optional.ofNullable(securityConfigProperties.getCaCertPath()).ifPresent(c -> config.setCaCertPath(c));
		Optional.ofNullable(securityConfigProperties.getNamespace()).ifPresent(c -> config.setNamespace(c));
		Optional.ofNullable(securityConfigProperties.getPodName()).ifPresent(c -> config.setPodName(c));
		Optional.ofNullable(securityConfigProperties.getClusterId()).ifPresent(c -> config.setClusterId(c));

		Optional.ofNullable(securityConfigProperties.getAsymCryptoType()).ifPresent(c -> {
			AsymCryptoType asymCryptoType = AsymCryptoType.getByKey(c);
			if (null != asymCryptoType) {
				config.setAsymCryptoType(asymCryptoType);
			}
		});

		Optional.ofNullable(securityConfigProperties.getHashType()).ifPresent(c -> {
			HashType hashType = HashType.getByKey(c);
			if (null != hashType) {
				config.setHashType(hashType);
			}
		});

		Optional.ofNullable(securityConfigProperties.getGetCertTimeoutS()).ifPresent(c -> config.setGetCertTimeoutS(c));
		Optional.ofNullable(securityConfigProperties.getCertValidityTimeS()).ifPresent(c -> config.setCertValidityTimeS(c));
		Optional.ofNullable(securityConfigProperties.getCertPeriodRatio()).ifPresent(c -> config.setCertPeriodRatio(c));
		Optional.ofNullable(securityConfigProperties.getReconnectionDelayS()).ifPresent(c -> config.setReconnectionDelayS(c));
		Optional.ofNullable(securityConfigProperties.getInitAwaitTimeS()).ifPresent(c -> config.setInitAwaitTimeS(c));
		return config;
	}

	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public TlsModeListener tlsModeListener(XdsDataSource xdsDataSource) {
		TrustManager trustManager = TrustManager.getInstance();
		TlsModeListener tlsModoListener = new TlsModeListener(xdsDataSource);
		trustManager.registerTlsModeCallback(tlsMode -> tlsModoListener.onUpdate());
		return tlsModoListener;
	}


	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public XdsDataSource xdsDataSource(XdsConfigProperties properties) throws InterruptedException {
		XdsDataSource xdsDataSource = new XdsDataSource<>(properties);
		TrustManager trustManager = TrustManager.getInstance();
		xdsDataSource.registerTrustManager(trustManager);
		CountDownLatch countDownLatch = new CountDownLatch(1);
		trustManager.registerTlsModeCallback(tlsMode -> countDownLatch.countDown());
		xdsDataSource.start();
		countDownLatch.await();
		return xdsDataSource;
	}

	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public TrustSslStoreProvider mtlsSslStoreProvider(XdsDataSource xdsDataSource) {
		return new TrustSslStoreProvider();
	}


	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public SentinelTrustInterceptor trustSentinelInterceptor() {
		return new SentinelTrustInterceptor();
	}

	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public TrustWebMvcConfigurer xdsWebMvcConfigurer(SentinelTrustInterceptor sentinelTrustInterceptor) {
		return new TrustWebMvcConfigurer(sentinelTrustInterceptor);
	}

	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public TomcatConnectorCustomizer mtlsCustomizer(TrustSslStoreProvider sslStoreProvider, XdsDataSource xdsDataSource) {
		return new TrustTomcatConnectCustomizer(sslStoreProvider);
	}

	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public TrustRestBeanPostProcessor restMtlsBeanPostProcessor(
			ClientRequestFactoryProvider clientRequestFactoryProvider) {
		return new TrustRestBeanPostProcessor(clientRequestFactoryProvider);
	}


	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public TrustRestTemplateCallback restTemplateCallback(ClientRequestFactoryProvider clientRequestFactoryProvider) {
		TrustRestTemplateCallback trustRestTemplateCallback = new TrustRestTemplateCallback(clientRequestFactoryProvider);
		TrustManager.getInstance().registerCertCallback(certPair -> trustRestTemplateCallback.onUpdateCert());
		return trustRestTemplateCallback;
	}


	@Bean
	@ConditionalOnClass(XdsConfigProperties.class)
	public ClientRequestFactoryProvider clientRequestFactoryProvider(
			TrustSslStoreProvider trustSslStoreProvider) {
		return new ClientRequestFactoryProvider(trustSslStoreProvider);
	}
}
