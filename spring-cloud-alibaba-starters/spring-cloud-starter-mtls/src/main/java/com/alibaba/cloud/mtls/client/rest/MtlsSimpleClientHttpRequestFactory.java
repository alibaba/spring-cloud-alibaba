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

package com.alibaba.cloud.mtls.client.rest;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

public class MtlsSimpleClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

	private final SSLContext sslContext;

	public MtlsSimpleClientHttpRequestFactory(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	@Override
	protected void prepareConnection(HttpURLConnection connection, String httpMethod)
			throws IOException {
		if (connection instanceof HttpsURLConnection) {
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
			httpsURLConnection.setHostnameVerifier(new NoopHostnameVerifier());
			httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
		}
		super.prepareConnection(connection, httpMethod);
	}

}
