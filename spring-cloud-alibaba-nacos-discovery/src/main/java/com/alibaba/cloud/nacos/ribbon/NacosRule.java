package com.alibaba.cloud.nacos.ribbon;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cloud.nacos.NacosNamingManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;

/**
 * Supports preferentially calling the ribbon load balancing rules of the same cluster
 * instance.
 *
 * @author itmuch.com
 */
public class NacosRule extends AbstractLoadBalancerRule {
	private static final Logger LOGGER = LoggerFactory.getLogger(NacosRule.class);

	@Autowired
	private NacosDiscoveryProperties nacosDiscoveryProperties;

	@Autowired
	private NacosNamingManager nacosNamingManager;

	@Override
	public Server choose(Object key) {
		try {
			String clusterName = this.nacosDiscoveryProperties.getClusterName();
			DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
			String name = loadBalancer.getName();

			NamingService namingService = this.nacosNamingManager.getNamingService();
			List<Instance> instances = namingService.selectInstances(name, true);
			if (CollectionUtils.isEmpty(instances)) {
				LOGGER.warn("no instance in service {}", name);
				return null;
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
