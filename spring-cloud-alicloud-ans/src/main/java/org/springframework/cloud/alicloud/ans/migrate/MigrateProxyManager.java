package org.springframework.cloud.alicloud.ans.migrate;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pbting
 */
final class MigrateProxyManager {

	private static final Logger log = LoggerFactory.getLogger(MigrateProxyManager.class);

	private final static AtomicBoolean IS_PROXY = new AtomicBoolean(true);

	private final static Set<String> SERVICES_ID = new ConcurrentSkipListSet<>();

	private static Object springProxyFactory(Object target, ClassLoader classLoader,
			List<Advice> adviceList, Class... interfaces) {
		final ProxyFactory proxyFactory = new ProxyFactory(interfaces);
		proxyFactory.setTarget(target);
		for (Iterator<Advice> iterator = adviceList.iterator(); iterator.hasNext();) {
			proxyFactory.addAdvice(iterator.next());
		}
		return proxyFactory.getProxy(classLoader);
	}

	static Object newServerListProxy(Object bean, ClassLoader classLoader,
			IClientConfig clientConfig) {
		List<Advice> adviceLists = new LinkedList<>();
		adviceLists.add(new ServerListInvocationHandler(clientConfig));
		bean = springProxyFactory(bean, classLoader, adviceLists,
				new Class[] { ServerList.class });
		log.info("[service id]" + clientConfig.getClientName()
				+ " new a ServerList proxy instance for spring cloud netflix to spring cloud alibaba ");
		collectServiceId(clientConfig.getClientName());
		return bean;
	}

	static Object newLoadBalancerProxy(Object bean, ClassLoader classLoader,
			final IClientConfig clientConfig) {
		// step 1: initializer a advice for after returning advice
		final List<Advice> adviceLists = new LinkedList<>();
		adviceLists.add(new MethodInterceptor() {
			private final IClientConfig iclientConfig = clientConfig;

			@Override
			public Object invoke(MethodInvocation methodInvocation) throws Throwable {
				Object returnValue = methodInvocation.proceed();
				String methodName = methodInvocation.getMethod().getName();
				if (!"chooseServer".equals(methodName)) {
					return returnValue;
				}

				String serviceId = iclientConfig.getClientName();
				Server server = ServerListInvocationHandler
						.checkAndGetServiceServer(serviceId, (Server) returnValue);
				ServerListInvocationHandler.incrementCallService(serviceId, server);
				return server;
			}
		});
		// step 2: new proxy instance by spring factory
		bean = springProxyFactory(bean, classLoader, adviceLists,
				new Class[] { ILoadBalancer.class });
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