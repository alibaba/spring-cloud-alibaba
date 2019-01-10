package org.springframework.cloud.alicloud.ans.migrate;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pbting
 */
final class MigrateProxyManager {

	private final static Log log = LogFactory.getLog(MigrateProxyManager.class);
	private final static AtomicBoolean IS_PROXY = new AtomicBoolean(true);

	private final static Set<String> SERVICES_ID = new ConcurrentSkipListSet<>();

	private static Object springProxyFactory(Object target, ClassLoader classLoader,
			List<Advice> adviceList, Class... interfaces) {
		final ProxyFactory proxyFactory = new ProxyFactory(interfaces);
		proxyFactory.setTarget(target);
		adviceList.forEach(advice -> proxyFactory.addAdvice(advice));
		return proxyFactory.getProxy(classLoader);
	}

	static Object newServerListProxy(Object bean, ClassLoader classLoader,
			IClientConfig clientConfig) {
		bean = springProxyFactory(bean, classLoader,
				Arrays.asList(new ServerListInvocationHandler(clientConfig)),
				new Class[] { ServerList.class });
		log.info("[service id]" + clientConfig.getClientName()
				+ " new a ServerList proxy instance for spring cloud netflix to spring cloud alibaba ");
		collectServiceId(clientConfig.getClientName());
		return bean;
	}

	static Object newLoadBalancerProxy(Object bean, ClassLoader classLoader,
			final IClientConfig clientConfig) {

		bean = springProxyFactory(bean, classLoader,
				Arrays.asList(new AfterReturningAdvice() {
					private final IClientConfig iclientConfig = clientConfig;

					@Override
					public void afterReturning(Object returnValue, Method method,
							Object[] args, Object target) {
						String methodName = method.getName();
						if ("chooseServer".equals(methodName)) {
							String serviceId = iclientConfig.getClientName();
							Server server = (Server) returnValue;
							server = ServerListInvocationHandler
									.checkAndGetServiceServer(serviceId, server);
							ServerListInvocationHandler.incrementCallService(serviceId,
									server);
						}
					}
				}), new Class[] { ILoadBalancer.class });
		log.info("[service id]" + clientConfig.getClientName()
				+ " new a ILoadBalancer proxy instance for spring cloud netflix to spring cloud alibaba ");
		return bean;
	}

	static void migrateProxyClose() {
		IS_PROXY.set(false);
	}

	static void migrateProxyUp() {
		IS_PROXY.set(true);
	}

	static boolean isMigrateProxy() {

		return IS_PROXY.get();
	}

	static void collectServiceId(String serviceId) {
		SERVICES_ID.add(serviceId);
	}

	static Set<String> getServicesId() {

		return Collections.unmodifiableSet(SERVICES_ID);
	}
}