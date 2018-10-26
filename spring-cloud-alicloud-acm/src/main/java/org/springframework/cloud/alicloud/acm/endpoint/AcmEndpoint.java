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

package org.springframework.cloud.alicloud.acm.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cloud.alicloud.acm.AcmPropertySourceRepository;
import org.springframework.cloud.alicloud.acm.bootstrap.AcmPropertySource;
import org.springframework.cloud.alicloud.acm.refresh.AcmRefreshHistory;
import org.springframework.cloud.alicloud.context.acm.AcmProperties;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 01/10/2017.
 *
 * @author juven.xuxb
 */
@Endpoint(id = "acm")
public class AcmEndpoint {

    private final AcmProperties properties;

    private final AcmRefreshHistory refreshHistory;

    private final AcmPropertySourceRepository propertySourceRepository;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AcmEndpoint(AcmProperties properties, AcmRefreshHistory refreshHistory,
                       AcmPropertySourceRepository propertySourceRepository) {
        this.properties = properties;
        this.refreshHistory = refreshHistory;
        this.propertySourceRepository = propertySourceRepository;
    }

    @ReadOperation
    public Map<String, Object> invoke() {
        Map<String, Object> result = new HashMap<>();
        result.put("config", properties);

        Map<String, Object> runtime = new HashMap<>();
        List<AcmPropertySource> all = propertySourceRepository.getAll();

        List<Map<String, Object>> sources = new ArrayList<>();
        for (AcmPropertySource ps : all) {
            Map<String, Object> source = new HashMap<>();
            source.put("dataId", ps.getDataId());
            source.put("lastSynced", dateFormat.format(ps.getTimestamp()));
            sources.add(source);
        }
        runtime.put("sources", sources);
        runtime.put("refreshHistory", refreshHistory.getRecords());

        result.put("runtime", runtime);
        return result;
    }
}
