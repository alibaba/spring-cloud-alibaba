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

package com.alibaba.cloud.stream.binder.rocketmq.metrics;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.Lifecycle;

/**
 * @author Timur Valiev
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class Instrumentation {

	private final String name;

	private Lifecycle actuator;

	protected final AtomicBoolean started = new AtomicBoolean(false);

	protected Exception startException = null;

	public Instrumentation(String name) {
		this.name = name;
	}

	public Instrumentation(String name, Lifecycle actuator) {
		this.name = name;
		this.actuator = actuator;
	}

	public Lifecycle getActuator() {
		return actuator;
	}

	public void setActuator(Lifecycle actuator) {
		this.actuator = actuator;
	}

	public boolean isDown() {
		return startException != null;
	}

	public boolean isUp() {
		return started.get();
	}

	public boolean isOutOfService() {
		return !started.get() && startException == null;
	}

	public void markStartedSuccessfully() {
		started.set(true);
	}

	public void markStartFailed(Exception e) {
		started.set(false);
		startException = e;
	}

	public String getName() {
		return name;
	}

	public boolean isStarted() {
		return started.get();
	}

	public Exception getStartException() {
		return startException;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getActuator());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Instrumentation that = (Instrumentation) o;
		return name.equals(that.name) && actuator.equals(that.actuator);
	}

}
