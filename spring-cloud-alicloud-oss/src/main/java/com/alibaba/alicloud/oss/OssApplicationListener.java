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

package com.alibaba.alicloud.oss;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.aliyun.oss.OSS;

/**
 * Shutdown All OSS Clients when {@code ApplicationContext} gets closed
 * {@link ApplicationListener}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class OssApplicationListener implements ApplicationListener<ContextClosedEvent> {

	private static final Logger log = LoggerFactory
			.getLogger(OssApplicationListener.class);

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		Map<String, OSS> ossClientMap = event.getApplicationContext()
				.getBeansOfType(OSS.class);
		log.info("{} OSSClients will be shutdown soon", ossClientMap.size());
		ossClientMap.keySet().forEach(beanName -> {
			log.info("shutdown ossClient: {}", beanName);
			ossClientMap.get(beanName).shutdown();
		});
	}
}
