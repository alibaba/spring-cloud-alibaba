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

package com.alibaba.cloud.sentinel.endpoint;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import static com.alibaba.cloud.sentinel.SentinelConstants.BLOCK_PAGE_URL_CONF_KEY;

/**
 * Endpoint for Sentinel, contains ans properties and rules.
 *
 * @author xiaojing
 */
@Endpoint(id = "sentinel")
public class SentinelEndpoint {

	private final SentinelProperties sentinelProperties;

	public SentinelEndpoint(SentinelProperties sentinelProperties) {
		this.sentinelProperties = sentinelProperties;
	}

	@ReadOperation
	public Map<String, Object> invoke() {
		final Map<String, Object> result = new HashMap<>();
		if (sentinelProperties.isEnabled()) {

			result.put("appName", AppNameUtil.getAppName());
			result.put("logDir", LogBase.getLogBaseDir());
			result.put("logUsePid", LogBase.isLogNameUsePid());
			result.put("blockPage", SentinelConfig.getConfig(BLOCK_PAGE_URL_CONF_KEY));
			result.put("metricsFileSize", SentinelConfig.singleMetricFileSize());
			result.put("metricsFileCharset", SentinelConfig.charset());
			result.put("totalMetricsFileCount", SentinelConfig.totalMetricFileCount());
			result.put("consoleServer", TransportConfig.getConsoleServer());
			result.put("clientIp", TransportConfig.getHeartbeatClientIp());
			result.put("heartbeatIntervalMs", TransportConfig.getHeartbeatIntervalMs());
			result.put("clientPort", TransportConfig.getPort());
			result.put("coldFactor", sentinelProperties.getFlow().getColdFactor());
			result.put("filter", sentinelProperties.getFilter());
			result.put("datasource", sentinelProperties.getDatasource());

			final Map<String, Object> rules = new HashMap<>();
			result.put("rules", rules);
			rules.put("flowRules", FlowRuleManager.getRules());
			rules.put("degradeRules", DegradeRuleManager.getRules());
			rules.put("systemRules", SystemRuleManager.getRules());
			rules.put("authorityRule", AuthorityRuleManager.getRules());
			rules.put("paramFlowRule", ParamFlowRuleManager.getRules());
		}
		return result;
	}

}
