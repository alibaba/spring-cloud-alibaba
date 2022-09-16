/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.testsupport;

/**
 *
 * @author freeman
 * @since 2021.0.1.0
 */
public class Tester {

	/**
	 * Test function.
	 * @param function function name
	 * @param func func
	 */
	public static void testFunction(String function, Func func) {
		try {
			System.out.println(
					"============================================================================");
			System.out.println("Testing '" + function + "' ......");
			System.out.println(
					"============================================================================");

			func.justDo();

			System.out.println(
					"============================================================================");
			System.out.println("Function '" + function + "' OK !");
			System.out.println(
					"============================================================================\n");
		}
		catch (Throwable e) {
			System.err.println(
					"============================================================================");
			System.err.println("Function '" + function + "' err !");
			System.err.println(
					"============================================================================\n");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Do it, don't care exception.
	 * @param func func
	 */
	public static void justDo(Func func) {
		try {
			func.justDo();
		}
		catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
