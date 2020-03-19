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

package com.alibaba.alicloud.context.ans;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.alibaba.cloud.context.AliCloudServerMode;
import com.alibaba.cloud.context.ans.AnsConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.util.StringUtils;

/**
 * @author xiaolongzuo
 */
@ConfigurationProperties("spring.cloud.alicloud.ans")
public class AnsProperties implements AnsConfiguration {

	/**
	 * Server side mode，the default is LOCAL.
	 */
	private AliCloudServerMode serverMode = AliCloudServerMode.LOCAL;

	/**
	 * Server list.
	 */
	private String serverList = "127.0.0.1";

	/**
	 * Server port.
	 */
	private String serverPort = "8080";

	/**
	 * Service names，default value is ${spring.cloud.alicloud.ans.doms}. When not
	 * configured, use ${spring.application.name}.
	 */
	@Value("${spring.cloud.alicloud.ans.client-domains:${spring.application.name:}}")
	private String clientDomains;

	/**
	 * The weight of the registration service, obtained from the configuration
	 * ${spring.cloud.alicloud.ans.weight}, the default is 1.
	 */
	private float clientWeight = 1;

	/**
	 * When there are multiple doms and need to correspond to different weights, configure
	 * them by spring.cloud.alicloud.ans.weight.dom1=weight1.
	 */
	private Map<String, Float> clientWeights = new HashMap<String, Float>();

	/**
	 * The token of the registration service, obtained from
	 * ${spring.cloud.alicloud.ans.token}.
	 */
	private String clientToken;

	/**
	 * When there are multiple doms and need to correspond to different tokens, configure
	 * them by spring.cloud.alicloud.ans.tokens.dom1=token1.
	 */
	private Map<String, String> clientTokens = new HashMap<String, String>();

	/**
	 * Configure which cluster to register with, obtained from
	 * ${spring.cloud.alicloud.ans.cluster}, defaults to DEFAULT.
	 */
	private String clientCluster = "DEFAULT";

	/**
	 * Temporarily not supported, reserved fields.
	 */
	private Map<String, String> clientMetadata = new HashMap<>();

	/**
	 * Registration is turned on by default, and registration can be turned off by the
	 * configuration of spring.cloud.alicloud.ans.register-enabled=false.
	 */
	private boolean registerEnabled = true;

	/**
	 * The ip of the service you want to publish, obtained from
	 * ${spring.cloud.alicloud.ans.client-ip}.
	 */
	private String clientIp;

	/**
	 * Configure which NIC the ip of the service you want to publish is obtained from.
	 */
	private String clientInterfaceName;

	/**
	 * The port of the service you want to publish.
	 */
	private int clientPort = -1;

	/**
	 * The environment isolation configuration under the tenant, the services in the same
	 * environment of the same tenant can discover each other.
	 */
	@Value("${spring.cloud.alicloud.ans.env:${env.id:DEFAULT}}")
	private String env;

	/**
	 * Whether to register as https, configured by ${spring.cloud.alicloud.ans.secure},
	 * default is false.
	 */
	private boolean secure = false;

	@Autowired
	private InetUtils inetUtils;

	private Map<String, String> tags = new HashMap<>();

	@PostConstruct
	public void init() throws SocketException {

		// Marked as spring cloud application
		tags.put("ANS_SERVICE_TYPE", "SPRING_CLOUD");

		if (StringUtils.isEmpty(clientIp)) {
			if (StringUtils.isEmpty(clientInterfaceName)) {
				clientIp = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
			}
			else {
				NetworkInterface networkInterface = NetworkInterface
						.getByName(clientInterfaceName);
				if (null == networkInterface) {
					throw new RuntimeException(
							"no such network interface " + clientInterfaceName);
				}

				Enumeration<InetAddress> inetAddress = networkInterface
						.getInetAddresses();
				while (inetAddress.hasMoreElements()) {
					InetAddress currentAddress = inetAddress.nextElement();
					if (currentAddress instanceof Inet4Address
							&& !currentAddress.isLoopbackAddress()) {
						clientIp = currentAddress.getHostAddress();
						break;
					}
				}

				if (StringUtils.isEmpty(clientIp)) {
					throw new RuntimeException(
							"cannot find available ip from network interface "
									+ clientInterfaceName);
				}

			}
		}
	}

	@Override
	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	@Override
	public String getServerList() {
		return serverList;
	}

	public void setServerList(String serverList) {
		this.serverList = serverList;
	}

	@Override
	public boolean isRegisterEnabled() {
		return registerEnabled;
	}

	public void setRegisterEnabled(boolean registerEnabled) {
		this.registerEnabled = registerEnabled;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	@Override
	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	@Override
	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	@Override
	public AliCloudServerMode getServerMode() {
		return serverMode;
	}

	public void setServerMode(AliCloudServerMode serverMode) {
		this.serverMode = serverMode;
	}

	@Override
	public String getClientDomains() {
		return clientDomains;
	}

	public void setClientDomains(String clientDomains) {
		this.clientDomains = clientDomains;
	}

	@Override
	public float getClientWeight() {
		return clientWeight;
	}

	public void setClientWeight(float clientWeight) {
		this.clientWeight = clientWeight;
	}

	@Override
	public Map<String, Float> getClientWeights() {
		return clientWeights;
	}

	public void setClientWeights(Map<String, Float> clientWeights) {
		this.clientWeights = clientWeights;
	}

	@Override
	public String getClientToken() {
		return clientToken;
	}

	public void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	@Override
	public Map<String, String> getClientTokens() {
		return clientTokens;
	}

	public void setClientTokens(Map<String, String> clientTokens) {
		this.clientTokens = clientTokens;
	}

	@Override
	public String getClientCluster() {
		return clientCluster;
	}

	public void setClientCluster(String clientCluster) {
		this.clientCluster = clientCluster;
	}

	@Override
	public Map<String, String> getClientMetadata() {
		return clientMetadata;
	}

	public void setClientMetadata(Map<String, String> clientMetadata) {
		this.clientMetadata = clientMetadata;
	}

	@Override
	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	@Override
	public String getClientInterfaceName() {
		return clientInterfaceName;
	}

	public void setClientInterfaceName(String clientInterfaceName) {
		this.clientInterfaceName = clientInterfaceName;
	}

	@Override
	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	@Override
	public String toString() {
		return "AnsProperties{" + "doms='" + clientDomains + '\'' + ", weight="
				+ clientWeight + ", weights=" + clientWeights + ", token='" + clientToken
				+ '\'' + ", tokens=" + clientTokens + ", cluster='" + clientCluster + '\''
				+ ", metadata=" + clientMetadata + ", registerEnabled=" + registerEnabled
				+ ", ip='" + clientIp + '\'' + ", interfaceName='" + clientInterfaceName
				+ '\'' + ", port=" + clientPort + ", env='" + env + '\'' + ", secure="
				+ secure + ", tags=" + tags + '}';
	}

}
