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

package org.springframework.cloud.alibaba.sentinel.datasource;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.ApolloDataSourceFactoryBean;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.ZookeeperDataSourceFactoryBean;

/**
 * Registry to save DataSource FactoryBean
 *
 * @author fangjian
 * @see com.alibaba.csp.sentinel.datasource.DataSource
 * @see FileRefreshableDataSourceFactoryBean
 * @see ZookeeperDataSourceFactoryBean
 * @see NacosDataSourceFactoryBean
 * @see ApolloDataSourceFactoryBean
 */
public class SentinelDataSourceRegistry {

    private static ConcurrentHashMap<String, Class<? extends FactoryBean>> cache = new ConcurrentHashMap<>(
            32);

    static {
        SentinelDataSourceRegistry.registerFactoryBean("file",
                FileRefreshableDataSourceFactoryBean.class);
        SentinelDataSourceRegistry.registerFactoryBean("zk",
                ZookeeperDataSourceFactoryBean.class);
        SentinelDataSourceRegistry.registerFactoryBean("nacos",
                NacosDataSourceFactoryBean.class);
        SentinelDataSourceRegistry.registerFactoryBean("apollo",
                ApolloDataSourceFactoryBean.class);
    }

    public static synchronized void registerFactoryBean(String alias,
                                                        Class<? extends FactoryBean> clazz) {
        cache.putIfAbsent(alias, clazz);
        cache.put(alias, clazz);
    }

    public static Class<? extends FactoryBean> getFactoryBean(String alias) {
        return cache.get(alias);
    }

    public static boolean checkFactoryBean(String alias) {
        return cache.containsKey(alias);
    }

}
