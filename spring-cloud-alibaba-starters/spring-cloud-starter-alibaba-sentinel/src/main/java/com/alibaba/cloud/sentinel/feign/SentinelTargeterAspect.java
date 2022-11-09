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

package com.alibaba.cloud.sentinel.feign;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Record FeignClientFactoryBean to threadlocal, so that SentinelFeign can get it when
 * creating SentinelInvocationHandler.
 *
 * @see com.alibaba.cloud.sentinel.feign.SentinelFeign.Builder
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
@Aspect
public class SentinelTargeterAspect {

	private static final ThreadLocal<Object> FEIGN_CLIENT_FACTORY_BEAN = new ThreadLocal<>();

	public static Object getFeignClientFactoryBean() {
		return FEIGN_CLIENT_FACTORY_BEAN.get();
	}

	@Around("execution(* org.springframework.cloud.openfeign.Targeter.target(..))")
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		Object factory = pjp.getArgs()[0];
		try {
			FEIGN_CLIENT_FACTORY_BEAN.set(factory);
			return pjp.proceed();
		}
		finally {
			FEIGN_CLIENT_FACTORY_BEAN.remove();
		}
	}

}
