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

package com.alibaba.alicloud.context.statistics;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.alicloud.context.acm.AcmContextBootstrapConfiguration;
import com.alibaba.alicloud.context.acm.AcmProperties;
import com.alibaba.alicloud.context.ans.AnsContextAutoConfiguration;
import com.alibaba.alicloud.context.ans.AnsProperties;
import com.alibaba.alicloud.context.edas.EdasProperties;
import com.alibaba.alicloud.context.oss.OssContextAutoConfiguration;
import com.alibaba.alicloud.context.oss.OssProperties;
import com.alibaba.alicloud.context.scx.ScxContextAutoConfiguration;
import com.alibaba.alicloud.context.scx.ScxProperties;
import com.alibaba.cloud.context.AliCloudServerMode;
import com.alibaba.cloud.context.edas.AliCloudEdasSdk;
import com.alibaba.cloud.context.statistics.StatisticsTask;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaolongzuo
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({ ScxContextAutoConfiguration.class,
		OssContextAutoConfiguration.class, AnsContextAutoConfiguration.class,
		AcmContextBootstrapConfiguration.class })
public class StatisticsTaskStarter implements InitializingBean {

	private static final String NACOS_CONFIG_SERVER_MODE_KEY = "spring.cloud.nacos.config.server-mode";

	private static final String NACOS_DISCOVERY_SERVER_MODE_KEY = "spring.cloud.nacos.discovery.server-mode";

	private static final String NACOS_SERVER_MODE_VALUE = "EDAS";

	@Autowired(required = false)
	private AliCloudEdasSdk aliCloudEdasSdk;

	@Autowired(required = false)
	private EdasProperties edasProperties;

	@Autowired(required = false)
	private ScxProperties scxProperties;

	@Autowired(required = false)
	private OssProperties ossProperties;

	@Autowired(required = false)
	private AnsProperties ansProperties;

	@Autowired(required = false)
	private AcmProperties acmProperties;

	@Autowired(required = false)
	private ScxContextAutoConfiguration scxContextAutoConfiguration;

	@Autowired(required = false)
	private OssContextAutoConfiguration ossContextAutoConfiguration;

	@Autowired(required = false)
	private AnsContextAutoConfiguration ansContextAutoConfiguration;

	@Autowired(required = false)
	private AcmContextBootstrapConfiguration acmContextBootstrapConfiguration;

	@Override
	public void afterPropertiesSet() {
		StatisticsTask statisticsTask = new StatisticsTask(aliCloudEdasSdk,
				edasProperties, getComponents());
		statisticsTask.start();
	}

	private List<String> getComponents() {
		List<String> components = new ArrayList<>();
		if (scxContextAutoConfiguration != null && scxProperties != null) {
			components.add("SC-SCX");
		}
		if (ossContextAutoConfiguration != null && ossProperties != null) {
			components.add("SC-OSS");
		}
		boolean edasEnabled = edasProperties != null && edasProperties.isEnabled();
		boolean ansEnableEdas = edasEnabled || (ansProperties != null
				&& ansProperties.getServerMode() == AliCloudServerMode.EDAS);
		if (ansContextAutoConfiguration != null && ansEnableEdas) {
			components.add("SC-ANS");
		}
		boolean acmEnableEdas = edasEnabled || (acmProperties != null
				&& acmProperties.getServerMode() == AliCloudServerMode.EDAS);
		if (acmContextBootstrapConfiguration != null && acmEnableEdas) {
			components.add("SC-ACM");
		}
		if (NACOS_SERVER_MODE_VALUE
				.equals(System.getProperty(NACOS_CONFIG_SERVER_MODE_KEY))) {
			components.add("SC-NACOS-CONFIG");
		}
		if (NACOS_SERVER_MODE_VALUE
				.equals(System.getProperty(NACOS_DISCOVERY_SERVER_MODE_KEY))) {
			components.add("SC-NACOS-DISCOVERY");
		}
		return components;
	}

}
