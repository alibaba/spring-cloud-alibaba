/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.examples;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.spring.util.parse.DefaultPropertiesConfigParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
@Service
public class PrintLogger {

	private static Logger logger = LoggerFactory.getLogger(PrintLogger.class);

	private static final String LOGGER_TAG = "logging.level.";

	@Autowired
	private LoggingSystem loggingSystem;

	@NacosConfigListener(dataId = "nacos.log", timeout = 5000)
	public void onChange(String newLog) throws Exception {
		Properties properties = new DefaultPropertiesConfigParse().parse(newLog);
		for (Object t : properties.keySet()) {
			String key = String.valueOf(t);
			if (key.startsWith(LOGGER_TAG)) {
				String strLevel = properties.getProperty(key, "info");
				LogLevel level = LogLevel.valueOf(strLevel.toUpperCase());
				loggingSystem.setLogLevel(key.replace(LOGGER_TAG, ""), level);
				logger.info("{}:{}", key, strLevel);
			}
		}
	}

	@PostConstruct
	public void printLogger() throws Exception {
		Executors.newSingleThreadExecutor().submit(() -> {
			while (true) {
				TimeUnit.SECONDS.sleep(5);
				logger.info("我是info级别日志");
				logger.error("我是error级别日志");
				logger.warn("我是warn级别日志");
				logger.debug("我是debug级别日志");
			}
		});
	}

}
