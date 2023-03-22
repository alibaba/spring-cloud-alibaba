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

package com.alibaba.cloud.kubernetes.config.processor;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * {@link FileProcessor} use to generate {@link PropertySource} from file content.
 *
 * @author Freeman
 */
public interface FileProcessor {

	/**
	 * Whether the fileName is supported by the processor.
	 *
	 * @param fileName file name
	 * @return true if hit
	 */
	boolean hit(String fileName);

	/**
	 * Generate property source from file content.
	 *
	 * @param name property source name
	 * @param content file content
	 * @return property source
	 */
	EnumerablePropertySource<?> generate(String name, String content);
}
