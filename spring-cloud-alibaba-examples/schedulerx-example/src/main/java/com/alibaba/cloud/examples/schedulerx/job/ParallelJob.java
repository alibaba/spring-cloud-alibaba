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
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.alibaba.cloud.examples.schedulerx.domain.AccountInfo;
import com.alibaba.schedulerx.common.util.JsonUtil;
import com.alibaba.schedulerx.common.util.StringUtils;
import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.MapReduceJobProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Visual MapReduce job support user defined lables.
 *
 * @author xiaomeng.hxm
 **/
@Component
public class ParallelJob extends MapReduceJobProcessor {

	private static final Logger logger = LoggerFactory.getLogger("schedulerx");

	@Override
	public ProcessResult reduce(JobContext context) throws Exception {
		return new ProcessResult(true);
	}

	@Override
	public ProcessResult process(JobContext context) throws Exception {
		if (isRootTask(context)) {
			logger.info("start to constract task list");
			List<AccountInfo> list = Lists.newArrayList();
			for (int i = 0; i < 20; i++) {
				list.add(new AccountInfo(i, "CUS" + StringUtils.leftPad(i + "", 4, "0"),
						"AC" + StringUtils.leftPad(i + "", 12, "0")));
			}
			return map(list, "transfer");
		}
		else {
			AccountInfo obj = (AccountInfo) context.getTask();
			// do something
			logger.info("accountInfo:{}", JsonUtil.toJson(obj));
			TimeUnit.SECONDS.sleep(2 / new Random().nextInt(2));
			return new ProcessResult(true);
		}
	}

}
