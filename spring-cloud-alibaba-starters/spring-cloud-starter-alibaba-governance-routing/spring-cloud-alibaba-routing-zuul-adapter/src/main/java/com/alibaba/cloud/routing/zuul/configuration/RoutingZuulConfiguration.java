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

package com.alibaba.cloud.routing.zuul.configuration;

import com.alibaba.cloud.routing.context.RoutingContextHolder;
import com.alibaba.cloud.routing.zuul.context.defaults.DefaultRoutingZuulContextHolder;
import com.alibaba.cloud.routing.zuul.filter.RoutingZuulFilter;
import com.alibaba.cloud.routing.zuul.filter.defaults.DefaultRoutingZuulFilter;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Configuration
@AutoConfigureBefore(RibbonClientConfiguration.class)
public class RoutingZuulConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public RoutingContextHolder routingZuulContextHolder() {

		return new DefaultRoutingZuulContextHolder();
	}

	@Bean
	@ConditionalOnMissingBean
	public RoutingZuulFilter routingZuulFilter() {

		return new DefaultRoutingZuulFilter();
	}

}
