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

package com.alibaba.cloud.dubbo.autoconfigure;

import java.util.Collection;
import java.util.function.Supplier;

import com.alibaba.cloud.dubbo.metadata.DubboProtocolConfigSupplier;
import com.alibaba.cloud.dubbo.metadata.repository.RandomServiceInstanceSelector;
import com.alibaba.cloud.dubbo.metadata.repository.ServiceInstanceSelector;
import com.alibaba.cloud.dubbo.metadata.resolver.DubboServiceBeanMetadataResolver;
import com.alibaba.cloud.dubbo.metadata.resolver.MetadataResolver;
import feign.Contract;
import org.apache.dubbo.config.ProtocolConfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Auto-Configuration class for Dubbo Metadata Bean.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration(proxyBeanMethods = false)
public class DubboMetadataBeanConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public MetadataResolver metadataJsonResolver(ObjectProvider<Contract> contract) {
		return new DubboServiceBeanMetadataResolver(contract);
	}

	@Bean
	@ConditionalOnMissingBean
	public ServiceInstanceSelector metadataServiceInstanceSelector() {
		return new RandomServiceInstanceSelector();
	}

	@Bean
	public Supplier<ProtocolConfig> dubboProtocolConfigSupplier(
			ObjectProvider<Collection<ProtocolConfig>> protocols) {
		return new DubboProtocolConfigSupplier(protocols);
	}

}
