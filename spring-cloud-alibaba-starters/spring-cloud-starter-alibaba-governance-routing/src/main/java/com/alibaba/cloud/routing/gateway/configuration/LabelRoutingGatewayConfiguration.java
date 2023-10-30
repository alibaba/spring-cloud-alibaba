/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.gateway.configuration;

import com.alibaba.cloud.routing.gateway.filter.LabelRoutingGatewayClearFilter;
import com.alibaba.cloud.routing.gateway.filter.LabelRoutingGatewayFilter;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

@Configuration
@ConditionalOnClass({ GlobalFilter.class })
@AutoConfigureBefore(RibbonClientConfiguration.class)
public class LabelRoutingGatewayConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public LabelRoutingGatewayFilter labelRoutingGatewayFilter() {

		return new LabelRoutingGatewayFilter();
	}

	@Bean
	@ConditionalOnMissingBean
	public LabelRoutingGatewayClearFilter labelRoutingGatewayClearFilter() {

		return new LabelRoutingGatewayClearFilter();
	}

}
