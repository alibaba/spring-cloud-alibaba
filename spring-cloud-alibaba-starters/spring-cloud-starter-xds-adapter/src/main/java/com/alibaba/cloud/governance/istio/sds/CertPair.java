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

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

import com.alibaba.cloud.governance.istio.exception.CertificateException;
import com.alibaba.cloud.governance.istio.util.CertificateUtil;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;

public class CertPair {

	private Certificate[] certificateChain;

	private PrivateKey privateKey;

	private byte[] rawCertificateChain;

	private byte[] rawPrivateKey;

	private Certificate rootCA;

	private long expireTime;

	public CertPair() {

	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public Certificate[] getCertificateChain() {
		return certificateChain;
	}

	public void setCertificateChain(Certificate[] certificateChain) {
		this.certificateChain = certificateChain;
	}

	public void setCertificateChain(List<String> certificateChain) {
		final int n = certificateChain.size();
		Certificate[] certificates = new Certificate[n];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; ++i) {
			sb.append(certificateChain.get(i));
			certificates[i] = CertificateUtil.loadCertificate(certificateChain.get(i));
			if (certificates[i] == null) {
				throw new RuntimeException(
						"Failed to load certificate, pem is " + certificateChain.get(i));
			}
		}
		this.rawCertificateChain = sb.toString().getBytes(StandardCharsets.UTF_8);
		this.certificateChain = certificates;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
		try {
			PemObject pemObject = new PemObject("RSA PRIVATE KEY",
					privateKey.getEncoded());
			StringWriter str = new StringWriter();
			JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(str);
			jcaPEMWriter.writeObject(pemObject);
			jcaPEMWriter.close();
			str.close();
			rawPrivateKey = str.toString().getBytes(StandardCharsets.UTF_8);
		}
		catch (Exception e) {
			throw new CertificateException("Unable to parse raw private key");
		}
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public byte[] getRawCertificateChain() {
		return rawCertificateChain;
	}

	public byte[] getRawPrivateKey() {
		return rawPrivateKey;
	}

	public Certificate getRootCA() {
		if (rootCA == null) {
			return certificateChain[certificateChain.length - 1];
		}
		return rootCA;
	}

	public void setRootCA(Certificate rootCA) {
		this.rootCA = rootCA;
	}

}
