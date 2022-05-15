package com.alibaba.cloud.integration.test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.alibaba.cloud.integration.common.ChaosContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;


public class NacosContainer<SelfT extends NacosContainer<SelfT>> extends ChaosContainer<SelfT> {


	private static final String NAME = "nacos";

	private static final Integer NACOS_PORT = 8092;

	private static final Integer NACOS_SERVER_PORT = 8848;

	public static final String DEFAULT_IMAGE_NAME = System.getenv().getOrDefault("TEST_IMAGE_NAME",
			"");

	@Override
	protected void configure() {
		super.configure();
		this.withNetworkAliases(NAME)
				.withExposedPorts(NACOS_PORT)
				.withCreateContainerCmdModifier( createContainerCmd -> {
					createContainerCmd.withHostName(NAME);
					createContainerCmd.withName(clusterName + "-" + NAME);
				} );

	}

	public NacosContainer(String clusterName, String image) {
		super(clusterName, image);
		withExposedPorts(NACOS_PORT)
				.withCommand("./startup.sh -m standalone")
				//定义前置动作 nacos-server是否启动
				.waitingFor(new HttpWaitStrategy()
						.forPort(NACOS_SERVER_PORT)
						.forStatusCode(200)
						.withStartupTimeout(Duration.of(300, ChronoUnit.SECONDS)));

	}




}
