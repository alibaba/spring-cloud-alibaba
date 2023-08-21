/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.security.trust.rest;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


import com.alibaba.csp.sentinel.trust.TrustManager;
import com.alibaba.csp.sentinel.trust.tls.TlsMode;
import org.apache.http.conn.ssl.NoopHostnameVerifier;


public class TrustSimpleClientHttpRequestFactory extends org.springframework.http.client.SimpleClientHttpRequestFactory {

	private final SSLContext sslContext;

	public TrustSimpleClientHttpRequestFactory(SSLContext sslContext) {
		this.sslContext = sslContext;
	}


	@Override
	protected HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
		url = tlsBefore(url);
		return super.openConnection(url, proxy);
	}

	private URL tlsBefore(URL url) {
		if (TrustManager.getInstance().getTlsMode().getGlobalTls() != TlsMode.TlsType.STRICT) {
			return url;
		}
		try {
			URI uri = url.toURI();
			if ("http".equals(uri.getScheme())) {
				String uriSt = uri.toString();
				String uriStNew = uriSt.replaceFirst("http", "https");
				URI newUri = new URI(uriStNew);
				return newUri.toURL();
			}
			else {
				return url;
			}
		}
		catch (Exception e) {
			return url;
		}
	}


	@Override
	protected void prepareConnection(HttpURLConnection connection, String httpMethod)
			throws IOException {
		if (TrustManager.getInstance().getTlsMode().getGlobalTls() != TlsMode.TlsType.STRICT) {
			return;
		}
		if (connection instanceof HttpsURLConnection) {
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
			httpsURLConnection.setHostnameVerifier(new NoopHostnameVerifier());
			httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
		}
		super.prepareConnection(connection, httpMethod);
	}

}
