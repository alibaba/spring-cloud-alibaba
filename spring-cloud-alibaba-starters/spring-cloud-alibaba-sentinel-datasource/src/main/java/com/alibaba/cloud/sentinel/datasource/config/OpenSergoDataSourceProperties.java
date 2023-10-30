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

package com.alibaba.cloud.sentinel.datasource.config;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.cloud.sentinel.datasource.factorybean.OpenSergoDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.OpenSergoDataSourceGroup;
import com.alibaba.csp.sentinel.datasource.OpenSergoSentinelConstants;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;

import org.springframework.util.CollectionUtils;

/**
 * OpenSergo Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link OpenSergoDataSourceFactoryBean}.
 *
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
public class OpenSergoDataSourceProperties extends AbstractDataSourceProperties {
	private static final String FLOW = "flow";
	private static final String DEGRADE = "degrade";
	private String host = "127.0.0.1";

	private int port = 10246;

	private String namespace = "default";

	private String app = AppNameUtil.getAppName();

	private Set<String> enabledRules = new HashSet<>();

	public OpenSergoDataSourceProperties() {
		super(OpenSergoDataSourceFactoryBean.class.getName());
	}

	public void postRegister(OpenSergoDataSourceGroup dataSourceGroup) {
		// TODO: SystemRule and ParamFlowRule
		if (enabledRules.contains(FLOW)) {
			FlowRuleManager.register2Property(dataSourceGroup.subscribeFlowRules());
		}
		if (enabledRules.contains(DEGRADE)) {
			DegradeRuleManager.register2Property(dataSourceGroup.subscribeDegradeRules());
		}
		// When there is no enabled-rules, try ruleType
		RuleType ruleType = getRuleType();
		switch (ruleType) {
		case FLOW:
			FlowRuleManager.register2Property(dataSourceGroup.subscribeFlowRules());
			break;
		case DEGRADE:
			DegradeRuleManager.register2Property(dataSourceGroup.subscribeDegradeRules());
			break;
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Set<String> getEnabledRules() {
		return enabledRules;
	}

	public void setEnabledRules(Set<String> enabledRules) {
		this.enabledRules = enabledRules;
	}

}
