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

package com.alibaba.cloud.dubbo.registry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.dubbo.common.URLBuilder.from;
import static org.apache.dubbo.common.constants.RegistryConstants.CATEGORY_KEY;
import static org.apache.dubbo.common.constants.RegistryConstants.EMPTY_PROTOCOL;
import static org.apache.dubbo.common.utils.CollectionUtils.isEmpty;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public abstract class AbstractServiceSubscribeHandler {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final URL url;

	protected final NotifyListener listener;

	protected final DubboCloudRegistry registry;

	public AbstractServiceSubscribeHandler(URL url, NotifyListener listener,
			DubboCloudRegistry registry) {
		this.url = url;
		this.listener = listener;
		this.registry = registry;
	}

	protected void notifyAllSubscribedURLs(URL url, List<URL> subscribedURLs,
			NotifyListener listener) {

		if (isEmpty(subscribedURLs)) {
			// Add the EMPTY_PROTOCOL URL
			listener.notify(Collections.singletonList(emptyURL(url)));
			// if (isDubboMetadataServiceURL(url)) {
			// if meta service change, and serviceInstances is zero, will clean up
			// information about this client
			// String serviceName = url.getParameter(GROUP_KEY);
			// repository.removeMetadataAndInitializedService(serviceName, url);
			// }
		}
		else {
			// Notify all
			listener.notify(subscribedURLs);
		}
	}

	private URL emptyURL(URL url) {
		// issue : When the last service provider is closed, the client still periodically
		// connects to the last provider.n
		// fix https://github.com/alibaba/spring-cloud-alibaba/issues/1259
		return from(url).setProtocol(EMPTY_PROTOCOL).removeParameter(CATEGORY_KEY)
				.build();
	}

	private final AtomicBoolean inited = new AtomicBoolean(false);

	public void init() {
		if (inited.compareAndSet(false, true)) {
			doInit();
		}
	}

	protected abstract void doInit();

}
