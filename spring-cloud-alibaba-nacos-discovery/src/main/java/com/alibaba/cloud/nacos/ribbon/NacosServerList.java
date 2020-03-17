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

package com.alibaba.cloud.nacos.ribbon;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;

/**
 * @author xiaojing
 * @author renhaojun
 */
public class NacosServerList extends AbstractServerList<Server> {

    private IClientConfig clientConfig;
    private NacosDiscoveryProperties discoveryProperties;

    private String serviceId;

    public NacosServerList(NacosDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    public NacosServerList(NacosDiscoveryProperties discoveryProperties, IClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.discoveryProperties = discoveryProperties;
    }


    @Override
    public List<Server> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<Server> getUpdatedListOfServers() {
        return getServers();
    }

    private List<Server> getServers() {
        //先寻找配置文件中的地址
        String listOfServers = clientConfig.get(CommonClientConfigKey.ListOfServers);

        List<Server> serverList = derive(listOfServers);
        if (serverList != null && serverList.size() != 0) {
            return serverList;
        }
        // 如果配置文件找不到，在nacos中继续寻找
        try {
            String group = discoveryProperties.getGroup();
            List<Instance> instances = discoveryProperties.namingServiceInstance()
                    .selectInstances(serviceId, group, true);
            return instancesToServerList(instances);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Can not get service instances from nacos, serviceId=" + serviceId,
                    e);
        }
    }

    private List<Server> instancesToServerList(List<Instance> instances) {
        List<Server> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(instances)) {
            return result;
        }
        for (Instance instance : instances) {
            result.add(new NacosServer(instance));
        }

        return result;
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        this.serviceId = iClientConfig.getClientName();
    }

    protected List<Server> derive(String value) {
        List<Server> list = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(value)) {
            for (String s : value.split(",")) {
                list.add(new Server(s.trim()));
            }
        }
        return list;
    }
}