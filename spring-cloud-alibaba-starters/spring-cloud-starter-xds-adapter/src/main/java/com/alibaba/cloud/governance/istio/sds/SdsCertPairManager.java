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

package com.alibaba.cloud.governance.istio.sds;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.ParseException;
import java.util.Map;

import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.bootstrap.Bootstrapper;
import com.alibaba.cloud.governance.istio.util.CertificateUtil;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;

public class SdsCertPairManager extends AbstractCertManager {

	private final Bootstrapper.BootstrapInfo bootstrapInfo;

	private static final String CERTIFICATE_FILE_KEY = "certificate_file";

	private static final String PRIVATE_KEY_FILE_KEY = "private_key_file";

	private static final String CA_CERTIFICATE_FILE_KEY = "ca_certificate_file";

	private static final String REFRESH_INTERVAL_KEY = "refresh_interval";

	public SdsCertPairManager(Bootstrapper.BootstrapInfo bootstrapInfo,
			XdsConfigProperties xdsConfigProperties) {
		super(xdsConfigProperties);
		this.bootstrapInfo = bootstrapInfo;
	}

	public synchronized CertPair doGetCertPair() {
		CertPair certPair = new CertPair();
		try {
			Bootstrapper.CertificateProviderInfo certificateProviderInfo = bootstrapInfo
					.certProviders().get("default");
			if (certificateProviderInfo == null) {
				return new CertPair();
			}
			Map<String, ?> config = certificateProviderInfo.config();
			if (config == null) {
				return new CertPair();
			}
			certPair = getCertPairFromConfig(config);
			return certPair;
		}
		catch (Exception e) {
			log.error("Failed to request cert pair", e);
		}
		return certPair;
	}

	private CertPair getCertPairFromConfig(Map<String, ?> config) throws ParseException {
		CertPair certPair = new CertPair();
		String certificateFilePath = (String) config.get(CERTIFICATE_FILE_KEY);
		Certificate[] certificates = CertificateUtil
				.loadCertificateFromPath(certificateFilePath);
		String privateKeyPath = (String) config.get(PRIVATE_KEY_FILE_KEY);
		PrivateKey privateKey = CertificateUtil.loadPrivateKeyFromPath(privateKeyPath);
		String caCertFilePath = (String) config.get(CA_CERTIFICATE_FILE_KEY);
		Certificate caCertificate = CertificateUtil
				.loadCertificateFromPath(caCertFilePath)[0];
		String refreshInterval = (String) config.get(REFRESH_INTERVAL_KEY);
		Duration duration = Durations.parse(refreshInterval);
		certPair.setCertificateChain(certificates);
		certPair.setPrivateKey(privateKey);
		certPair.setRootCA(caCertificate);
		certPair.setExpireTime(
				duration.getSeconds() * 1000L + System.currentTimeMillis());
		log.info("Received {} certificates, 1 private key", certificates.length);
		return certPair;
	}

}
