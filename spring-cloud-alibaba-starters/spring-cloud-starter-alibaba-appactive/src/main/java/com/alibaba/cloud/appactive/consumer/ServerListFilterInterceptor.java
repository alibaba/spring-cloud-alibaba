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

package com.alibaba.cloud.appactive.consumer;

import java.util.List;

import com.netflix.loadbalancer.Server;
import io.appactive.support.lang.CollectionUtils;
import io.appactive.support.log.LogUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
@Aspect
public class ServerListFilterInterceptor {

	private static final Logger logger = LogUtil.getLogger();

	/**
	 * filtering servers for every ribbon request.
	 * @param pjp ProceedingJoinPoint
	 * @return filtered servers
	 */
	@Around("execution(* com.netflix.loadbalancer.BaseLoadBalancer.getAllServers(..))"
			+ "|| execution(* com.netflix.loadbalancer.BaseLoadBalancer.getReachableServers(..))")
	public List<Server> around(ProceedingJoinPoint pjp) {
		logger.debug("ServerListFilterInterceptor at around {}", pjp.getSignature());
		List<Server> finalServers = null;
		try {
			Object result = pjp.proceed();
			if (result instanceof List) {
				List list = (List) result;
				if (CollectionUtils.isNotEmpty(list) && list.get(0) instanceof Server) {
					List<Server> servers = (List<Server>) list;
					logger.debug("origin servers {}", servers);
					finalServers = ConsumerRouter.filter(servers);
					logger.debug("filtered servers {}", finalServers);
				}
			}
		}
		catch (Throwable th) {
			logger.error("error filtering server list ", th);
		}
		return finalServers;
	}

	/**
	 * refresh local server list cache when servers changes.
	 * @param jp JoinPoint
	 */
	@After("execution(* com.netflix.loadbalancer.BaseLoadBalancer.setServersList(..))")
	public void after(JoinPoint jp) {
		if (!"com.netflix.loadbalancer.BaseLoadBalancer"
				.equals(jp.getTarget().getClass().getName())) {
			// avoid subclass triggering
			return;
		}
		logger.debug("ServerListFilterInterceptor at after {}", jp.getSignature());
		Object[] args = jp.getArgs();
		if (args.length > 0) {
			List<Server> servers = (List<Server>) args[0];
			Integer num = ConsumerRouter.refresh(servers);
			if (num > 0) {
				logger.info("new servers {}, updated {} services[app+uri]", servers, num);
			}
			else {
				logger.info("new servers {}, no services[app+uri] updated {} ", servers,
						num);
			}
		}
	}

}
