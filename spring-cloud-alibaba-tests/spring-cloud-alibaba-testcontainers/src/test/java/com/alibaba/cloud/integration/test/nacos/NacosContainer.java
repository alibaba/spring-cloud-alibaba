package com.alibaba.cloud.integration.test.nacos;

import com.alibaba.cloud.integration.common.ChaosContainer;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.alibaba.cloud.integration.common.nacos.Const.NACOS_SERVER_PORT;
import static java.lang.String.format;


public class NacosContainer<SelfT extends NacosContainer<SelfT>> extends ChaosContainer<SelfT> {

	
	public static final String DEFAULT_IMAGE_NAME = System.getenv().getOrDefault("TEST_IMAGE_NAME",
			"nacos");

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
			.withCommand(format("/bin/bash -c '</dev/tcp/localhost/%d'", NACOS_SERVER_PORT));
//				.withCreateContainerCmdModifier(
//					cmd -> cmd.withHostConfig(
//						new HostConfig()
//							.withPortBindings(new PortBinding(Ports.Binding.bindPort(8849), new ExposedPort(NACOS_SERVER_PORT)))));;

	}




}
