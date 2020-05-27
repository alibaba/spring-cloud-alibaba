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

package com.alibaba.cloud.nacos;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.spring.util.PropertySourcesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT_PORT;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMING_LOAD_CACHE_AT_START;
import static com.alibaba.nacos.api.PropertyKeyConst.PASSWORD;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.alibaba.nacos.api.PropertyKeyConst.USERNAME;

/**
 * @author dungu.zpf
 * @author xiaojing
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @author <a href="mailto:lyuzb@lyuzb.com">lyuzb</a>
 * @author <a href="mailto:78552423@qq.com">eshun</a>
 */
@ConfigurationProperties("spring.cloud.nacos.discovery")
public class NacosDiscoveryProperties {

	private static final Logger log = LoggerFactory
			.getLogger(NacosDiscoveryProperties.class);

	/**
	 * Prefix of {@link NacosDiscoveryProperties}.
	 */
	public static final String PREFIX = "spring.cloud.nacos.discovery";

	private static final Pattern PATTERN = Pattern.compile("-(\\w)");

	/**
	 * nacos discovery server address.
	 */
	private String serverAddr;

	/**
	 * the nacos authentication username.
	 */
	private String username;

	/**
	 * the nacos authentication password.
	 */
	private String password;

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
	 * watch delay,duration to pull new service from nacos server.
	 */
	private long watchDelay = 30000;

	/**
	 * nacos naming log file name.
	 */
	private String logName;

	/**
	 * service name to registry.
	 */
	@Value("${spring.cloud.nacos.discovery.service:${spring.application.name:}}")
	private String service;

	/**
	 * weight for service instance, the larger the value, the larger the weight.
	 */
	private float weight = 1;

	/**
	 * cluster name for nacos .
	 */
	private String clusterName = "DEFAULT";

	/**
	 * group name for nacos.
	 */
	private String group = "DEFAULT_GROUP";

	/**
	 * naming load from local cache at application start. true is load.
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
	 * which network interface's ip you want to register.
	 */
	private String networkInterface = "";

	/**
	 * The port your want to register for your service instance, needn't to set it if the
	 * auto detect port works well.
	 */
	private int port = -1;

	/**
	 * whether your service is a https service.
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

	/**
	 * Heart beat interval. Time unit: second.
	 */
	private Integer heartBeatInterval;

	/**
	 * Heart beat timeout. Time unit: second.
	 */
	private Integer heartBeatTimeout;

	/**
	 * Ip delete timeout. Time unit: second.
	 */
	private Integer ipDeleteTimeout;

	/**
	 * If instance is enabled to accept request. The default value is true.
	 */
	private boolean instanceEnabled = true;

	/**
	 * If instance is ephemeral.The default value is true.
	 */
	private boolean ephemeral = true;

	@Autowired
	private InetUtils inetUtils;

	@Autowired
	private Environment environment;

	private static NamingService namingService;

	private static NamingMaintainService namingMaintainService;

