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

package org.springframework.cloud.alicloud.context.ans;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.context.AliCloudServerMode;
import com.alibaba.cloud.context.ans.AnsConfiguration;

/**
 * @author xiaolongzuo
 */
@ConfigurationProperties("spring.cloud.alicloud.ans")
public class AnsProperties implements AnsConfiguration {

	/**
	 * 服务端模式，默认为LOCAL
	 */
	private AliCloudServerMode serverMode = AliCloudServerMode.LOCAL;

	/**
	 * 服务端列表
	 */
	private String serverList = "127.0.0.1";

	/**
	 * 服务端列表
	 */
	private String serverPort = "8080";

	/**
	 * 注册的服务名，默认从 spring.cloud.alicloud.ans.doms 中获取，当没有配置时，使用 spring.application.name
	 */
	@Value("${spring.cloud.alicloud.ans.client-domains:${spring.application.name:}}")
	private String clientDomains;

	/**
	 * 注册服务的权重，从配置 spring.cloud.alicloud.ans.weight 中获取，默认为 1
	 */
	private float clientWeight = 1;

	/**
	 * 当存在多个doms，需要对应不同的 weight 时，通过 spring.cloud.alicloud.ans.weight.dom1=weight1 的方式配置
	 */
	private Map<String, Float> clientWeights = new HashMap<String, Float>();

	/**
	 * 注册服务的 token ，从 spring.cloud.alicloud.ans.token 中获取
	 */
	private String clientToken;

	/**
	 * 当存在多个doms，需要对应不同的token时，通过 spring.cloud.alicloud.ans.tokens.dom1=token1 的方式配置
	 */
	private Map<String, String> clientTokens = new HashMap<String, String>();

	/**
	 * 配置注册到哪个集群，从 spring.cloud.alicloud.ans.cluster 中获取，默认为 DEFAULT
	 */
	private String clientCluster = "DEFAULT";

	/**
	 * metadata 实现 serviceInstance 接口所需的字段，但 ans 目前尚不支持此字段，配置了也没用
	 */
	private Map<String, String> clientMetadata = new HashMap<>();

	/**
	 * 默认打开注册，可以通过 spring.cloud.alicloud.ans.register-enabled=false 的配置来关闭注册
	 */
	private boolean registerEnabled = true;

	/**
	 * 想要发布的服务的ip，从 spring.cloud.alicloud.ans.client-ip 中获取
	 */
	private String clientIp;

	/**
	 * 想要发布的服务的ip从哪一块网卡中获取
	 */
	private String clientInterfaceName;

	/**
	 * 想要发布的服务的端口，从 spring.cloud.alicloud.ans.port 中获取
	 */
	private int clientPort = -1;

	/**
	 * 租户下的环境隔离配置，相同租户的相同环境下的服务才能互相发现
	 */
	@Value("${spring.cloud.alicloud.ans.env:${env.id:DEFAULT}}")
	private String env;

	/**
	 * 是否注册成 https 的形式，通过 spring.cloud.alicloud.ans.secure 来配置，默认为false
	 */
	private boolean secure = false;

	@Autowired
	private InetUtils inetUtils;

	private Map<String, String> tags = new HashMap<>();

	@PostConstruct
	public void init() throws SocketException {

		// 增加注册类型，标记为 spring cloud 应用
		tags.put("ANS_SERVICE_TYPE", "SPRING_CLOUD");

		if (StringUtils.isEmpty(clientIp)) {
			// 如果没有指定注册的ip对应的网卡名，则通过遍历网卡去获取
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
