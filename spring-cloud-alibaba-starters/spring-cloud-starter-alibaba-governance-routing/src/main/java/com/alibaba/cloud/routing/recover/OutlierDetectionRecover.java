/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.recover;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.cloud.routing.RoutingProperties;
import com.alibaba.cloud.routing.model.ServiceInstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xqw
 */

public class OutlierDetectionRecover {

	@Autowired
	private RoutingProperties routingProperties;

	private static final Logger log = LoggerFactory
			.getLogger(OutlierDetectionRecover.class);

	// move to spring config
	private static final boolean enabledInstanceRecoverTask = true;

	public void updateInstanceStatus(String targetServiceName) {

		if (enabledInstanceRecoverTask) {
			log.info(
					"The instance recover task is started. Please pay attention to the service status.");
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					recover(targetServiceName);
				}
			}, 0, routingProperties.getRecoverInterval());
		}
	}

	/**
	 * Recover instance and error count，setting consecutiveErrors And When.
	 * consecutiveErrors to 5, down this forever.
	 */
	private void recover(String targetServiceName) {

		List<ServiceInstanceInfo> calledErrorInstance = GlobalInstanceStatusListCache
				.getCalledErrorInstance();
		double minHealthPercent = routingProperties.getMinHealthPercent();
		int removeUpperLimitNum = GlobalInstanceStatusListCache
				.getServiceUpperLimitRatioNum(targetServiceName, minHealthPercent);
		int unHealthInstanceNum = GlobalInstanceStatusListCache.getRemoveInstanceNum(targetServiceName);
		long baseEjectionTime = routingProperties.getBaseEjectionTime();

		System.out.println(
				"最大移除上限数：" + removeUpperLimitNum +
				"，不健康实例数：" + unHealthInstanceNum +
				"，缓存中 " + targetServiceName + " 的服务实例数："
						+ GlobalInstanceStatusListCache.getInstanceNumByTargetServiceName(targetServiceName) );

		for (ServiceInstanceInfo sif : calledErrorInstance) {

			// 判断错误率是否合格？ use metrics!
			if (sif.getConsecutiveErrors().get() == 2) {
				System.out.println("错误次数达到上限，进入摘除逻辑...");

				// 判断是否达到上限比
				if (!(removeUpperLimitNum == unHealthInstanceNum)) {
					System.out.println("通过摘除上限比判断，准备摘除...");
					// 摘除
					sif.setStatus(false);
					sif.setRemoveTime(System.currentTimeMillis());
					System.err.println("成功摘除：" + GlobalInstanceStatusListCache.getAll());
				}

				GlobalInstanceStatusListCache.setInstanceInfoByInstanceNames(sif);
				System.err.println("");
			}
			else {

				System.out.println("错误率条件不成立，进入实例恢复....");

				System.out.println(sif);

				// 不健康 当前的时间 - 上次摘除的时间 == 恢复时间
				long current = System.currentTimeMillis();
				long removeTime = sif.getRemoveTime();

				if ((current - removeTime) > baseEjectionTime) {
					// 恢复实例 设置健康状态 status ——> true
					sif.setStatus(true);
				}

				GlobalInstanceStatusListCache.setInstanceInfoByInstanceNames(sif);
			}
		}

	}

}
