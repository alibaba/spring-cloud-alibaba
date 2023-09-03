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

package com.alibaba.cloud.governance.istio;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class TlsContext {

	private static boolean isTls;

	private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private static volatile boolean once = false;

	private TlsContext() {

	}

	public static void setIsTls(boolean isTls) {
		if (once) {
			return;
		}
		try {
			rwLock.writeLock().lock();
			once = true;
			TlsContext.isTls = isTls;
		}
		finally {
			rwLock.writeLock().unlock();
		}
	}

	public static boolean isIsTls() {
		try {
			rwLock.readLock().lock();
			return isTls;
		}
		finally {
			rwLock.readLock().unlock();
		}
	}

	public static boolean isOnce() {
		try {
			rwLock.readLock().lock();
			return once;
		}
		finally {
			rwLock.readLock().unlock();
		}
	}

	public static void close() {
		once = false;
	}

}
