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

package org.springframework.cloud.alicloud.context.scx;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.cloud.alicloud.context.edas.EdasContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.context.edas.AliCloudEdasSdk;
import com.alibaba.cloud.context.scx.AliCloudScxInitializer;
import com.alibaba.dts.common.exception.InitException;

/**
 * @author xiaolongzuo
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.cloud.alicloud.scx.ScxAutoConfiguration")
@EnableConfigurationProperties(ScxProperties.class)
@ImportAutoConfiguration(EdasContextAutoConfiguration.class)
public class ScxContextAutoConfiguration {

	private static final Logger log = LoggerFactory
			.getLogger(ScxContextAutoConfiguration.class);

	@Autowired
	private AliCloudProperties aliCloudProperties;

	@Autowired
	private EdasProperties edasProperties;

	@Autowired
	private ScxProperties scxProperties;

	@Autowired
	private AliCloudEdasSdk aliCloudEdasSdk;

	@PostConstruct
	public void initAcmProperties() {
		try {
			AliCloudScxInitializer.initialize(aliCloudProperties, edasProperties,
					scxProperties, aliCloudEdasSdk);
		}
		catch (InitException e) {
			log.error("Init SchedulerX failed.", e);
			throw new RuntimeException(e);
		}
	}
}
