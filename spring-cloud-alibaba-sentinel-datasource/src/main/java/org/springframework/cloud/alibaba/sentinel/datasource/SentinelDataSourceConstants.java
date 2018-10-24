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

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public interface SentinelDataSourceConstants {

    String PROPERTY_PREFIX = "spring.cloud.sentinel";

    String PROPERTY_ITEM_SEPARATOR = ".";

    String PROPERTY_DATASOURCE_NAME = "datasource";

    String PROPERTY_DATASOURCE_PREFIX = PROPERTY_PREFIX + PROPERTY_ITEM_SEPARATOR
        + PROPERTY_DATASOURCE_NAME;

}
