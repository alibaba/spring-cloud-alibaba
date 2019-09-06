/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.nacos;

import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class NacosNamingManager implements ApplicationContextAware {

	public NamingService getNamingService() {
		return ServiceHolder.getInstance().getNamingService();
	}

	public NamingMaintainService getNamingMaintainService() {
		return ServiceHolder.getInstance().getNamingMaintainService();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		NacosDiscoveryProperties properties = applicationContext
				.getBean(NacosDiscoveryProperties.class);
		ServiceHolder holder = ServiceHolder.getInstance();
		if (!holder.alreadyInit[0]) {
			holder.setNamingService(properties.namingServiceInstance());
		}
		if (!holder.alreadyInit[1]) {
			holder.setNamingMaintainService(properties.namingMaintainServiceInstance());
		}
	}

	static class ServiceHolder {
		private NamingService namingService = null;
		private NamingMaintainService namingMaintainService = null;

		final boolean[] alreadyInit = new boolean[2];

		private static final ServiceHolder HOLDER = new ServiceHolder();

		ServiceHolder() {
		}

		static ServiceHolder getInstance() {
			return HOLDER;
		}

		public NamingService getNamingService() {
			return namingService;
		}

		void setNamingService(NamingService namingService) {
			alreadyInit[0] = true;
			this.namingService = namingService;
		}

		NamingMaintainService getNamingMaintainService() {
			return namingMaintainService;
		}

		void setNamingMaintainService(NamingMaintainService namingMaintainService) {
			alreadyInit[1] = true;
			this.namingMaintainService = namingMaintainService;
		}
	}
}
