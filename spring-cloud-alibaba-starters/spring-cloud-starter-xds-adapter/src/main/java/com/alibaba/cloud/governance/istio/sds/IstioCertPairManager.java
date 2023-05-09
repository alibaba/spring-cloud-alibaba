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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.exception.CertificateException;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

public class IstioCertPairManager extends AbstractCertManager {

	private final ScheduledExecutorService schedule;

	public IstioCertPairManager(XdsConfigProperties xdsConfigProperties) {
		super(xdsConfigProperties);
		schedule = Executors.newScheduledThreadPool(1);
		schedule.scheduleAtFixedRate(() -> {
			try {
				getCertPair();
			}
			catch (Exception e) {
				log.error("Generate Cert from Istio failed.", e);
			}
		}, 0, 30, TimeUnit.SECONDS);
	}

	@PreDestroy
	public void close() {
		schedule.shutdown();
	}

	protected synchronized CertPair doGetCertPair() {
		CertPair newCertPair = new CertPair();
		try {
			KeyPairGenerator localKeyPairGenerator;
			if (IstioConstants.DEFAULT_ECC_SIG_ALG
					.equals(xdsConfigProperties.getEccSigAlg())) {
				ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
				localKeyPairGenerator = KeyPairGenerator.getInstance("EC");
				localKeyPairGenerator.initialize(ecSpec, new SecureRandom());

			}
			else {
				localKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
				localKeyPairGenerator.initialize(xdsConfigProperties.getRsaKeySize());
			}
			KeyPair localKeyPair = localKeyPairGenerator.genKeyPair();
			newCertPair.setPrivateKey(localKeyPair.getPrivate());
			String csr = getCSR(localKeyPair);
			String caCert = getCaCert();
			ManagedChannel channel;
			if (StringUtils.isNotEmpty(caCert)) {
				channel = NettyChannelBuilder.forTarget(xdsConfigProperties.getCaAddr())
						.sslContext(GrpcSslContexts.forClient()
								.trustManager(new ByteArrayInputStream(
										caCert.getBytes(StandardCharsets.UTF_8)))
								.build())
						.build();
			}
			else {
				channel = NettyChannelBuilder.forTarget(xdsConfigProperties.getCaAddr())
						.sslContext(GrpcSslContexts.forClient()
								.trustManager(InsecureTrustManagerFactory.INSTANCE)
								.build())
						.build();
			}
			Metadata header = new Metadata();
			Metadata.Key<String> key = Metadata.Key.of("authorization",
					Metadata.ASCII_STRING_MARSHALLER);
			header.put(key, "Bearer " + xdsConfigProperties.getIstiodToken());

			key = Metadata.Key.of("ClusterID", Metadata.ASCII_STRING_MARSHALLER);
			header.put(key, xdsConfigProperties.getIstioMetaClusterId());

			istio.v1.auth.IstioCertificateServiceGrpc.IstioCertificateServiceStub stub = istio.v1.auth.IstioCertificateServiceGrpc
					.newStub(channel);
			stub = MetadataUtils.attachHeaders(stub, header);
			final CountDownLatch countDownLatch = new CountDownLatch(1);
			stub.createCertificate(
					istio.v1.auth.IstioCertificateRequest.newBuilder().setCsr(csr)
							.setValidityDuration(xdsConfigProperties.getSecretTTL())
							.build(),
					new StreamObserver<istio.v1.auth.IstioCertificateResponse>() {
						@Override
						public void onNext(
								istio.v1.auth.IstioCertificateResponse istioCertificateResponse) {
							final int n = istioCertificateResponse.getCertChainCount();
							List<String> certificates = new ArrayList<>();
							for (int i = 0; i < n; ++i) {
								certificates
										.add(istioCertificateResponse.getCertChain(i));
							}
							newCertPair.setExpireTime(System.currentTimeMillis()
									+ (long) (xdsConfigProperties.getSecretTTL()
											* xdsConfigProperties
													.getSecretGracePeriodRatio())
											* 1000);
							newCertPair.setCertificateChain(certificates);
							log.info(
									"Send CSR to CA successfully, {} certificates received",
									n);
							countDownLatch.countDown();
						}

						@Override
						public void onError(Throwable throwable) {
							log.error("Unable to send csr to ca server", throwable);
							countDownLatch.countDown();
						}

						@Override
						public void onCompleted() {
							if (countDownLatch.getCount() > 0) {
								countDownLatch.countDown();
							}
							if (log.isDebugEnabled()) {
								log.debug("Send csr to istiod completed");
							}
						}
					});
			try {
				countDownLatch.await();
			}
			catch (InterruptedException e) {
				throw new CertificateException(
						"Generate Cert Failed. Wait for cert failed.", e);
			}
			finally {
				channel.shutdown();
			}
			return newCertPair;
		}
		catch (Exception e) {
			log.error("Unable to get cert pair from istio", e);
		}
		return newCertPair;
	}

	private String getCaCert() {
		File caFile = new File(IstioConstants.ISTIO_CA_PATH);
		if (caFile.canRead()) {
			try {
				return FileUtils.readFileToString(caFile, StandardCharsets.UTF_8);
			}
			catch (IOException e) {
				log.error("read ca file error", e);
			}
		}
		return null;
	}

	private String getCsrHost() {
		return IstioConstants.SPIFFE + xdsConfigProperties.getTrustDomain()
				+ IstioConstants.NS + xdsConfigProperties.getNamespaceName()
				+ IstioConstants.SA + xdsConfigProperties.getServiceAccountName();
	}

	private String getCSR(KeyPair localKeyPair) {
		try {
			X500NameBuilder localX500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
			localX500NameBuilder.addRDN(BCStyle.CN, getCsrHost());
			localX500NameBuilder.addRDN(BCStyle.O, xdsConfigProperties.getTrustDomain());
			X500Name localX500Name = localX500NameBuilder.build();
			JcaPKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
					localX500Name, localKeyPair.getPublic());
			JcaContentSignerBuilder csBuilder;
			if (IstioConstants.DEFAULT_ECC_SIG_ALG
					.equals(xdsConfigProperties.getEccSigAlg())) {
				csBuilder = new JcaContentSignerBuilder("SHA256withECDSA");
			}
			else {
				csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
			}
			ContentSigner signer = csBuilder.build(localKeyPair.getPrivate());
			PKCS10CertificationRequest csr = p10Builder.build(signer);
			return CSR_REQUEST_BEGIN + "\n"
					+ new String(Base64.getEncoder().encode(csr.getEncoded())) + "\n"
					+ CSR_REQUEST_END + "\n";
		}
		catch (Exception e) {
			throw new CertificateException("Unable to generate CSR", e);
		}
	}

}
