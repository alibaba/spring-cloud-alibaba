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

package com.alibaba.cloud.nacos.ribbon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.util.InetIPv6Utils;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.util.CollectionUtils;

/**
 * Supports preferentially calling the ribbon load balancing rules of the same cluster
 * instance.
 *
 * @author itmuch.com
 * @author HH
 */
public class NacosRule extends AbstractLoadBalancerRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(NacosRule.class);

	private static final String IPV4_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
			+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
			+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
			+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private InetIPv6Utils inetIPv6Utils;

	@Autowired
	private InetUtils inetUtils;

	@Override
	public Server choose(Object key) {
		try {
			String clusterName = this.nacosDiscoveryProperties.getClusterName();
			String group = this.nacosDiscoveryProperties.getGroup();
			DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
			String name = loadBalancer.getName();

			NamingService namingService = nacosServiceManager.getNamingService();
			List<Instance> instances = namingService.selectInstances(name, group, true);
			if (CollectionUtils.isEmpty(instances)) {
				LOGGER.warn("no instance in service {}", name);
				return null;
			}

			String ipv4 = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
			String ipv6 = inetIPv6Utils.findIPv6Address();
			if (!StringUtils.isEmpty(ipv6)) {
				List<Instance> instanceList = new ArrayList<>();
				for (Instance instance : instances) {
					if (Pattern.compile(IPV4_REGEX).matcher(instance.getIp()).matches()) {
						if (!StringUtils.isEmpty(instance.getMetadata().get("IPv6"))) {
							instanceList.add(instance);
						}
					}
					else {
						instanceList.add(instance);
					}
				}
				if (instanceList.size() == 0) {
					instances = instances.stream()
							.filter(instance -> Pattern.compile(IPV4_REGEX)
									.matcher(instance.getIp()).matches())
							.collect(Collectors.toList());
				}
				else {
					instances = instanceList;
				}
			}
			else if (StringUtils.isEmpty(ipv6) && !StringUtils.isEmpty(ipv4)) {
				instances = instances.stream()
						.filter(instance -> Pattern.compile(IPV4_REGEX)
								.matcher(instance.getIp()).matches())
						.collect(Collectors.toList());
			}

			List<Instance> instancesToChoose = instances;
			if (StringUtils.isNotBlank(clusterName)) {
				List<Instance> sameClusterInstances = instances.stream()
						.filter(instance -> Objects.equals(clusterName,
								instance.getClusterName()))
						.collect(Collectors.toList());
				if (!CollectionUtils.isEmpty(sameClusterInstances)) {
					instancesToChoose = sameClusterInstances;
				}
				else {
					LOGGER.warn(
							"A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}",
							name, clusterName, instances);
				}
			}

			Instance instance = ExtendBalancer.getHostByRandomWeight2(instancesToChoose);
			if (Pattern.compile(IPV4_REGEX).matcher(instance.getIp()).matches()) {
				String ip = instance.getMetadata().get("IPv6");
				if (!StringUtils.isEmpty(ip)) {
					instance.setIp(ip);
				}
			}

			return new NacosServer(instance);
		}
		catch (Exception e) {
			LOGGER.warn("NacosRule error", e);
			return null;
		}
	}

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {
	}

}
