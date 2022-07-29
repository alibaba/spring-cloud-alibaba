package com.alibaba.cloud.integration.test.nacos;

import com.alibaba.cloud.integration.common.ChaosContainer;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.integration.common.nacos.Const.LOCAL_SERVER_PORT;
import static com.alibaba.cloud.integration.common.nacos.Const.NACOS_SERVER_PORT;
import static java.lang.String.format;

public class NacosContainer<SelfT extends NacosContainer<SelfT>>
		extends ChaosContainer<SelfT> {

	public static final String DEFAULT_IMAGE_NAME = System.getenv()
			.getOrDefault("TEST_IMAGE_NAME", "nacos");

	public NacosContainer(String clusterName, String image) {
		super(clusterName, image);
		Map<String,String> envKey = new HashMap<>();
		envKey.put("MODE", "standalone");
				withEnv(envKey)
						.withCommand(
				format("/bin/bash -c '</dev/tcp/localhost/%d'", NACOS_SERVER_PORT))
						.withCommand("-p", LOCAL_SERVER_PORT+":"+NACOS_SERVER_PORT)
						.withCommand( "--name","nacos-quick",
									"-d", image
						);
				
	}

	@Override
	protected void configure() {
		super.configure();
		this.withNetworkAliases(DEFAULT_IMAGE_NAME)
				.withCreateContainerCmdModifier(createContainerCmd -> {
					createContainerCmd.withHostName(DEFAULT_IMAGE_NAME);
					createContainerCmd.withName(clusterName + "-" + DEFAULT_IMAGE_NAME);
					createContainerCmd.withHostConfig(new HostConfig().withPortBindings(
							new PortBinding(Ports.Binding.bindPort(NACOS_SERVER_PORT), new ExposedPort(LOCAL_SERVER_PORT))));
				});
	}

}
