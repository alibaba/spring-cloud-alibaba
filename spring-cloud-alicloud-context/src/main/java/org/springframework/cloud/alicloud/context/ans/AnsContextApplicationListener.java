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

package org.springframework.cloud.alicloud.context.ans;

import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.cloud.alicloud.context.listener.AbstractOnceApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.alibaba.cloud.context.ans.AliCloudAnsInitializer;
import com.alibaba.cloud.context.edas.AliCloudEdasSdk;

/**
 * Init {@link com.alibaba.ans.core.NamingService} properties.
 *
 * @author xiaolongzuo
 */
public class AnsContextApplicationListener
		extends AbstractOnceApplicationListener<ContextRefreshedEvent> {

	@Override
	protected String conditionalOnClass() {
		return "org.springframework.cloud.alicloud.ans.AnsAutoConfiguration";
	}

	@Override
	public void handleEvent(ContextRefreshedEvent event) {
		ApplicationContext applicationContext = event.getApplicationContext();
		AliCloudProperties aliCloudProperties = applicationContext
				.getBean(AliCloudProperties.class);
		EdasProperties edasProperties = applicationContext.getBean(EdasProperties.class);
		AnsProperties ansProperties = applicationContext.getBean(AnsProperties.class);
		AliCloudEdasSdk aliCloudEdasSdk = applicationContext
				.getBean(AliCloudEdasSdk.class);
		AliCloudAnsInitializer.initialize(aliCloudProperties, edasProperties,
				ansProperties, aliCloudEdasSdk);
	}

}
