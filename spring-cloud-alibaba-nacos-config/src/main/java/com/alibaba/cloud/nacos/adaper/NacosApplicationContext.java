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
 *//*
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

package com.alibaba.cloud.nacos.adaper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;

/**
 * To solve for situations where there are multiple contexts
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
class NacosApplicationContext implements ConfigurableApplicationContext {

	private volatile static Set<ApplicationContext> contextList = new HashSet<>();

	private ConfigurableApplicationContext target;

	private final Object monitor = new Object();

	private boolean initialize = false;

	NacosApplicationContext(ConfigurableApplicationContext target) {
		this.target = target;
		contextList.add(target);
	}

	private void initContexts() {
		synchronized (monitor) {
			Set<ApplicationContext> tmp = new HashSet<>();
			for (ApplicationContext context : contextList) {
				tmp.add(context);
				while (context.getParent() != null) {
					tmp.add(context.getParent());
					context = context.getParent();
				}
			}
			contextList = tmp;
			initialize = true;
		}
	}

	@Override
	public void publishEvent(ApplicationEvent event) {
		if (!initialize) {
			initContexts();
		}
		for (ApplicationContext context : contextList) {
			context.publishEvent(event);
		}
	}

	@Override
	public void setId(String id) {
		target.setId(id);
	}

	@Override
	public void setParent(ApplicationContext parent) {
		target.setParent(parent);
	}

	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		target.setEnvironment(environment);
	}

	@Override
	public ConfigurableEnvironment getEnvironment() {
		return target.getEnvironment();
	}

	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		target.addBeanFactoryPostProcessor(postProcessor);
	}

	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		target.addApplicationListener(listener);
	}

	@Override
	public void addProtocolResolver(ProtocolResolver resolver) {
		target.addProtocolResolver(resolver);
	}

	@Override
	public void refresh() throws BeansException, IllegalStateException {
		target.refresh();
	}

	@Override
	public void registerShutdownHook() {
		target.registerShutdownHook();
	}

	@Override
	public void close() {
		target.close();
	}

	@Override
	public boolean isActive() {
		return target.isActive();
	}

	@Override
	public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
		return target.getBeanFactory();
	}

	@Override
	public String getId() {
		return target.getId();
	}

	@Override
	public String getApplicationName() {
		return target.getApplicationName();
	}

	@Override
	public String getDisplayName() {
		return target.getDisplayName();
	}

	@Override
	public long getStartupDate() {
		return target.getStartupDate();
	}

	@Override
	public ApplicationContext getParent() {
		return target.getParent();
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory()
			throws IllegalStateException {
		return target.getAutowireCapableBeanFactory();
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		return target.getParentBeanFactory();
	}

	@Override
	public boolean containsLocalBean(String s) {
		return target.containsLocalBean(s);
	}

	@Override
	public boolean containsBeanDefinition(String s) {
		return target.containsBeanDefinition(s);
	}

	@Override
	public int getBeanDefinitionCount() {
		return target.getBeanDefinitionCount();
	}

	@Override
	public String[] getBeanDefinitionNames() {
		return target.getBeanDefinitionNames();
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType resolvableType) {
		return target.getBeanNamesForType(resolvableType);
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType resolvableType, boolean b,
			boolean b1) {
		return target.getBeanNamesForType(resolvableType, b, b1);
	}

	@Override
	public String[] getBeanNamesForType(Class<?> aClass) {
		return target.getBeanNamesForType(aClass);
	}

	@Override
	public String[] getBeanNamesForType(Class<?> aClass, boolean b, boolean b1) {
		return target.getBeanNamesForType(aClass, b, b1);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> aClass) throws BeansException {
		return target.getBeansOfType(aClass);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> aClass, boolean b, boolean b1)
			throws BeansException {
		return target.getBeansOfType(aClass, b, b1);
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> aClass) {
		return target.getBeanNamesForAnnotation(aClass);
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> aClass)
			throws BeansException {
		return target.getBeansWithAnnotation(aClass);
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String s, Class<A> aClass)
			throws NoSuchBeanDefinitionException {
		return target.findAnnotationOnBean(s, aClass);
	}

	@Override
	public Object getBean(String s) throws BeansException {
		return target.getBean(s);
	}

	@Override
	public <T> T getBean(String s, Class<T> aClass) throws BeansException {
		return target.getBean(s, aClass);
	}

	@Override
	public Object getBean(String s, Object... objects) throws BeansException {
		return target.getBean(s, objects);
	}

	@Override
	public <T> T getBean(Class<T> aClass) throws BeansException {
		return target.getBean(aClass);
	}

	@Override
	public <T> T getBean(Class<T> aClass, Object... objects) throws BeansException {
		return target.getBean(aClass, objects);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> aClass) {
		return target.getBeanProvider(aClass);
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType resolvableType) {
		return target.getBeanProvider(resolvableType);
	}

	@Override
	public boolean containsBean(String s) {
		return target.containsBean(s);
	}

	@Override
	public boolean isSingleton(String s) throws NoSuchBeanDefinitionException {
		return target.isSingleton(s);
	}

	@Override
	public boolean isPrototype(String s) throws NoSuchBeanDefinitionException {
		return target.isPrototype(s);
	}

	@Override
	public boolean isTypeMatch(String s, ResolvableType resolvableType)
			throws NoSuchBeanDefinitionException {
		return target.isTypeMatch(s, resolvableType);
	}

	@Override
	public boolean isTypeMatch(String s, Class<?> aClass)
			throws NoSuchBeanDefinitionException {
		return target.isTypeMatch(s, aClass);
	}

	@Override
	public Class<?> getType(String s) throws NoSuchBeanDefinitionException {
		return target.getType(s);
	}

	@Override
	public Class<?> getType(String s, boolean b) throws NoSuchBeanDefinitionException {
		return target.getType(s, b);
	}

	@Override
	public String[] getAliases(String s) {
		return target.getAliases(s);
	}

	@Override
	public void publishEvent(Object event) {
		if (!initialize) {
			initContexts();
		}
		for (ApplicationContext context : contextList) {
			context.publishEvent(event);
		}
	}

	@Override
	public void start() {
		target.start();
	}

	@Override
	public void stop() {
		target.stop();
	}

	@Override
	public boolean isRunning() {
		return target.isRunning();
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage,
			Locale locale) {
		return target.getMessage(code, args, defaultMessage, locale);
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale)
			throws NoSuchMessageException {
		return target.getMessage(code, args, locale);
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
			throws NoSuchMessageException {
		return target.getMessage(resolvable, locale);
	}

	@Override
	public Resource[] getResources(String s) throws IOException {
		return target.getResources(s);
	}

	@Override
	public Resource getResource(String s) {
		return target.getResource(s);
	}

	@Override
	public ClassLoader getClassLoader() {
		return target.getClassLoader();
	}
}
