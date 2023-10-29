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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCertManager implements CertUpdater, CertPairProvider {

	protected List<CertUpdateCallback> callbacks = new ArrayList<>();

	protected XdsConfigProperties xdsConfigProperties;

	protected static final Logger log = LoggerFactory
			.getLogger(AbstractCertManager.class);

	protected static final String CSR_REQUEST_BEGIN = "-----BEGIN CERTIFICATE REQUEST-----";

	protected static final String CSR_REQUEST_END = "-----END CERTIFICATE REQUEST-----";

	protected CertPair certPair = new CertPair();

	protected final ScheduledExecutorService schedule;

	public AbstractCertManager(XdsConfigProperties xdsConfigProperties) {
		this.xdsConfigProperties = xdsConfigProperties;
		schedule = Executors.newScheduledThreadPool(1);
		schedule.scheduleAtFixedRate(() -> {
			try {
				getCertPair();
			}
			catch (Exception e) {
				log.error("Generate Cert from Istio failed.", e);
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	@Override
	public synchronized CertPair getCertPair() {
		if (System.currentTimeMillis() < certPair.getExpireTime()) {
			return certPair;
		}
		CertPair p = doGetCertPair();
		certPair = p;
		if (p != null && p.getExpireTime() != 0) {
			for (CertUpdateCallback callback : callbacks) {
				callback.onUpdateCert(p);
			}
		}
		return certPair;
	}

	protected synchronized CertPair doGetCertPair() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerCallback(CertUpdateCallback certUpdateCallback) {
		callbacks.add(certUpdateCallback);
	}

	@PreDestroy
	public void close() {
		schedule.shutdown();
	}

}
