/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.examples.schedulerx.job;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.JavaProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;

import org.springframework.stereotype.Component;

/**
 * Sharding job demo.
 *
 * @author xiaomeng.hxm
 */
@Component
public class ShardingJob extends JavaProcessor {

	@Override
	public ProcessResult process(JobContext context) throws Exception {
		System.out.println("shardingId=" + context.getShardingId() + ", shardingPara="
				+ context.getShardingParameter());
		return new ProcessResult(true);
	}

}
