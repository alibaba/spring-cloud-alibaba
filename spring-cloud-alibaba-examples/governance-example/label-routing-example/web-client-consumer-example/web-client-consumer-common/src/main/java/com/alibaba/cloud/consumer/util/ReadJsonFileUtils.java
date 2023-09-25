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

package com.alibaba.cloud.consumer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public final class ReadJsonFileUtils {

	private ReadJsonFileUtils() {
	}

	public static String convertFile2String(String filePath) {

		File file = new File(filePath);
		StringBuilder content = new StringBuilder();

		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				content.append(scanner.nextLine()).append("\n");
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found: " + filePath);
			e.printStackTrace();
		}

		return content.toString();
	}

}
