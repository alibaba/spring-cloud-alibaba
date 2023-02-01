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

package com.alibaba.cloud.testsupport;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import org.springframework.core.io.ClassPathResource;

/**
 * Util class for starting the related container.
 *
 * @author freeman
 * @since 2021.0.1.0
 */
public class ContainerStarter {

	private static final String ROCKETMQ_BROKER_CONFIG_PATH = "rocketmq/conf/broker.conf";

	private static final String NACOS_VERSION = "1.4.2";
	private static final String SENTINEL_VERSION = "1.8.3";
	private static final String ROCKETMQ_VERSION = "4.9.2";
	private static final String SEATA_VERSION = "1.4.2";

	private static final Map<String, GenericContainer> nacosMap = new ConcurrentHashMap<>(
			4);
	private static final Map<String, GenericContainer> rocketmqMap = new ConcurrentHashMap<>(
			4);

	/**
	 * Start Nacos container, using default version.
	 */
	public static GenericContainer startNacos() {
		return startNacos(NACOS_VERSION);
	}

	/**
	 * Start RocketMQ container, using default version.
	 */
	public static GenericContainer startRocketmq() {
		return startRocketmq(ROCKETMQ_VERSION);
	}

	/**
	 * Start Nacos container, using specific version.
	 * @param version Nacos version
	 */
	public static GenericContainer startNacos(String version) {
		if (!nacosMap.containsKey(version)) {
			GenericContainer nacos = new GenericContainer("freemanlau/nacos:" + version)
					.withExposedPorts(8848).withEnv("MODE", "standalone")
					.withEnv("JVM_XMS", "256m").withEnv("JVM_XMX", "256m")
					.withEnv("JVM_XMN", "128m");
			nacos.start();
			nacosMap.put(version, nacos);
		}
		return nacosMap.get(version);
	}

	/**
	 * Start RocketMQ container, using specific version.
	 * @param version RocketMQ version
	 */
	public static GenericContainer startRocketmq(String version) {
		if (!rocketmqMap.containsKey(version)) {
			loadHostIp2BrokerConf();
			// this image exposes 4 ports, include namesrv and broker
			// we need use FixedHostPortGenericContainer !
			GenericContainer rocketmq = new FixedHostPortGenericContainer(
					"freemanlau/rocketmq:" + version).withFixedExposedPort(9876, 9876)
							.withFixedExposedPort(10909, 10909)
							.withFixedExposedPort(10911, 10911)
							.withFixedExposedPort(10912, 10912);
			rocketmq.withFileSystemBind(getAbsolutePath4BrokerConf(),
					"/home/rocketmq/rocketmq-" + version + "/conf/broker.conf");
			rocketmq.start();
			rocketmqMap.put(version, rocketmq);
		}
		return rocketmqMap.get(version);
	}

	private static void loadHostIp2BrokerConf() {
		try {
			ClassPathResource resource = new ClassPathResource(
					ROCKETMQ_BROKER_CONFIG_PATH);
			FileUtils.writeLines(resource.getFile(),
					Collections.singletonList("brokerIP1 = " + InetUtil.getHostIp()),
					true);
		}
		catch (IOException e) {
			throw new RuntimeException("load host ip to 'broker.conf' err !", e);
		}
	}

	private static String getAbsolutePath4BrokerConf() {
		try {
			ClassPathResource resource = new ClassPathResource(
					ROCKETMQ_BROKER_CONFIG_PATH);
			return resource.getFile().getAbsolutePath();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
