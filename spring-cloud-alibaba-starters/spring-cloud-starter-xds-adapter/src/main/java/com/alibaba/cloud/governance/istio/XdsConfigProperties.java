/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.istio;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
@ConfigurationProperties(XdsConfigProperties.PREFIX)
public class XdsConfigProperties {

	private static final Logger log = LoggerFactory.getLogger(XdsConfigProperties.class);

	/**
	 * Prefix in yaml.
	 */
	public static final String PREFIX = "spring.cloud.istio.config";

	private String host;

	private int port;

	/**
	 * jwt token for istiod 15012 port.
	 */
	private String istiodToken;

	private Boolean logXds;

	private Boolean useAgent;

	private String podName;

	private String caAddr;

	private String jwtPolicy;

	private String trustDomain;

	private String namespaceName;

	private String eccSigAlg;

	private int secretTTL;

	private int rsaKeySize;

	private float secretGracePeriodRatio;

	private String istioMetaClusterId;

	private String serviceAccountName;

	private boolean skipXdsRequest = false;

	@Value("${spring.application.name:}")
	private String applicationName;

	@PostConstruct
	public void init() {
		if (this.port <= 0 || this.port > 65535) {
			this.port = IstioConstants.ISTIOD_SECURE_PORT;
		}
		if (StringUtils.isEmpty(host)) {
			this.host = IstioConstants.DEFAULT_ISTIOD_ADDR;
		}
		if (logXds == null) {
			logXds = true;
		}
		if (useAgent == null) {
			useAgent = false;
		}
		if (podName == null) {
			podName = Optional.ofNullable(System.getenv(IstioConstants.POD_NAME))
					.orElse(IstioConstants.DEFAULT_POD_NAME);
		}
		if (caAddr == null) {
			caAddr = Optional.ofNullable(System.getenv(IstioConstants.CA_ADDR_KEY))
					.orElse(IstioConstants.DEFAULT_CA_ADDR);
		}
		if (jwtPolicy == null) {
			jwtPolicy = IstioConstants.FIRST_PARTY_JWT;
		}
		if (trustDomain == null) {
			trustDomain = IstioConstants.DEFAULT_TRUST_DOMAIN;
		}
		if (namespaceName == null) {
			namespaceName = Optional
					.ofNullable(Optional
							.ofNullable(System.getenv(IstioConstants.POD_NAMESPACE))
							.orElse(System.getenv(IstioConstants.WORKLOAD_NAMESPACE)))
					.orElseGet(() -> {
						File namespaceFile = new File(
								IstioConstants.KUBERNETES_NAMESPACE_PATH);
						if (namespaceFile.canRead()) {
							try {
								return FileUtils.readFileToString(namespaceFile,
										StandardCharsets.UTF_8);
							}
							catch (IOException e) {
								log.error("Read k8s namespace file error", e);
							}
						}
						return IstioConstants.DEFAULT_NAMESPACE;
					});
		}
		if (eccSigAlg == null) {
			eccSigAlg = Optional.ofNullable(System.getenv(IstioConstants.ECC_SIG_ALG_KEY))
					.orElse(IstioConstants.DEFAULT_ECC_SIG_ALG);
		}
		if (secretGracePeriodRatio <= 0) {
			secretGracePeriodRatio = Float.parseFloat(Optional
					.ofNullable(
							System.getenv(IstioConstants.SECRET_GRACE_PERIOD_RATIO_KEY))
					.orElse(IstioConstants.DEFAULT_SECRET_GRACE_PERIOD_RATIO));
		}
		if (secretTTL <= 0) {
			secretTTL = Integer.parseInt(
					Optional.ofNullable(System.getenv(IstioConstants.SECRET_TTL_KEY))
							.orElse(IstioConstants.DEFAULT_SECRET_TTL));
		}
		if (istioMetaClusterId == null) {
			istioMetaClusterId = IstioConstants.DEFAULT_ISTIO_META_CLUSTER_ID;
		}
		if (rsaKeySize <= 0) {
			rsaKeySize = Integer.parseInt(
					Optional.ofNullable(System.getenv(IstioConstants.RSA_KEY_SIZE_KEY))
							.orElse(IstioConstants.DEFAULT_RSA_KEY_SIZE));
		}
		if (serviceAccountName == null) {
			serviceAccountName = Optional
					.ofNullable(System.getenv(IstioConstants.SERVICE_ACCOUNT_KEY))
					.orElse(applicationName);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIstiodToken() {
		if (istiodToken != null) {
			return istiodToken;
		}
		File saFile;
		switch (jwtPolicy) {
		case IstioConstants.FIRST_PARTY_JWT:
			saFile = new File(IstioConstants.FIRST_PART_JWT_PATH);
			break;
		case IstioConstants.THIRD_PARTY_JWT:
		default:
			saFile = new File(IstioConstants.THIRD_PART_JWT_PATH);
		}
		if (saFile.canRead()) {
			try {
				return FileUtils.readFileToString(saFile, StandardCharsets.UTF_8);
			}
			catch (IOException e) {
				log.error("Unable to read token file.", e);
			}
		}
		return "";
	}

	public void setIstiodToken(String istiodToken) {
		this.istiodToken = istiodToken;
	}

	public boolean isLogXds() {
		return Boolean.TRUE.equals(logXds);
	}

	public void setLogXds(boolean logXds) {
		this.logXds = logXds;
	}

	public Boolean getUseAgent() {
		return useAgent;
	}

	public void setUseAgent(Boolean useAgent) {
		this.useAgent = useAgent;
	}

	public String getPodName() {
		return podName;
	}

	public void setPodName(String podName) {
		this.podName = podName;
	}

	public String getCaAddr() {
		return caAddr;
	}

	public void setCaAddr(String caAddr) {
		this.caAddr = caAddr;
	}

	public String getJwtPolicy() {
		return jwtPolicy;
	}

	public void setJwtPolicy(String jwtPolicy) {
		this.jwtPolicy = jwtPolicy;
	}

	public String getTrustDomain() {
		return trustDomain;
	}

	public void setTrustDomain(String trustDomain) {
		this.trustDomain = trustDomain;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public float getSecretGracePeriodRatio() {
		return secretGracePeriodRatio;
	}

	public int getSecretTTL() {
		return secretTTL;
	}

	public String getEccSigAlg() {
		return eccSigAlg;
	}

	public void setEccSigAlg(String eccSigAlg) {
		this.eccSigAlg = eccSigAlg;
	}

	public void setSecretTTL(int secretTTL) {
		this.secretTTL = secretTTL;
	}

	public void setLogXds(Boolean logXds) {
		this.logXds = logXds;
	}

	public void setSecretGracePeriodRatio(float secretGracePeriodRatio) {
		this.secretGracePeriodRatio = secretGracePeriodRatio;
	}

	public String getIstioMetaClusterId() {
		return istioMetaClusterId;
	}

	public void setIstioMetaClusterId(String istioMetaClusterId) {
		this.istioMetaClusterId = istioMetaClusterId;
	}

	public int getRsaKeySize() {
		return rsaKeySize;
	}

	public void setRsaKeySize(int rsaKeySize) {
		this.rsaKeySize = rsaKeySize;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getServiceAccountName() {
		return serviceAccountName;
	}

	public void setServiceAccountName(String serviceAccountName) {
		this.serviceAccountName = serviceAccountName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public boolean isSkipXdsRequest() {
		return skipXdsRequest;
	}

	public void setSkipXdsRequest(boolean skipXdsRequest) {
		this.skipXdsRequest = skipXdsRequest;
	}

}
