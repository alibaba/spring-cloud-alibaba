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

package com.alibaba.cloud.mtls;

import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.governance.istio.sds.CertPair;
import com.alibaba.cloud.governance.istio.util.CertificateUtil;
import com.alibaba.cloud.mtls.server.webflux.MtlsWebfluxServerTest;

public class MockCertManager extends AbstractCertManager {

	public MockCertManager(XdsConfigProperties xdsConfigProperties) {
		super(xdsConfigProperties);
	}

	public MockCertManager() {
		super(null);
	}

	@Override
	public synchronized CertPair getCertPair() {
		Certificate[] certificate = CertificateUtil.loadCertificateFromPath(
				MtlsWebfluxServerTest.class.getResource("/cert/mtls.crt").getPath());
		PrivateKey privateKey = CertificateUtil.loadPrivateKeyFromPath(
				MtlsWebfluxServerTest.class.getResource("/cert/mtls.key").getPath());
		CertPair p = new CertPair();
		p.setCertificateChain(certificate);
		p.setPrivateKey(privateKey);
		p.setExpireTime(Long.MAX_VALUE);
		return p;
	}

}
