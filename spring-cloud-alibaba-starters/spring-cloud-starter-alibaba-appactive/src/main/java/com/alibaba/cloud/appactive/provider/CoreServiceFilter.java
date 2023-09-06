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

package com.alibaba.cloud.appactive.provider;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.cloud.appactive.constant.AppactiveConstants;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.java.api.base.constants.AppactiveConstant;
import io.appactive.java.api.bridge.servlet.ServletService;
import io.appactive.java.api.rule.TrafficMachineService;
import io.appactive.java.api.rule.machine.AbstractMachineUnitRuleService;
import io.appactive.java.api.rule.traffic.TrafficRouteRuleService;
import io.appactive.java.api.utils.lang.StringUtils;
import io.appactive.rule.ClientRuleService;
import io.appactive.support.log.LogUtil;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class CoreServiceFilter implements Filter {

	private final TrafficRouteRuleService trafficRouteRuleService = ClientRuleService
			.getTrafficRouteRuleService();

	private final AbstractMachineUnitRuleService machineUnitRuleService = ClientRuleService
			.getMachineUnitRuleService();

	private final TrafficMachineService trafficMachineService = new TrafficMachineService(
			trafficRouteRuleService, machineUnitRuleService);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest
				&& response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String routerId = ServletService.getRouteIdFromHeader(httpRequest,
				AppactiveConstants.ROUTER_ID_HEADER_KEY);
		if (StringUtils.isBlank(routerId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"no routerId provided for this request");
		}
		if (!trafficMachineService.isInCurrentUnit(routerId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"routerId " + routerId + " does not belong in core : "
							+ machineUnitRuleService.getCurrentUnit());
		}
		AppContextClient.setUnitContext(routerId);
		LogUtil.info(AppactiveConstant.PROJECT_NAME + "-routerIdFilter-doFilter-header:"
				+ AppContextClient.getRouteId());
		chain.doFilter(request, response);
		clear();
	}

	@Override
	public void destroy() {

	}

	private void clear() {
		AppContextClient.clearUnitContext();
	}

}
