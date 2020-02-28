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

package com.alibaba.alicloud.oss;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.alicloud.oss.resource.OssStorageProtocolResolver;
import com.aliyun.oss.OSS;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.alicloud.oss.OssConstants.OSS_TASK_EXECUTOR_BEAN_NAME;

/**
 * OSS Auto {@link Configuration}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OSS.class)
@ConditionalOnProperty(name = OssConstants.ENABLED, havingValue = "true",
		matchIfMissing = true)
public class OssAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OssStorageProtocolResolver ossStorageProtocolResolver() {
		return new OssStorageProtocolResolver();
	}

	@Bean(name = OSS_TASK_EXECUTOR_BEAN_NAME)
	@ConditionalOnMissingBean
	public ExecutorService ossTaskExecutor() {
		int coreSize = Runtime.getRuntime().availableProcessors();
		return new ThreadPoolExecutor(coreSize, 128, 60, TimeUnit.SECONDS,
				new SynchronousQueue<>());
	}

}
