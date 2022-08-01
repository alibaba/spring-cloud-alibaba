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

package com.alibaba.cloud.appactive.consumer;

import java.text.MessageFormat;
import java.util.List;

import com.alibaba.cloud.appactive.common.ServiceMeta;
import com.alibaba.cloud.appactive.common.UriContext;
import com.alibaba.cloud.appactive.consumer.callback.SpringCloud2AddressCallBack;
import com.alibaba.cloud.appactive.util.Util;
import com.alibaba.fastjson.JSON;
import com.netflix.loadbalancer.Server;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.java.api.base.enums.MiddleWareTypeEnum;
import io.appactive.java.api.base.exception.ExceptionFactory;
import io.appactive.java.api.bridge.rpc.constants.constant.RPCConstant;
import io.appactive.java.api.utils.lang.StringUtils;
import io.appactive.support.lang.CollectionUtils;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class ConsumerRouter {

	private static final Logger logger = LogUtil.getLogger();

	private static final ServerMeta serverMeta;

	private static final SpringCloudAddressFilterByUnitServiceImpl<Server> addressFilterByUnitService;

	static {
		String baseName = "io.appactive.rpc.springcloud.nacos.consumer.";
		String[] classes = new String[] { "NacosServerMeta", "EurekaServerMeta", };
		serverMeta = loadServerMeta(baseName, classes);
		if (serverMeta == null) {
			String msg = MessageFormat
					.format("No available ServerMeta among classes: {0}", classes);
			throw ExceptionFactory.makeFault(msg);
		}
		else {
			logger.info("filter ServerMeta found: {}", serverMeta.getClass().getName());
			addressFilterByUnitService = new SpringCloudAddressFilterByUnitServiceImpl<>(
					MiddleWareTypeEnum.SPRING_CLOUD);
			addressFilterByUnitService
					.initAddressCallBack(new SpringCloud2AddressCallBack<>(serverMeta));
		}
	}

	private static ServerMeta loadServerMeta(String baseName, String[] classNames) {
		for (String className : classNames) {
			try {
				Class clazz = Class.forName(baseName + className);
				return (ServerMeta) clazz.newInstance();
			}
			catch (ClassNotFoundException | IllegalAccessException
					| InstantiationException e) {

			}
		}
		return null;
	}

	/**
	 * return qualified server subset from origin list
	 * @param servers origin server list
	 * @return qualified server list
	 */
	public static List<Server> filter(List<Server> servers) {
		if (CollectionUtils.isEmpty(servers)) {
			return servers;
		}
		Server oneServer = servers.get(0);
		String appName = oneServer.getMetaInfo().getAppName();
		String servicePrimaryKey = Util.buildServicePrimaryName(appName,
				UriContext.getUriPath());

		/// We all ready make sure all service stored through ConsumerRouter.refresh and
		/// URIRegister.doRegisterUris,
		// so there is no need to call method bellow
		// addressFilterByUnitService.refreshAddressList(null, servicePrimaryKey, servers,
		/// version);
		List<Server> list = addressFilterByUnitService.addressFilter(null,
				servicePrimaryKey, AppContextClient.getRouteId());
		return list;
	}

	/**
	 * server size changes or server meta changes.
	 * @param servers new servers
	 * @return updated number
	 */
	public static synchronized Integer refresh(List<Server> servers) {
		Integer changed = 0;
		if (CollectionUtils.isEmpty(servers)) {
			return changed;
		}
		Server oneServer = servers.get(0);
		String appName = oneServer.getMetaInfo().getAppName();
		String version = serverMeta.getMetaMap(oneServer)
				.get(RPCConstant.SPRING_CLOUD_SERVICE_META_VERSION);

		String metaMapValue = addressFilterByUnitService.getMetaMapFromServer(oneServer,
				RPCConstant.SPRING_CLOUD_SERVICE_META);
		if (StringUtils.isBlank(metaMapValue)) {
			return changed;
		}
		List<ServiceMeta> serviceMetaList = JSON.parseArray(metaMapValue,
				ServiceMeta.class);
		if (CollectionUtils.isEmpty(serviceMetaList)) {
			return changed;
		}
		for (ServiceMeta serviceMeta : serviceMetaList) {
			String servicePrimaryKey = Util.buildServicePrimaryName(appName,
					serviceMeta.getUriPrefix());
			if (addressFilterByUnitService.refreshAddressList(null, servicePrimaryKey,
					servers, version, serviceMeta.getRa())) {
				changed++;
			}
		}
		return changed;
	}

}
