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

package com.alibaba.cloud.kubernetes.commons;

import io.fabric8.kubernetes.client.KubernetesClient;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * We don't provide autoconfiguration for KubernetesClient in this module.
 * <p>
 * Instead, we provide a configuration class, let the user decide whether to use it, and
 * it can be imported manually.
 * <p>
 * For example:
 *
 * <pre>
 * &#64;Configuration(proxyBeanMethods = false)
 * &#64;Import(KubernetesClientConfiguration.class)
 * public class MyConfiguration {
 * }
 * </pre>
 *
 * @author Freeman
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KubernetesClient.class)
public class KubernetesClientConfiguration implements DisposableBean {

	@Bean
	@ConditionalOnMissingBean
	public KubernetesClient fabric8KubernetesClient() {
		return KubernetesClientHolder.getKubernetesClient();
	}

	@Override
	public void destroy() {
		KubernetesClientHolder.remove();
	}
}
