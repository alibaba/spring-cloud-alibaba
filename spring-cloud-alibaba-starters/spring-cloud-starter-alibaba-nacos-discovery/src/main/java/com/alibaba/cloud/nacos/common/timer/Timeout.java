/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.nacos.common.timer;

/**
 * A handle associated with a {@link TimerTask} that is returned by a {@link Timer}.
 */
public interface Timeout {

	/**
	 * Returns the {@link Timer} that created this handle.
	 * @return Timer
	 */
	Timer timer();

	/**
	 * Returns the {@link TimerTask} which is associated with this handle.
	 * @return TimerTask
	 */
	TimerTask task();

	/**
	 * Returns {@code true} if and only if the {@link TimerTask} associated with this
	 * handle has been expired.
	 * @return the flag if is expired
	 */
	boolean isExpired();

	/**
	 * Returns {@code true} if and only if the {@link TimerTask} associated with this
	 * handle has been cancelled.
	 * @return the flag if is cancelled
	 */
	boolean isCancelled();

	/**
	 * Attempts to cancel the {@link TimerTask} associated with this handle. If the task
	 * has been executed or cancelled already, it will return with no side effect.
	 * @return True if the cancellation completed successfully, otherwise false
	 */
	boolean cancel();

}
