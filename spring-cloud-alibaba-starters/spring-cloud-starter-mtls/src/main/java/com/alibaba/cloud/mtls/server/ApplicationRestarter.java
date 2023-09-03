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

package com.alibaba.cloud.mtls.server;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ClassUtils;

public class ApplicationRestarter {

	private ConfigurableApplicationContext context;

	private SpringApplication application;

	private String[] args;

	private static final Log log = LogFactory.getLog(PreparedEventRecorder.class);

	public void restart() {
		Thread thread = new Thread(this::safeRestart);
		thread.setDaemon(false);
		thread.start();
	}

	public ApplicationRestarter() {
		this.application = PreparedEventRecorder.getApplication();
		this.context = PreparedEventRecorder.getContext();
		this.args = PreparedEventRecorder.getArgs();
	}

	private Boolean safeRestart() {
		try {
			doRestart();
			return true;
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.info("Could not doRestart", e);
			}
			else {
				log.info("Could not doRestart: " + e.getMessage());
			}
			return false;
		}
	}

	// @ManagedOperation
	private synchronized void doRestart() {
		if (this.context != null) {
			this.application.setEnvironment(this.context.getEnvironment());
			close();
			// If running in a webapp then the context classloader is probably going to
			// die so we need to revert to a safe place before starting again
			overrideClassLoaderForRestart();
			this.context = this.application.run(this.args);
		}
	}

	private void close() {
		ApplicationContext context = this.context;
		while (context instanceof Closeable) {
			try {
				((Closeable) context).close();
			}
			catch (IOException e) {
				log.error("Cannot close context: " + context.getId(), e);
			}
			context = context.getParent();
		}
	}

	// @ManagedAttribute
	public boolean isRunning() {
		if (this.context != null) {
			return this.context.isRunning();
		}
		return false;
	}

	private void overrideClassLoaderForRestart() {
		ClassUtils.overrideThreadContextClassLoader(
				this.application.getClass().getClassLoader());
	}

}
