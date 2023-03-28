/*
 * Copyright 2013-2023 the original author or authors.
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

import javax.annotation.PostConstruct;

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

	private static final String IPV4_REGEX = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";

	private static final String IPV6_KEY = "IPv6";

	private String ipv6;

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private InetIPv6Utils inetIPv6Utils;

	@PostConstruct
	public void init() {
		String ip = nacosDiscoveryProperties.getIp();
		if (StringUtils.isNotEmpty(ip)) {
			this.ipv6 = Pattern.matches(IPV4_REGEX, ip)
					? nacosDiscoveryProperties.getMetadata().get(IPV6_KEY) : ip;
		}
		else {
			this.ipv6 = getAppLocalIPv6Address();
		}
	}

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
			instances = filterInstanceByIpType(instances);

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
			// When local support IPv6 address stack, referred to use IPv6 address.
			if (StringUtils.isNotEmpty(this.ipv6)) {
				convertIPv4ToIPv6(instance);
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

	private String getAppLocalIPv6Address() {
		return inetIPv6Utils.findIPv6Address();
	}

	private List<Instance> filterInstanceByIpType(List<Instance> instances) {
		if (StringUtils.isNotEmpty(this.ipv6)) {
			List<Instance> ipv6InstanceList = new ArrayList<>();
			for (Instance instance : instances) {
				if (Pattern.matches(IPV4_REGEX, instance.getIp())) {
					if (StringUtils.isNotEmpty(instance.getMetadata().get(IPV6_KEY))) {
						ipv6InstanceList.add(instance);
					}
				}
				else {
					ipv6InstanceList.add(instance);
				}
			}
			// provider has no IPv6, should use IPv4.
			if (ipv6InstanceList.size() == 0) {
				return instances.stream()
						.filter(instance -> Pattern.matches(IPV4_REGEX, instance.getIp()))
						.collect(Collectors.toList());
			}
			else {
				return ipv6InstanceList;
			}
		}
		return instances.stream()
				.filter(instance -> Pattern.matches(IPV4_REGEX, instance.getIp()))
				.collect(Collectors.toList());
	}

	/**
	 * There is two type Ip,using IPv6 should use IPv6 in metadata to replace IPv4 in IP
	 * field.
	 */
	private void convertIPv4ToIPv6(Instance instance) {
		if (Pattern.matches(IPV4_REGEX, instance.getIp())) {
			String ip = instance.getMetadata().get(IPV6_KEY);
			if (StringUtils.isNotEmpty(ip)) {
				instance.setIp(ip);
			}
		}
	}

}
