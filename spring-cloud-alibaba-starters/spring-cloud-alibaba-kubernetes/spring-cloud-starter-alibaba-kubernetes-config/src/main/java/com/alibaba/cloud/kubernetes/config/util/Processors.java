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

import java.util.Arrays;
import java.util.List;

import com.alibaba.cloud.kubernetes.config.processor.FileProcessor;
import com.alibaba.cloud.kubernetes.config.processor.JsonFileProcessor;
import com.alibaba.cloud.kubernetes.config.processor.PropertiesFileProcessor;
import com.alibaba.cloud.kubernetes.config.processor.YamlFileProcessor;

/**
 * @author Freeman
 */
public final class Processors {

	private Processors() {
		throw new UnsupportedOperationException("No Processors instances for you!");
	}

	private static final List<FileProcessor> processors = Arrays.asList(
			new YamlFileProcessor(), new PropertiesFileProcessor(),
			new JsonFileProcessor());

	public static List<FileProcessor> fileProcessors() {
		return processors;
	}
}
