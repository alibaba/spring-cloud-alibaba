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

import java.util.List;
import java.util.Map;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.MapReduceJobProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;
import com.google.common.collect.Lists;

import org.springframework.stereotype.Component;

/**
 * MapReduce job demo.
 *
 * @author xiaomeng.hxm
 **/
@Component
public class MapReduceJob extends MapReduceJobProcessor {

	@Override
	public ProcessResult process(JobContext context) throws Exception {
		String taskName = context.getTaskName();
		int dispatchNum = 50;
		if (context.getJobParameters() != null) {
			dispatchNum = Integer.valueOf(context.getJobParameters());
		}
		if (isRootTask(context)) {
			System.out.println("start root task");
			List<String> msgList = Lists.newArrayList();
			for (int i = 0; i <= dispatchNum; i++) {
				msgList.add("msg_" + i);
			}
			return map(msgList, "Level1Dispatch");
		}
		else if (taskName.equals("Level1Dispatch")) {
			String task = (String) context.getTask();
			Thread.sleep(2000);
			return new ProcessResult(true, task);
		}

		return new ProcessResult(false);
	}

	@Override
	public ProcessResult reduce(JobContext context) throws Exception {
		for (Map.Entry<Long, String> result : context.getTaskResults().entrySet()) {
			System.out.println(
					"taskId:" + result.getKey() + ", result:" + result.getValue());
		}
		return new ProcessResult(true, "MapReduceJob.reduce");
	}

}
