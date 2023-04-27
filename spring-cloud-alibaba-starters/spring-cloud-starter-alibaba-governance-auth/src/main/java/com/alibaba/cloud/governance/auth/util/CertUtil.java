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

package com.alibaba.cloud.governance.auth.util;

import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CertUtil {
	private static final Logger log = LoggerFactory.getLogger(CertUtil.class);
	public static String getCN(X509Certificate x509Certificate) {
		try {
			X500Principal x500Principal = x509Certificate.getSubjectX500Principal();
			String dn = x500Principal.getName();
			X500Principal x500Name = new X500Principal(dn);
			return x500Name.getName("CN");
		}
		catch (Exception e) {
			log.error("Failed to get common name from X509Certificate");
		}
		return "";
	}
}
