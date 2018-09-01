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

package org.springframework.cloud.alibaba.storage.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.context.ApplicationContext;

import com.aliyun.oss.OSSClient;

/**
 * Actuator Endpoint to expose OSS Meta Data
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class OSSEndpoint extends AbstractEndpoint<Map<String, Object>> {

    @Autowired
    private ApplicationContext applicationContext;

    public OSSEndpoint() {
        super("oss");
    }

    @Override
    public Map<String, Object> invoke() {
        Map<String, Object> result = new HashMap<>();

        Map<String, OSSClient> ossClientMap = applicationContext
                .getBeansOfType(OSSClient.class);

        int size = ossClientMap.size();

        List<Object> ossClientList = new ArrayList<>();

        for(String beanName : ossClientMap.keySet()) {
            Map<String, Object> ossProperties = new HashMap<>();
            OSSClient client = ossClientMap.get(beanName);
            ossProperties.put("beanName", beanName);
            ossProperties.put("endpoint", client.getEndpoint().toString());
            ossProperties.put("clientConfiguration", client.getClientConfiguration());
            ossProperties.put("credentials",
                    client.getCredentialsProvider().getCredentials());

            String[] bucketList = new String[client.listBuckets().size()];

            for(int index = 0; index < bucketList.length; index ++) {
                bucketList[index] = client.listBuckets().get(index).getName();
            }

            ossProperties.put("bucketList", bucketList);
            ossClientList.add(ossProperties);
        }

        result.put("size", size);
        result.put("info", ossClientList);

        return result;
    }

}