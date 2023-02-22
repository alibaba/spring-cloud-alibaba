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

package com.alibaba.cloud.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author raozihao
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
@SpringBootApplication
public class RestTemplateApplication {

	public static void main(String[] args) throws Exception {
		try {
			SpringApplication.run(RestTemplateApplication.class, args);
		} catch (SpringApplication.AbandonedRunException e) {
			System.out.println("Abandoned run `process-aot`.");
			throw e;
		} catch (Throwable t) {
			// In the `native-image`, if an exception occurs prematurely during the startup process, the exception log will not be recorded,
			// so here we sleep for 60 seconds to observe the exception information.
			if (System.getProperty("org.graalvm.nativeimage.imagecode") != null) {
				t.printStackTrace();
				Thread.sleep(60000);
			}

			throw t;
		}
	}

}
