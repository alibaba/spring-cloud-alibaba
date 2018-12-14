/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.context.statistics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.alicloud.context.acm.AcmContextBootstrapConfiguration;
import org.springframework.cloud.alicloud.context.acm.AcmProperties;
import org.springframework.cloud.alicloud.context.ans.AnsContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.ans.AnsProperties;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.cloud.alicloud.context.oss.OssContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.oss.OssProperties;
import org.springframework.cloud.alicloud.context.scx.ScxContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.scx.ScxProperties;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.context.AliCloudServerMode;
import com.alibaba.cloud.context.edas.AliCloudEdasSdk;
import com.alibaba.cloud.context.statistics.StatisticsTask;

/**
 * @author xiaolongzuo
 */
@Configuration
@AutoConfigureAfter({ ScxContextAutoConfiguration.class,
		OssContextAutoConfiguration.class, AnsContextAutoConfiguration.class,
		AcmContextBootstrapConfiguration.class })
public class StatisticsTaskStarter implements InitializingBean {

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
		return components;
	}

}
