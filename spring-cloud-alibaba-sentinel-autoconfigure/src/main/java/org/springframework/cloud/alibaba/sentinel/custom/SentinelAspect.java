/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xiaojing
 */
@Aspect
public class SentinelAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(SentinelAspect.class);

	@Around("@annotation(org.springframework.cloud.alibaba.sentinel.custom.EnableSentinel)")
	public Object customBlock(ProceedingJoinPoint pjp) throws Throwable {
		SentinelEntry sentinelEntry = new SentinelEntry();
		try {
			beforeProceed(sentinelEntry, pjp);
			return pjp.proceed();
		}
		catch (BlockException e) {
			LOGGER.error(e.getMessage(), e);
			if (null != sentinelEntry.getHandler()
					&& sentinelEntry.getHandler().length() > 0) {
				return HandlerUtil.getHandler(sentinelEntry.getHandler()).handler(e);
			}
			else {
				throw e;
			}
		}
		finally {
			releaseContextResources(sentinelEntry);
		}
	}

	private void beforeProceed(SentinelEntry sentinelEntry, ProceedingJoinPoint pjp)
			throws BlockException {
		Method method = getMethod(pjp);

		int modifiers = method.getModifiers();

		if (!Modifier.isPublic(modifiers) || "toString".equals(method.getName())
				|| "hashCode".equals(method.getName())
				|| "equals".equals(method.getName())
				|| "finalize".equals(method.getName())) {
			return;
		}

		Annotation[] annotations = method.getDeclaredAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof EnableSentinel) {
				sentinelEntry.setKey(((EnableSentinel) annotation).value());
				sentinelEntry.setHandler(((EnableSentinel) annotation).handler());
			}
		}
		if (null == sentinelEntry.getKey() || sentinelEntry.getKey().length() == 0) {
			return;
		}

		ContextUtil.enter(sentinelEntry.getKey());
		sentinelEntry.setEntry(SphU.entry(sentinelEntry.getKey()));
	}

	private void releaseContextResources(SentinelEntry sentinelEntry) {

		if (null == sentinelEntry.getEntry()) {
			return;
		}

		Entry entry = sentinelEntry.getEntry();
		entry.exit();
		ContextUtil.exit();
	}

	private Method getMethod(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		return signature.getMethod();
	}

}
