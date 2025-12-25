/*
 * Copyright 2024-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.scheduling.schedulerx;

import com.alibaba.cloud.scheduling.SchedulingConstants;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * schedulerx2 starter.
 *
 * @author yaohui
 **/
@EnableConfigurationProperties(SchedulerxProperties.class)
@ConditionalOnProperty(name = SchedulingConstants.SCHEDULING_CONFIG_DISTRIBUTED_MODE_KEY, havingValue = "schedulerx")
@Import({SchedulerxConfigurations.SchedulerxWorkerConfiguration.class,
		SchedulerxConfigurations.SpringScheduleAdaptConfiguration.class})
public class SchedulerxAutoConfigure {

}
