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

package com.alibaba.cloud.appactive.config;

import com.alibaba.cloud.appactive.AppactiveProperties;
import com.alibaba.cloud.appactive.provider.CenterServiceFilter;
import com.alibaba.cloud.appactive.provider.UnitServiceFilter;
import io.appactive.servlet.RequestFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ChengPu raozihao
 * @description
 * @date 2022/8/15
 */
@Configuration
public class FilterAutoConfiguration {

	@Autowired
	private AppactiveProperties appactiveProperties;

	@Bean
	public FilterRegistrationBean<CenterServiceFilter> appActiveCenterServiceFilter() {
		if (appactiveProperties.getCenterPath() == null) {
			return null;
		}
		FilterRegistrationBean<CenterServiceFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		CenterServiceFilter reqResFilter = new CenterServiceFilter();
		filterRegistrationBean.setFilter(reqResFilter);
		filterRegistrationBean.addUrlPatterns(appactiveProperties.getCenterPath());
		return filterRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean<UnitServiceFilter> appActiveUnitServiceFilter() {
		if (appactiveProperties.getUnitPath() == null) {
			return null;
		}
		FilterRegistrationBean<UnitServiceFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		UnitServiceFilter reqResFilter = new UnitServiceFilter();
		filterRegistrationBean.setFilter(reqResFilter);
		filterRegistrationBean.addUrlPatterns(appactiveProperties.getUnitPath());
		return filterRegistrationBean;
	}

	@Bean
	public FilterRegistrationBean<RequestFilter> appActiveNormalServiceFilter() {
		if (appactiveProperties.getNormalPath() == null) {
			return null;
		}
		FilterRegistrationBean<RequestFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		RequestFilter reqResFilter = new RequestFilter();
		filterRegistrationBean.setFilter(reqResFilter);
		filterRegistrationBean.addUrlPatterns(appactiveProperties.getNormalPath());
		return filterRegistrationBean;
	}

}
