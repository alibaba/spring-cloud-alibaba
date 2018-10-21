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

package org.springframework.cloud.alibaba.oss.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.aliyun.oss.OSS;

/**
 * A {@link ProtocolResolver} implementation for the {@code oss://} protocol.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class OSSStorageProtocolResolver
		implements ProtocolResolver, BeanFactoryPostProcessor, ResourceLoaderAware {

	public static final String PROTOCOL = "oss://";

	private static final Logger logger = LoggerFactory
			.getLogger(OSSStorageProtocolResolver.class);

	private ConfigurableListableBeanFactory beanFactory;

	private OSS oss;

	private OSS getOSS() {
		if (this.oss == null) {
			if (this.beanFactory.getBeansOfType(OSS.class).size() > 1) {
				logger.warn(
						"There are multiple OSS instances, consider marking one of them as @Primary to resolve oss protocol.");
			}
			this.oss = this.beanFactory.getBean(OSS.class);
		}
		return this.oss;
	}

	@Override
	public Resource resolve(String location, ResourceLoader resourceLoader) {
		if (!location.startsWith(PROTOCOL)) {
			return null;
		}
		return new OSSStorageResource(getOSS(), location);
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		if (DefaultResourceLoader.class.isAssignableFrom(resourceLoader.getClass())) {
			((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
		}
		else {
			logger.warn("The provided delegate resource loader is not an implementation "
					+ "of DefaultResourceLoader. Custom Protocol using oss:// prefix will not be enabled.");
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
			throws BeansException {
		this.beanFactory = beanFactory;
	}
}
