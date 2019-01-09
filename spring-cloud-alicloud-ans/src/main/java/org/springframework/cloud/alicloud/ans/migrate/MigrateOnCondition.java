package org.springframework.cloud.alicloud.ans.migrate;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

/**
 * @author pbting
 */
public abstract class MigrateOnCondition implements Condition, BeanClassLoaderAware {

	final String[] conditionOnClass = new String[] {
			"org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration",
			"org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration" };

	ClassLoader classLoader;

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public abstract boolean matches(ConditionContext context,
			AnnotatedTypeMetadata metadata);

	boolean isPresent(String className, ClassLoader classLoader) {
		if (classLoader == null) {
			classLoader = ClassUtils.getDefaultClassLoader();
		}

		try {
			forName(className, classLoader);
			return true;
		}
		catch (Throwable var3) {
			return false;
		}
	}

	Class<?> forName(String className, ClassLoader classLoader)
			throws ClassNotFoundException {
		return classLoader != null ? classLoader.loadClass(className)
				: Class.forName(className);
	}

}