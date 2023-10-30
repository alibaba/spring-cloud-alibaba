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

package com.alibaba.cloud.consumer.resttemplate.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author yuluo-yx
 * @author <a href="1481556636@qq.com"></a>
 */

public final class ConsumerResTemplateUtil {

	public static String getResult(InputStream inputStream) {

		InputStreamReader isr;
		isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader bf = new BufferedReader(isr);
		StringBuilder results = new StringBuilder();
		String newLine;
		while (true) {
			try {
				newLine = bf.readLine();
				if (newLine == null) {
					break;
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			results.append(newLine).append("\n");
		}

		return results.toString();
	}

}
