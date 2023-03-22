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

package com.alibaba.cloud.kubernetes.config.util;

import java.util.Objects;

import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;

/**
 * Helper class to get the Spring ApplicationContext and RefreshEvent when refreshing the
 * context.
 *
 * @author Freeman
 */
public final class RefreshContext {
	private final ApplicationContext applicationContext;
	private final RefreshEvent refreshEvent;

	public RefreshContext(ApplicationContext applicationContext,
			RefreshEvent refreshEvent) {
		this.applicationContext = applicationContext;
		this.refreshEvent = refreshEvent;
	}

	public ApplicationContext applicationContext() {
		return applicationContext;
	}

	public RefreshEvent refreshEvent() {
		return refreshEvent;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		RefreshContext that = (RefreshContext) obj;
		return Objects.equals(this.applicationContext, that.applicationContext)
				&& Objects.equals(this.refreshEvent, that.refreshEvent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(applicationContext, refreshEvent);
	}

	@Override
	public String toString() {
		return "RefreshContext[" + "applicationContext=" + applicationContext + ", "
				+ "refreshEvent=" + refreshEvent + ']';
	}

	private static final ThreadLocal<RefreshContext> holder = new ThreadLocal<>();

	public static void set(RefreshContext refreshContext) {
		holder.set(refreshContext);
	}

	public static RefreshContext get() {
		return holder.get();
	}

	public static void remove() {
		holder.remove();
	}
}
