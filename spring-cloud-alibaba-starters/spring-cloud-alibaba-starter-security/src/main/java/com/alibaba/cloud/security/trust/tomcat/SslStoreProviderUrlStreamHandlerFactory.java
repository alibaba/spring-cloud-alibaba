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

package com.alibaba.cloud.security.trust.tomcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.KeyStore;

import org.springframework.boot.web.server.SslStoreProvider;


public class SslStoreProviderUrlStreamHandlerFactory implements URLStreamHandlerFactory {

	private static final String PROTOCOL = "springbootssl";

	private static final String KEY_STORE_PATH = "keyStore";

	static final String KEY_STORE_URL = PROTOCOL + ":" + KEY_STORE_PATH;

	private static final String TRUST_STORE_PATH = "trustStore";

	static final String TRUST_STORE_URL = PROTOCOL + ":" + TRUST_STORE_PATH;

	// Must be a static variable, or we can not reload sslStoreProvider when we restart
	// the spring context.
	private static SslStoreProvider sslStoreProvider;

	SslStoreProviderUrlStreamHandlerFactory(SslStoreProvider sslStoreProvider) {
		SslStoreProviderUrlStreamHandlerFactory.sslStoreProvider = sslStoreProvider;
	}

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (PROTOCOL.equals(protocol)) {
			return new URLStreamHandler() {

				@Override
				protected URLConnection openConnection(URL url) throws IOException {
					try {
						if (KEY_STORE_PATH.equals(url.getPath())) {
							return new KeyStoreUrlConnection(url,
									SslStoreProviderUrlStreamHandlerFactory.sslStoreProvider
											.getKeyStore());
						}
						if (TRUST_STORE_PATH.equals(url.getPath())) {
							return new KeyStoreUrlConnection(url,
									SslStoreProviderUrlStreamHandlerFactory.sslStoreProvider
											.getTrustStore());
						}
					}
					catch (Exception ex) {
						throw new IOException(ex);
					}
					throw new IOException("Invalid path: " + url.getPath());
				}
			};
		}
		return null;
	}

	private static final class KeyStoreUrlConnection extends URLConnection {

		private final KeyStore keyStore;

		private KeyStoreUrlConnection(URL url, KeyStore keyStore) {
			super(url);
			this.keyStore = keyStore;
		}

		@Override
		public void connect() throws IOException {

		}

		@Override
		public InputStream getInputStream() throws IOException {

			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				this.keyStore.store(stream, new char[0]);
				return new ByteArrayInputStream(stream.toByteArray());
			}
			catch (Exception ex) {
				throw new IOException(ex);
			}
		}

	}

}
