package org.springframework.cloud.alicloud.ans.migrate;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MigrateRibbonBeanPostProcessor
		implements BeanPostProcessor, BeanClassLoaderAware {

	private static final Logger log = LoggerFactory.getLogger(MigrateOnCondition.class);

	private ClassLoader classLoader;
	private IClientConfig clientConfig;

	public MigrateRibbonBeanPostProcessor(IClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {

		// step 1 : check the bean whether proxy or not
		if (!MigrateProxyManager.isMigrateProxy()) {
			log.info("Migrate proxy is Close.");
			return bean;
		}

		// step 2 : proxy the designated bean
		if (bean instanceof ServerList) {
			bean = MigrateProxyManager.newServerListProxy(bean, classLoader,
					clientConfig);
		}

		if (bean instanceof ILoadBalancer) {
			bean = MigrateProxyManager.newLoadBalancerProxy(bean, classLoader,
					clientConfig);
		}
		return bean;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

}