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

import com.alibaba.cloud.dubbo.env.DubboCloudProperties;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import com.alibaba.cloud.dubbo.service.parameter.PathVariableServiceParameterResolver;
import com.alibaba.cloud.dubbo.service.parameter.RequestBodyServiceParameterResolver;
import com.alibaba.cloud.dubbo.service.parameter.RequestHeaderServiceParameterResolver;
import com.alibaba.cloud.dubbo.service.parameter.RequestParamServiceParameterResolver;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot Auto-Configuration class for Dubbo Service.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DubboCloudProperties.class)
public class DubboServiceAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public DubboGenericServiceFactory dubboGenericServiceFactory() {
		return new DubboGenericServiceFactory();
	}

	@Configuration(proxyBeanMethods = false)
	@Import({ DubboGenericServiceExecutionContextFactory.class,
			RequestParamServiceParameterResolver.class,
			RequestBodyServiceParameterResolver.class,
			RequestHeaderServiceParameterResolver.class,
			PathVariableServiceParameterResolver.class })
	static class ParameterResolversConfiguration {

	}

}