	@PostConstruct
	public void init() throws SocketException {
		namingService = null;

		metadata.put(PreservedMetadataKeys.REGISTER_SOURCE, "SPRING_CLOUD");
		if (secure) {
			metadata.put("secure", "true");
		}

		serverAddr = Objects.toString(serverAddr, "");
		if (serverAddr.endsWith("/")) {
			serverAddr = serverAddr.substring(0, serverAddr.length() - 1);
		}
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
				if (null == netInterface) {
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

	public Integer getHeartBeatInterval() {
		return heartBeatInterval;
	}

	public void setHeartBeatInterval(Integer heartBeatInterval) {
		this.heartBeatInterval = heartBeatInterval;
	}

	public Integer getHeartBeatTimeout() {
		return heartBeatTimeout;
	}

	public void setHeartBeatTimeout(Integer heartBeatTimeout) {
		this.heartBeatTimeout = heartBeatTimeout;
	}

	public Integer getIpDeleteTimeout() {
		return ipDeleteTimeout;
	}

	public void setIpDeleteTimeout(Integer ipDeleteTimeout) {
		this.ipDeleteTimeout = ipDeleteTimeout;
	}

	public String getNamingLoadCacheAtStart() {
		return namingLoadCacheAtStart;
	}

	public void setNamingLoadCacheAtStart(String namingLoadCacheAtStart) {
		this.namingLoadCacheAtStart = namingLoadCacheAtStart;
	}

	public long getWatchDelay() {
		return watchDelay;
	}

	public void setWatchDelay(long watchDelay) {
		this.watchDelay = watchDelay;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isInstanceEnabled() {
		return instanceEnabled;
	}

	public void setInstanceEnabled(boolean instanceEnabled) {
		this.instanceEnabled = instanceEnabled;
	}

	public boolean isEphemeral() {
		return ephemeral;
	}

	public void setEphemeral(boolean ephemeral) {
		this.ephemeral = ephemeral;
	}

	@Override
	public String toString() {
		return "NacosDiscoveryProperties{" + "serverAddr='" + serverAddr + '\''
				+ ", endpoint='" + endpoint + '\'' + ", namespace='" + namespace + '\''
				+ ", watchDelay=" + watchDelay + ", logName='" + logName + '\''
				+ ", service='" + service + '\'' + ", weight=" + weight
				+ ", clusterName='" + clusterName + '\'' + ", group='" + group + '\''
				+ ", namingLoadCacheAtStart='" + namingLoadCacheAtStart + '\''
				+ ", metadata=" + metadata + ", registerEnabled=" + registerEnabled
				+ ", ip='" + ip + '\'' + ", networkInterface='" + networkInterface + '\''
				+ ", port=" + port + ", secure=" + secure + ", accessKey='" + accessKey
				+ '\'' + ", secretKey='" + secretKey + '\'' + ", heartBeatInterval="
				+ heartBeatInterval + ", heartBeatTimeout=" + heartBeatTimeout
				+ ", ipDeleteTimeout=" + ipDeleteTimeout + '}';
	}

	public void overrideFromEnv(Environment env) {

		if (StringUtils.isEmpty(this.getServerAddr())) {
			String serverAddr = env
					.resolvePlaceholders("${spring.cloud.nacos.discovery.server-addr:}");
			if (StringUtils.isEmpty(serverAddr)) {
				serverAddr = env.resolvePlaceholders(
						"${spring.cloud.nacos.server-addr:localhost:8848}");
			}
			this.setServerAddr(serverAddr);
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
					"${spring.cloud.nacos.discovery.cluster-name:}"));
		}
		if (StringUtils.isEmpty(this.getEndpoint())) {
			this.setEndpoint(
					env.resolvePlaceholders("${spring.cloud.nacos.discovery.endpoint:}"));
		}
		if (StringUtils.isEmpty(this.getGroup())) {
			this.setGroup(
					env.resolvePlaceholders("${spring.cloud.nacos.discovery.group:}"));
		}
		if (StringUtils.isEmpty(this.getUsername())) {
			this.setUsername(env.resolvePlaceholders("${spring.cloud.nacos.username:}"));
		}
		if (StringUtils.isEmpty(this.getPassword())) {
			this.setPassword(env.resolvePlaceholders("${spring.cloud.nacos.password:}"));
		}
	}

	public NamingService namingServiceInstance() {

		if (null != namingService) {
			return namingService;
		}

		try {
			namingService = NacosFactory.createNamingService(getNacosProperties());
		}
		catch (Exception e) {
			log.error("create naming service error!properties={},e=,", this, e);
			return null;
		}
		return namingService;
	}

	@Deprecated
	public NamingMaintainService namingMaintainServiceInstance() {

		if (null != namingMaintainService) {
			return namingMaintainService;
		}

		try {
			namingMaintainService = NamingMaintainFactory
					.createMaintainService(getNacosProperties());
		}
		catch (Exception e) {
			log.error("create naming service error!properties={},e=,", this, e);
			return null;
		}
		return namingMaintainService;
	}

	private Properties getNacosProperties() {
		Properties properties = new Properties();
		properties.put(SERVER_ADDR, serverAddr);
		properties.put(USERNAME, Objects.toString(username, ""));
		properties.put(PASSWORD, Objects.toString(password, ""));
		properties.put(NAMESPACE, namespace);
		properties.put(UtilAndComs.NACOS_NAMING_LOG_NAME, logName);

		if (endpoint.contains(":")) {
			int index = endpoint.indexOf(":");
			properties.put(ENDPOINT, endpoint.substring(0, index));
			properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
		}
		else {
			properties.put(ENDPOINT, endpoint);
		}

		properties.put(ACCESS_KEY, accessKey);
		properties.put(SECRET_KEY, secretKey);
		properties.put(CLUSTER_NAME, clusterName);
		properties.put(NAMING_LOAD_CACHE_AT_START, namingLoadCacheAtStart);

		enrichNacosDiscoveryProperties(properties);
		return properties;
	}

	private void enrichNacosDiscoveryProperties(Properties nacosDiscoveryProperties) {
		Map<String, Object> properties = PropertySourcesUtils
				.getSubProperties((ConfigurableEnvironment) environment, PREFIX);
		properties.forEach((k, v) -> nacosDiscoveryProperties.putIfAbsent(resolveKey(k),
				String.valueOf(v)));
	}

	private String resolveKey(String key) {
		Matcher matcher = PATTERN.matcher(key);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

}
