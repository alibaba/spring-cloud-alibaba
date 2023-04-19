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

package com.alibaba.cloud.governance.istio.util;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CertificateUtil {

	private static final Logger log = LoggerFactory.getLogger(CertificateUtil.class);

	private static final String ALG_RSA = "RSA";

	private static final String PEM_CERTIFICATE_START = "-----BEGIN CERTIFICATE-----";

	private static final String PEM_CERTIFICATE_END = "-----END CERTIFICATE-----";

	private static final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----";

	private static final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----";

	private CertificateUtil() {

	}

	public static Certificate loadCertificate(String certificatePem) {
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X509");
			certificatePem = certificatePem.replaceAll(PEM_CERTIFICATE_START, "");
			certificatePem = certificatePem.replaceAll(PEM_CERTIFICATE_END, "");
			certificatePem = certificatePem.replaceAll("\\s*", "");
			return certificateFactory.generateCertificate(
					new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));
		}
		catch (Exception e) {
			log.error("Load certificate failed from pem string", e);
		}
		return null;
	}

}
