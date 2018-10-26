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

package org.springframework.cloud.alicloud.acm.bootstrap;

import org.springframework.core.env.MapPropertySource;

import java.util.Date;
import java.util.Map;

/**
 * @author juven.xuxb
 * @author xiaolongzuo
 */
public class AcmPropertySource extends MapPropertySource {

    private final String dataId;

    private final Date timestamp;

    private final boolean groupLevel;

    AcmPropertySource(String dataId, Map<String, Object> source, Date timestamp,
                      boolean groupLevel) {
        super(dataId, source);
        this.dataId = dataId;
        this.timestamp = timestamp;
        this.groupLevel = groupLevel;
    }

    public String getDataId() {
        return dataId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isGroupLevel() {
        return groupLevel;
    }
}
