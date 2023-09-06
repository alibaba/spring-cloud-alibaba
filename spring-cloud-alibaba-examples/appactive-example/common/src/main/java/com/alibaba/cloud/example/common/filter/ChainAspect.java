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

package com.alibaba.cloud.example.common.filter;

import com.alibaba.cloud.example.common.entity.ResultHolder;
import io.appactive.support.log.LogUtil;
import io.appactive.support.sys.JvmPropertyUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class ChainAspect {

	private static final Logger logger = LogUtil.getLogger();

	/**
	 * Add tag information in result.
	 * @param joinPoint joinPoint
	 * @param result result
	 */
	@AfterReturning(
			pointcut = "execution(* com.alibaba.cloud.example.frontend.service.*.*(..)) || "
					+ "execution(* com.alibaba.cloud.example.product.service.*.*(..)) || "
					+ "execution(* com.alibaba.cloud.example.storage.service.*.*(..)) || "
					+ "execution(* com.alibaba.cloud.example.common.service.*.*(..))",
			returning = "result")
	public void afterRunning(JoinPoint joinPoint, Object result) {
		if (result instanceof ResultHolder) {
			ResultHolder resultHolder = (ResultHolder) result;
			resultHolder.addChain(JvmPropertyUtil.getJvmAndEnvValue("appactive.app"),
					JvmPropertyUtil.getJvmAndEnvValue("appactive.unit"));
			logger.info("ChainAspect: " + resultHolder);
		}
	}

}
