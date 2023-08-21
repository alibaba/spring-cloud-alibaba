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

package com.alibaba.cloud.security;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @author <a href="liweijian@foxmail.com">lwj</a>
 */
@ConfigurationProperties(SecurityConfigProperties.PREFIX)
public class SecurityConfigProperties {
	/**
	 * Spring Cloud Security PREFIX.
	 */
	public static final String PREFIX = "spring.cloud.security";


	private Boolean open;

	private String host;

	private Integer port;

	private String jwtPolicy;

	private String istiodToken;

	private String caAddr;

	private String caCertPath;

	private String namespace;

	private String podName;

	private String clusterId;

	private String asymCryptoType;

	private String hashType;

	private Integer getCertTimeoutS;

	private Integer certValidityTimeS;

	private Float certPeriodRatio;

	private Integer reconnectionDelayS;

	private Integer initAwaitTimeS;

	public Boolean isOpen() {
		return open;
	}

	public void setOpen(Boolean open) {
		this.open = open;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getJwtPolicy() {
		return jwtPolicy;
	}

	public void setJwtPolicy(String jwtPolicy) {
		this.jwtPolicy = jwtPolicy;
	}

	public String getIstiodToken() {
		return istiodToken;
	}

	public void setIstiodToken(String istiodToken) {
		this.istiodToken = istiodToken;
	}

	public String getCaAddr() {
		return caAddr;
	}

	public void setCaAddr(String caAddr) {
		this.caAddr = caAddr;
	}

	public String getCaCertPath() {
		return caCertPath;
	}

	public void setCaCertPath(String caCertPath) {
		this.caCertPath = caCertPath;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getPodName() {
		return podName;
	}

	public void setPodName(String podName) {
		this.podName = podName;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getAsymCryptoType() {
		return asymCryptoType;
	}

	public void setAsymCryptoType(String asymCryptoType) {
		this.asymCryptoType = asymCryptoType;
	}

	public String getHashType() {
		return hashType;
	}

	public void setHashType(String hashType) {
		this.hashType = hashType;
	}

	public Integer getGetCertTimeoutS() {
		return getCertTimeoutS;
	}

	public void setGetCertTimeoutS(Integer getCertTimeoutS) {
		this.getCertTimeoutS = getCertTimeoutS;
	}

	public Integer getCertValidityTimeS() {
		return certValidityTimeS;
	}

	public void setCertValidityTimeS(Integer certValidityTimeS) {
		this.certValidityTimeS = certValidityTimeS;
	}

	public Float getCertPeriodRatio() {
		return certPeriodRatio;
	}

	public void setCertPeriodRatio(Float certPeriodRatio) {
		this.certPeriodRatio = certPeriodRatio;
	}

	public Integer getReconnectionDelayS() {
		return reconnectionDelayS;
	}

	public void setReconnectionDelayS(Integer reconnectionDelayS) {
		this.reconnectionDelayS = reconnectionDelayS;
	}

	public Integer getInitAwaitTimeS() {
		return initAwaitTimeS;
	}

	public void setInitAwaitTimeS(Integer initAwaitTimeS) {
		this.initAwaitTimeS = initAwaitTimeS;
	}
}
