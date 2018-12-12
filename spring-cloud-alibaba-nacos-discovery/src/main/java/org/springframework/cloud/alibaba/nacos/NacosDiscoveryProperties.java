/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import javax.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import static com.alibaba.nacos.api.PropertyKeyConst.*;

/**
 * @author dungu.zpf
 * @author xiaojing
 */

@ConfigurationProperties("spring.cloud.nacos.discovery")
public class NacosDiscoveryProperties {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NacosDiscoveryProperties.class);

	/**
	 * nacos discovery server address
	 */
	private String serverAddr;

	/**
	 * the domain name of a service, through which the server address can be dynamically
	 * obtained.
	 */
	private String endpoint;

	/**
	 * namespace, separation registry of different environments.
	 */
	private String namespace;

	/**
	 * nacos naming log file name
	 */
	private String logName;

	/**
	 * service name to registry
	 */
	@Value("${spring.cloud.nacos.discovery.service:${spring.application.name:}}")
	private String service;

	/**
	 * weight for service instance, the larger the value, the larger the weight.
	 */
	private float weight = 1;

	/**
	 * cluster name for nacos server.
	 */
	private String clusterName = "DEFAULT";

	/**
	 * naming load from local cache at application start. true is load
	 */
	private String namingLoadCacheAtStart = "false";

	/**
	 * extra metadata to register.
	 */
	private Map<String, String> metadata = new HashMap<>();

	/**
	 * if you just want to subscribe, but don't want to register your service, set it to
	 * false.
	 */
	private boolean registerEnabled = true;

	/**
	 * The ip address your want to register for your service instance, needn't to set it
	 * if the auto detect ip works well.
	 */
	private String ip;

	/**
	 * which network interface's ip you want to register
	 */
	private String networkInterface = "";

	/**
	 * The port your want to register for your service instance, needn't to set it if the
	 * auto detect port works well
	 */
	private int port = -1;

	/**
	 * whether your service is a https service
	 */
	private boolean secure = false;

	/**
	 * access key for namespace.
	 */
	private String accessKey;

	/**
	 * secret key for namespace.
	 */
	private String secretKey;

	@Autowired
	private InetUtils inetUtils;

	@Autowired
	private Environment environment;

	private NamingService namingService;

	@PostConstruct
	public void init() throws SocketException {

		serverAddr = Objects.toString(serverAddr, "");
		endpoint = Objects.toString(endpoint, "");
		namespace = Objects.toString(namespace, "");
		logName = Objects.toString(logName, "");

		if (StringUtils.isEmpty(ip)) {
			// traversing network interfaces if didn't specify a interface
			if (StringUtils.isEmpty(networkInterface)) {
				ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
			}
			else {
				NetworkInterface netInterface = NetworkInterface
						.getByName(networkInterface);
				if (null == networkInterface) {
					throw new IllegalArgumentException(
							"no such interface " + networkInterface);
				}

				Enumeration<InetAddress> inetAddress = netInterface.getInetAddresses();
				while (inetAddress.hasMoreElements()) {
					InetAddress currentAddress = inetAddress.nextElement();
					if (currentAddress instanceof Inet4Address
							&& !currentAddress.isLoopbackAddress()) {
						ip = currentAddress.getHostAddress();
						break;
					}
				}

				if (StringUtils.isEmpty(ip)) {
					throw new RuntimeException("cannot find available ip from"
							+ " network interface " + networkInterface);
				}

			}
		}

		this.overrideFromEnv(environment);
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public void setInetUtils(InetUtils inetUtils) {
		this.inetUtils = inetUtils;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public boolean isRegisterEnabled() {
		return registerEnabled;
	}

	public void setRegisterEnabled(boolean registerEnabled) {
		this.registerEnabled = registerEnabled;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(String networkInterface) {
		this.networkInterface = networkInterface;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getNamingLoadCacheAtStart() {
		return namingLoadCacheAtStart;
	}

	public void setNamingLoadCacheAtStart(String namingLoadCacheAtStart) {
		this.namingLoadCacheAtStart = namingLoadCacheAtStart;
	}

	@Override
	public String toString() {
		return "NacosDiscoveryProperties{" + "serverAddr='" + serverAddr + '\''
				+ ", endpoint='" + endpoint + '\'' + ", namespace='" + namespace + '\''
				+ ", logName='" + logName + '\'' + ", service='" + service + '\''
				+ ", weight=" + weight + ", clusterName='" + clusterName + '\''
				+ ", metadata=" + metadata + ", registerEnabled=" + registerEnabled
				+ ", ip='" + ip + '\'' + ", networkInterface='" + networkInterface + '\''
				+ ", port=" + port + ", secure=" + secure + ", accessKey='" + accessKey
				+ ", namingLoadCacheAtStart=" + namingLoadCacheAtStart + '\''
				+ ", secretKey='" + secretKey + '\'' + '}';
	}

	public void overrideFromEnv(Environment env) {

		if (StringUtils.isEmpty(this.getServerAddr())) {
			this.setServerAddr(env
					.resolvePlaceholders("${spring.cloud.nacos.discovery.server-addr:}"));
		}
		if (StringUtils.isEmpty(this.getNamespace())) {
			this.setNamespace(env
					.resolvePlaceholders("${spring.cloud.nacos.discovery.namespace:}"));
		}
		if (StringUtils.isEmpty(this.getAccessKey())) {
			this.setAccessKey(env
					.resolvePlaceholders("${spring.cloud.nacos.discovery.access-key:}"));
		}
		if (StringUtils.isEmpty(this.getSecretKey())) {
			this.setSecretKey(env
					.resolvePlaceholders("${spring.cloud.nacos.discovery.secret-key:}"));
		}
		if (StringUtils.isEmpty(this.getLogName())) {
			this.setLogName(
					env.resolvePlaceholders("${spring.cloud.nacos.discovery.log-name:}"));
		}
		if (StringUtils.isEmpty(this.getClusterName())) {
			this.setClusterName(env.resolvePlaceholders(
					"${spring.cloud.nacos.discovery.clusterName-name:}"));
		}
		if (StringUtils.isEmpty(this.getEndpoint())) {
			this.setEndpoint(
					env.resolvePlaceholders("${spring.cloud.nacos.discovery.endpoint:}"));
		}
	}

	public NamingService namingServiceInstance() {

		if (null != namingService) {
			return namingService;
		}

		Properties properties = new Properties();
		properties.put(SERVER_ADDR, serverAddr);
		properties.put(NAMESPACE, namespace);
		properties.put(UtilAndComs.NACOS_NAMING_LOG_NAME, logName);
		properties.put(ENDPOINT, endpoint);
		properties.put(ACCESS_KEY, accessKey);
		properties.put(SECRET_KEY, secretKey);
		properties.put(CLUSTER_NAME, clusterName);
		properties.put(NAMING_LOAD_CACHE_AT_START, namingLoadCacheAtStart);

		try {
			namingService = NacosFactory.createNamingService(properties);
			return namingService;
		}
		catch (Exception e) {
			LOGGER.error("create naming service error!properties={},e=,", this, e);
			return null;
		}
	}

}
