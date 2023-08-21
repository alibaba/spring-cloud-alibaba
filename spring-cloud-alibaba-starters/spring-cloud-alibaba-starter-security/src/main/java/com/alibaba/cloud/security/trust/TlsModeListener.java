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

package com.alibaba.cloud.security.trust;

import java.io.Closeable;
import java.io.IOException;

import com.alibaba.csp.sentinel.datasource.xds.XdsDataSource;
import com.alibaba.csp.sentinel.trust.TrustManager;
import com.alibaba.csp.sentinel.trust.tls.TlsMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;

public class TlsModeListener {
	private Logger log = LoggerFactory.getLogger(TlsModeListener.class);


	private ConfigurableApplicationContext context;
	private SpringApplication application;
	private String[] args;

	private TlsMode tlsMode = null;

	private XdsDataSource xdsDataSource;

	public TlsModeListener(XdsDataSource xdsDataSource) {
		context = ApplicationPreparedEventListener.getContext();
		application = ApplicationPreparedEventListener.getApplication();
		args = ApplicationPreparedEventListener.getArgs();
		tlsMode = TrustManager.getInstance().getTlsMode();
		this.xdsDataSource = xdsDataSource;

	}


	public void onUpdate() {
		synchronized (this) {
			if (tlsMode.equals(TrustManager.getInstance().getTlsMode())) {
				return;
			}
			Thread thread = new Thread(this::restart);
			thread.setDaemon(false);
			thread.start();
		}
	}


	public synchronized void restart() {
		try {
			TrustManager.getInstance().removeAllTlsModeCallback();
			TrustManager.getInstance().removeAllCertCallback();
			TrustManager.getInstance().removeAllRulesCallback();
			xdsDataSource.close();
			application.setEnvironment(context.getEnvironment());
			ApplicationContext applicationContex = context;

			while (applicationContex instanceof Closeable) {
				try {
					((Closeable) context).close();
				}
				catch (IOException e) {
					log.error("Cannot close context: " + context.getId(), e);
				}
				applicationContex = applicationContex.getParent();
			}

			ClassUtils.overrideThreadContextClassLoader(application.getClass().getClassLoader());
			context = application.run(this.args);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


	public ConfigurableApplicationContext getContext() {
		return context;
	}

	public SpringApplication getApplication() {
		return application;
	}

	public String[] getArgs() {
		return args;
	}
}
