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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public final class NoVerifyHttpClientFactory {

	private NoVerifyHttpClientFactory() {

	}

	private static final CloseableHttpClient CLIENT;

	static {
		try {
			CLIENT = HttpClients.custom()
					.setSSLSocketFactory(new SSLConnectionSocketFactory(
							createIgnoreVerifySSL(), null, null, new HostnameVerifier() {
								@Override
								public boolean verify(String hostname,
										SSLSession session) {
									return true;
								}
							}))
					.build();
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static CloseableHttpClient getClient() {
		return CLIENT;
	}

	public static CloseableHttpClient getClient(SSLContext sslContext) {
		return HttpClients.custom()
				.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, null,
						null, new HostnameVerifier() {
							@Override
							public boolean verify(String hostname, SSLSession session) {
								return true;
							}
						}))
				.build();
	}

	private static SSLContext createIgnoreVerifySSL() throws Exception {
		SSLContext sc = SSLContext.getInstance("TLS");
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) {
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

}
