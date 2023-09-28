/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.istio;

import java.util.List;

import com.alibaba.cloud.commons.governance.ControlPlaneInitedBean;
import com.alibaba.cloud.commons.governance.event.GovernanceEvent;
import com.alibaba.cloud.governance.istio.bootstrap.Bootstrapper;
import com.alibaba.cloud.governance.istio.bootstrap.BootstrapperImpl;
import com.alibaba.cloud.governance.istio.filter.XdsResolveFilter;
import com.alibaba.cloud.governance.istio.filter.impl.AuthXdsResolveFilter;
import com.alibaba.cloud.governance.istio.filter.impl.RoutingXdsResolveFilter;
import com.alibaba.cloud.governance.istio.protocol.impl.CdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.EdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.RdsProtocol;
import com.alibaba.cloud.governance.istio.sds.IstioCertPairManager;
import com.alibaba.cloud.governance.istio.sds.SdsCertPairManager;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.istio.config.enabled", matchIfMissing = true)
@EnableConfigurationProperties(XdsConfigProperties.class)
// We need to auto config the class after all the governance data listener, to prevent
// event publisher hang permanently.
@AutoConfigureOrder(XdsAutoConfiguration.RESOURCE_TRANSFORM_AUTO_CONFIG_ORDER)
public class XdsAutoConfiguration {

	/**
	 * xds auto configuration log.
	 */
	private static final Logger log = LoggerFactory.getLogger(XdsAutoConfiguration.class);

	/**
	 * Order of xds auto config.
	 */
	public static final int RESOURCE_TRANSFORM_AUTO_CONFIG_ORDER = 100;

	@Autowired
	private XdsConfigProperties xdsConfigProperties;

	@Bean
	@ConditionalOnProperty(name = "spring.cloud.istio.config.use-agent",
			havingValue = "false")
	public IstioCertPairManager istioCertPairManager(
			XdsConfigProperties xdsConfigProperties) {
		return new IstioCertPairManager(xdsConfigProperties);
	}

	@Bean
	@ConditionalOnProperty(value = "spring.cloud.istio.config.use-agent",
			havingValue = "true")
	public SdsCertPairManager sdsCertPairManager(
			Bootstrapper.BootstrapInfo bootstrapInfo) {
		return new SdsCertPairManager(bootstrapInfo, xdsConfigProperties);
	}

	@Bean
	public DummyGovernanceDataListener dummyGovernanceDataListener() {
		return new DummyGovernanceDataListener();
	}

	@Bean
	public XdsChannel xdsChannel(
			@Autowired(required = false) IstioCertPairManager certManager,
			@Autowired(required = false) Bootstrapper.BootstrapInfo bootstrapInfo) {
		return new XdsChannel(xdsConfigProperties, certManager, bootstrapInfo);
	}

	@Bean
	public XdsResolveFilter<List<Listener>> authXdsResolveFilter() {
		return new AuthXdsResolveFilter();
	}

	@Bean
	public XdsResolveFilter<List<RouteConfiguration>> routingXdsResolveFilter() {
		return new RoutingXdsResolveFilter();
	}

	@Bean
	public AggregateDiscoveryService aggregateDiscoveryService(XdsChannel xdsChannel) {
		return new AggregateDiscoveryService(xdsChannel, xdsConfigProperties);
	}

	@Bean
	@ConditionalOnProperty(value = "spring.cloud.istio.config.use-agent",
			havingValue = "true")
	public Bootstrapper.BootstrapInfo bootstrapInfo() {
		BootstrapperImpl bootstrapper = new BootstrapperImpl();
		return bootstrapper.bootstrap();
	}

	@Bean
	public LdsProtocol ldsProtocol(List<XdsResolveFilter<List<Listener>>> filters,
			RdsProtocol rdsProtocol,
			AggregateDiscoveryService aggregateDiscoveryService) {
		LdsProtocol ldsProtocol = new LdsProtocol(xdsConfigProperties, filters,
				rdsProtocol, aggregateDiscoveryService);
		aggregateDiscoveryService.addProtocol(ldsProtocol);
		return ldsProtocol;
	}

	@Bean
	public CdsProtocol cdsProtocol(EdsProtocol edsProtocol, LdsProtocol ldsProtocol,
			AggregateDiscoveryService aggregateDiscoveryService) {
		CdsProtocol cdsProtocol = new CdsProtocol(xdsConfigProperties, edsProtocol,
				ldsProtocol, aggregateDiscoveryService);
		aggregateDiscoveryService.addProtocol(cdsProtocol);
		cdsProtocol.initAndObserve();
		return cdsProtocol;
	}

	@Bean
	public EdsProtocol edsProtocol(LdsProtocol ldsProtocol,
			AggregateDiscoveryService aggregateDiscoveryService) {
		EdsProtocol edsProtocol = new EdsProtocol(xdsConfigProperties, ldsProtocol,
				aggregateDiscoveryService);
		aggregateDiscoveryService.addProtocol(edsProtocol);
		return edsProtocol;
	}

	@Bean
	public RdsProtocol rdsProtocol(
			List<XdsResolveFilter<List<RouteConfiguration>>> filters,
			AggregateDiscoveryService aggregateDiscoveryService) {
		RdsProtocol rdsProtocol = new RdsProtocol(xdsConfigProperties, filters,
				aggregateDiscoveryService);
		aggregateDiscoveryService.addProtocol(rdsProtocol);
		return rdsProtocol;
	}

	@Bean
	@ConditionalOnMissingBean
	public ControlPlaneInitedBean controlPlaneInitedBean(CdsProtocol cdsProtocol) {
		return new ControlPlaneInitedBean();
	}

	/**
	 * To prevent the event publish hang permanently.
	 */
	private final class DummyGovernanceDataListener
			implements ApplicationListener<GovernanceEvent> {

		@Override
		public void onApplicationEvent(GovernanceEvent event) {
			if (log.isDebugEnabled()) {
				log.debug("Received governance event " + event.toString());
			}
		}

	}

}
