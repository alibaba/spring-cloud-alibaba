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

package com.alibaba.cloud.commons.governance.tls;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerTlsModeHolder {

	private static final Logger log = LoggerFactory.getLogger(ServerTlsModeHolder.class);

	private static volatile Boolean isTls;

	private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private ServerTlsModeHolder() {

	}

	public static void init(boolean initValue) {
		try {
			rwLock.writeLock().lock();
			if (isTls == null) {
				isTls = initValue;
			}
		}
		finally {
			rwLock.writeLock().unlock();
		}
	}

	public static void setTlsMode(boolean isTls) {
		try {
			rwLock.writeLock().lock();
			ServerTlsModeHolder.isTls = isTls;
		}
		finally {
			rwLock.writeLock().unlock();
		}
	}

	public static Boolean getTlsMode() {
		try {
			rwLock.readLock().lock();
			return isTls;
		}
		finally {
			rwLock.readLock().unlock();
		}
	}

	// Verify whether the tls mode is initialized and also updated.
	public static boolean canModeUpdate(boolean isTls) {
		try {
			rwLock.readLock().lock();
			return ServerTlsModeHolder.isTls != null
					&& !Objects.equals(ServerTlsModeHolder.isTls, isTls);
		}
		finally {
			rwLock.readLock().unlock();
		}
	}

}
