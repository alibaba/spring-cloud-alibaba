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

package com.alibaba.cloud.examples.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Change the request path containing echo.
 *
 * @author fangjian0423, MieAh
 */
public class UrlCleaner {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlCleaner.class);

	private static final String URL_CLEAN_ECHO = ".*/echo/.*";

	public static String clean(String url) {
		LOGGER.info("enter urlCleaner");
		if (url.matches(URL_CLEAN_ECHO)) {
			LOGGER.info("change url");
			url = url.replaceAll("/echo/.*", "/echo/{str}");
		}
		return url;
	}

}
