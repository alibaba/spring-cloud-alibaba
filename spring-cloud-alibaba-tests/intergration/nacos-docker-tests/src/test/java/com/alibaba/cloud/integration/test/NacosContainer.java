package com.alibaba.cloud.integration.test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.alibaba.cloud.integration.common.ChaosContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import static com.alibaba.cloud.integration.common.nacos.Const.DEFAULT_IMAGE_NAME;
import static com.alibaba.cloud.integration.common.nacos.Const.NACOS_SERVER_PORT;


public class NacosContainer<SelfT extends NacosContainer<SelfT>> extends ChaosContainer<SelfT> {

	@Override
	protected void configure() {
		super.configure();
		this.withNetworkAliases(DEFAULT_IMAGE_NAME)
				.withExposedPorts(NACOS_SERVER_PORT)
				.withCreateContainerCmdModifier( createContainerCmd -> {
					createContainerCmd.withHostName(DEFAULT_IMAGE_NAME);
					createContainerCmd.withName(clusterName + "-" + DEFAULT_IMAGE_NAME);
				} );

	}

	public NacosContainer(String clusterName, String image) {
		super(clusterName, image);
		withExposedPorts(NACOS_SERVER_PORT)
				.withCommand("./startup.sh -m standalone");
	}




}
