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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.cloud.sentinel.feign.handler.FeignResourceHandler;
import com.alibaba.cloud.sentinel.feign.handler.FeignResourceHandlerFactory;
import com.alibaba.cloud.sentinel.feign.handler.ResourceHandler;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import feign.Feign;
import feign.InvocationHandlerFactory.MethodHandler;
import feign.MethodMetadata;
import feign.Target;
import feign.hystrix.FallbackFactory;

import static feign.Util.checkNotNull;

/**
 * {@link InvocationHandler} handle invocation that protected by Sentinel.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class SentinelInvocationHandler implements InvocationHandler {

	private final Target<?> target;

	private final Map<Method, MethodHandler> dispatch;

	private FallbackFactory fallbackFactory;

	private Map<Method, Method> fallbackMethodMap;

	public SentinelInvocationHandler(Target<?> target,
			Map<Method, MethodHandler> dispatch, FallbackFactory fallbackFactory) {
		this.target = checkNotNull(target, "target");
		this.dispatch = checkNotNull(dispatch, "dispatch");
		this.fallbackFactory = fallbackFactory;
		this.fallbackMethodMap = toFallbackMethod(dispatch);
	}

	public SentinelInvocationHandler(Target<?> target,
			Map<Method, MethodHandler> dispatch) {
		this.target = checkNotNull(target, "target");
		this.dispatch = checkNotNull(dispatch, "dispatch");
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args)
			throws Throwable {
		// invoke object methods
		if (Object.class.equals(method.getDeclaringClass())) {
			return invokeObjectMethod(proxy, method, args);
		}
		// invoke proxy method
		return invokeProxyMethod(proxy, method, args);
	}

	protected MethodMetadata parseMethodMetadata(
			Target.HardCodedTarget<?> hardCodedTarget, Method method) {
		return SentinelContractHolder.METADATA_MAP.get(hardCodedTarget.type().getName()
				+ Feign.configKey(hardCodedTarget.type(), method));
	}

	private FeignResourceHandler parseResourceHandler(Method method) {
		ResourceHandler resourceHandler = method.getAnnotation(ResourceHandler.class);
		if (resourceHandler == null) {
			resourceHandler = target.type().getAnnotation(ResourceHandler.class);
		}
		if (resourceHandler == null) {
			return FeignResourceHandlerFactory.getDefaultHandler();
		}
		return FeignResourceHandlerFactory.getHandler(resourceHandler.value());
	}

	protected Object invokeProxyMethod(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		Object result;
		MethodHandler methodHandler = this.dispatch.get(method);
		// only handle by HardCodedTarget
		if (target instanceof Target.HardCodedTarget) {
			Target.HardCodedTarget<?> hardCodedTarget = (Target.HardCodedTarget<?>) target;
			MethodMetadata methodMetadata = parseMethodMetadata(hardCodedTarget, method);

			if (methodMetadata == null) {
				result = methodHandler.invoke(args);
			}
			else {
				String resourceName = parseResourceHandler(method)
						.makeResourceName(this.target, methodMetadata);
				Entry entry = null;
				try {
					ContextUtil.enter(resourceName);
					entry = SphU.entry(resourceName, EntryType.OUT, 1, args);
					result = methodHandler.invoke(args);
				}
				catch (Throwable ex) {
					// fallback handle
					if (!BlockException.isBlockException(ex)) {
						Tracer.trace(ex);
					}
					if (fallbackFactory != null) {
						try {
							Object fallbackResult = fallbackMethodMap.get(method)
									.invoke(fallbackFactory.create(ex), args);
							return fallbackResult;
						}
						catch (IllegalAccessException e) {
							// shouldn't happen as method is public due to being an
							// interface
							throw new AssertionError(e);
						}
						catch (InvocationTargetException e) {
							throw new AssertionError(e.getCause());
						}
					}
					else {
						// throw exception if fallbackFactory is null
						throw ex;
					}
				}
				finally {
					if (entry != null) {
						entry.exit(1, args);
					}
					ContextUtil.exit();
				}
			}
		}
		else {
			// other target type using default strategy
			result = methodHandler.invoke(args);
		}

		return result;
	}

	private Object invokeObjectMethod(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		if ("equals".equals(method.getName())) {
			try {
				Object otherHandler = args.length > 0 && args[0] != null
						? Proxy.getInvocationHandler(args[0]) : null;
				return equals(otherHandler);
			}
			catch (IllegalArgumentException e) {
				return false;
			}
		}
		return method.invoke(this, args);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SentinelInvocationHandler) {
			SentinelInvocationHandler other = (SentinelInvocationHandler) obj;
			return target.equals(other.target);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return target.hashCode();
	}

	@Override
	public String toString() {
		return target.toString();
	}

	static Map<Method, Method> toFallbackMethod(Map<Method, MethodHandler> dispatch) {
		Map<Method, Method> result = new LinkedHashMap<>();
		for (Method method : dispatch.keySet()) {
			method.setAccessible(true);
			result.put(method, method);
		}
		return result;
	}

}
