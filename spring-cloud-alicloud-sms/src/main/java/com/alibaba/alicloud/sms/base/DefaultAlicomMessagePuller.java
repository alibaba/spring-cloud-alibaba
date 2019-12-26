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

package com.alibaba.alicloud.sms.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.common.ClientException;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.model.Message;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 阿里通信官方消息默认拉取工具类.
 */
public class DefaultAlicomMessagePuller {

	private static final Logger log = LoggerFactory
			.getLogger(DefaultAlicomMessagePuller.class);

	private String mnsAccountEndpoint = "https://1943695596114318.mns.cn-hangzhou.aliyuncs.com/"; // 阿里通信消息的endpoint,固定。

	private String endpointNameForPop = "cn-hangzhou";

	private String regionIdForPop = "cn-hangzhou";

	private String domainForPop = "dybaseapi.aliyuncs.com";

	private TokenGetterForAlicom tokenGetter;

	private MessageListener messageListener;

	private boolean isRunning = false;

	private Integer pullMsgThreadSize = 1;

	private boolean debugLogOpen = false;

	private Integer sleepSecondWhenNoData = 30;

	public void openDebugLog(boolean debugLogOpen) {
		this.debugLogOpen = debugLogOpen;
	}

	public Integer getSleepSecondWhenNoData() {
		return sleepSecondWhenNoData;
	}

	public void setSleepSecondWhenNoData(Integer sleepSecondWhenNoData) {
		this.sleepSecondWhenNoData = sleepSecondWhenNoData;
	}

	public Integer getPullMsgThreadSize() {
		return pullMsgThreadSize;
	}

	public void setPullMsgThreadSize(Integer pullMsgThreadSize) {
		if (pullMsgThreadSize != null && pullMsgThreadSize > 1) {
			this.pullMsgThreadSize = pullMsgThreadSize;
		}
	}

	private ExecutorService executorService;

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	protected static final Map<String, Object> S_LOCK_OBJ_MAP = new HashMap<>();

	protected static Map<String, Boolean> sPollingMap = new ConcurrentHashMap<>();

	protected Object lockObj;

	public boolean setPolling(String queueName) {
		synchronized (lockObj) {
			Boolean ret = sPollingMap.get(queueName);
			if (ret == null || !ret) {
				sPollingMap.put(queueName, true);
				return true;
			}
			return false;
		}
	}

	public void clearPolling(String queueName) {
		synchronized (lockObj) {
			sPollingMap.put(queueName, false);
			lockObj.notifyAll();
			if (debugLogOpen) {
				log.info("PullMessageTask_WakeUp:Everyone WakeUp and Work!");
			}
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean running) {
		isRunning = running;
	}

	/**
	 * @param accessKeyId accessKeyId
	 * @param accessKeySecret accessKeySecret
	 * @param messageType 消息类型
	 * @param queueName 队列名称
	 * @param messageListener 回调的listener,用户自己实现
	 * @throws com.aliyuncs.exceptions.ClientException throw by sdk
	 * @throws ParseException throw parse exception
	 */
	public void startReceiveMsg(String accessKeyId, String accessKeySecret,
			String messageType, String queueName, MessageListener messageListener)
			throws com.aliyuncs.exceptions.ClientException, ParseException {

		startReceiveMsg0(accessKeyId, accessKeySecret, null,  messageType, queueName,
				regionIdForPop, endpointNameForPop, domainForPop, mnsAccountEndpoint, messageListener);
	}

	/**
	 * @param accessKeyId accessKeyId
	 * @param accessKeySecret accessKeySecret
	 * @param messageType 消息类型
	 * @param queueName 队列名称
	 * @param regionIdForPop region
	 * @param endpointNameForPop endpoint name
	 * @param domainForPop domain
	 * @param mnsAccountEndpoint mns account endpoint
	 * @param messageListener 回调的listener,用户自己实现
	 * @throws com.aliyuncs.exceptions.ClientException throw by sdk
	 * @throws ParseException throw parse exception
	 */
	public void startReceiveMsgForVPC(String accessKeyId, String accessKeySecret,
			String messageType, String queueName, String regionIdForPop,
			String endpointNameForPop, String domainForPop, String mnsAccountEndpoint,
			MessageListener messageListener)
			throws com.aliyuncs.exceptions.ClientException, ParseException {

		startReceiveMsg0(accessKeyId, accessKeySecret, null,  messageType, queueName,
				regionIdForPop, endpointNameForPop, domainForPop, mnsAccountEndpoint, messageListener);
	}

	/**
	 * 虚商用户定制接收消息方法.
	 * @param accessKeyId accessKeyId
	 * @param accessKeySecret accessKeySecret
	 * @param ownerId 实际的ownerId
	 * @param messageType 消息类型
	 * @param queueName 队列名称
	 * @param messageListener 回调listener
	 * @throws com.aliyuncs.exceptions.ClientException throw by sdk
	 * @throws ParseException throw parse exception
	 */
	public void startReceiveMsgForPartnerUser(String accessKeyId, String accessKeySecret,
			Long ownerId, String messageType, String queueName,
			MessageListener messageListener)
			throws com.aliyuncs.exceptions.ClientException, ParseException {

		startReceiveMsg0(accessKeyId, accessKeySecret, ownerId, messageType, queueName,
				regionIdForPop, endpointNameForPop, domainForPop, mnsAccountEndpoint, messageListener);
	}

	public void stop() {
		isRunning = false;
	}

	private void startReceiveMsg0(String accessKeyId, String accessKeySecret, Long ownerId,
			String messageType, String queueName, String regionIdForPop,
			String endpointNameForPop, String domainForPop, String mnsAccountEndpoint,
			 MessageListener messageListener)
			throws com.aliyuncs.exceptions.ClientException {

		if(mnsAccountEndpoint != null) {
			this.mnsAccountEndpoint = mnsAccountEndpoint;
		}

		tokenGetter = new TokenGetterForAlicom(accessKeyId, accessKeySecret,
				endpointNameForPop, regionIdForPop, domainForPop, ownerId);

		this.messageListener = messageListener;
		isRunning = true;
		PullMessageTask task = new PullMessageTask();
		task.messageType = messageType;
		task.queueName = queueName;

		synchronized (S_LOCK_OBJ_MAP) {
			lockObj = S_LOCK_OBJ_MAP.get(queueName);
			if (lockObj == null) {
				lockObj = new Object();
				S_LOCK_OBJ_MAP.put(queueName, lockObj);
			}
		}

		if (executorService == null) {
			ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
					pullMsgThreadSize,
					new BasicThreadFactory.Builder()
							.namingPattern(
									"PullMessageTask-" + messageType + "-thread-pool-%d")
							.daemon(true).build());
			executorService = scheduledExecutorService;
		}
		for (int i = 0; i < pullMsgThreadSize; i++) {
			executorService.execute(task);
		}
	}

	private class PullMessageTask implements Runnable {

		private String messageType;

		private String queueName;

		@Override
		public void run() {

			boolean polling = false;
			while (isRunning) {
				try {
					synchronized (lockObj) {
						Boolean p = sPollingMap.get(queueName);
						if (p != null && p) {
							try {
								if (debugLogOpen) {
									log.info("PullMessageTask_sleep:"
											+ Thread.currentThread().getName()
											+ " Have a nice sleep!");
								}
								polling = false;
								lockObj.wait();
							}
							catch (InterruptedException e) {
								if (debugLogOpen) {
									log.info("PullMessageTask_Interrupted!"
											+ Thread.currentThread().getName()
											+ " QueueName is " + queueName);
								}
								continue;
							}
						}
					}

					TokenForAlicom tokenObject = tokenGetter.getTokenByMessageType(
							messageType, queueName, mnsAccountEndpoint);
					CloudQueue queue = tokenObject.getQueue();
					Message popMsg = null;
					if (!polling) {
						popMsg = queue.popMessage();
						if (debugLogOpen) {
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							log.info("PullMessageTask_popMessage:"
									+ Thread.currentThread().getName() + "-popDone at "
									+ "," + format.format(new Date()) + " msgSize="
									+ (popMsg == null ? 0 : popMsg.getMessageId()));
						}
						if (popMsg == null) {
							polling = true;
							continue;
						}
					}
					else {
						if (setPolling(queueName)) {
							if (debugLogOpen) {
								log.info("PullMessageTask_setPolling:"
										+ Thread.currentThread().getName() + " Polling!");
							}
						}
						else {
							continue;
						}
						do {
							if (debugLogOpen) {
								log.info("PullMessageTask_Keep_Polling"
										+ Thread.currentThread().getName()
										+ "KEEP Polling!");
							}
							try {
								popMsg = queue.popMessage(sleepSecondWhenNoData);
							}
							catch (ClientException e) {
								if (debugLogOpen) {
									log.info(
											"PullMessageTask_Pop_Message:ClientException Refresh accessKey"
													+ e);
								}
								tokenObject = tokenGetter.getTokenByMessageType(
										messageType, queueName, mnsAccountEndpoint);
								queue = tokenObject.getQueue();

							}
							catch (ServiceException e) {
								if (debugLogOpen) {
									log.info(
											"PullMessageTask_Pop_Message:ServiceException Refresh accessKey"
													+ e);
								}
								tokenObject = tokenGetter.getTokenByMessageType(
										messageType, queueName, mnsAccountEndpoint);
								queue = tokenObject.getQueue();

							}
							catch (Exception e) {
								if (debugLogOpen) {
									log.info(
											"PullMessageTask_Pop_Message:Exception Happened when polling popMessage: "
													+ e);
								}
							}
						}
						while (popMsg == null && isRunning);
						clearPolling(queueName);
					}
					boolean dealResult = messageListener.dealMessage(popMsg);
					if (dealResult) {
						// remember to delete message when consume message successfully.
						if (debugLogOpen) {
							log.info("PullMessageTask_Deal_Message:"
									+ Thread.currentThread().getName() + "deleteMessage "
									+ popMsg.getMessageId());
						}
						queue.deleteMessage(popMsg.getReceiptHandle());
					}
				}
				catch (ClientException e) {
					log.error("PullMessageTask_execute_error,messageType:" + messageType
							+ ",queueName:" + queueName, e);
					break;

				}
				catch (ServiceException e) {
					if (e.getErrorCode().equals("AccessDenied")) {
						log.error("PullMessageTask_execute_error,messageType:"
								+ messageType + ",queueName:" + queueName
								+ ",please check messageType and queueName", e);
					}
					else {
						log.error("PullMessageTask_execute_error,messageType:"
								+ messageType + ",queueName:" + queueName, e);
					}
					break;

				}
				catch (com.aliyuncs.exceptions.ClientException e) {
					if (e.getErrCode().equals("InvalidAccessKeyId.NotFound")) {
						log.error("PullMessageTask_execute_error,messageType:"
								+ messageType + ",queueName:" + queueName
								+ ",please check AccessKeyId", e);
					}
					if (e.getErrCode().equals("SignatureDoesNotMatch")) {
						log.error("PullMessageTask_execute_error,messageType:"
								+ messageType + ",queueName:" + queueName
								+ ",please check AccessKeySecret", e);
					}
					else {
						log.error("PullMessageTask_execute_error,messageType:"
								+ messageType + ",queueName:" + queueName, e);
					}
					break;

				}
				catch (Exception e) {
					log.error("PullMessageTask_execute_error,messageType:" + messageType
							+ ",queueName:" + queueName, e);
					try {
						Thread.sleep(sleepSecondWhenNoData);
					}
					catch (InterruptedException e1) {
						log.error("PullMessageTask_execute_error,messageType:"
								+ messageType + ",queueName:" + queueName, e);
					}
				}
			}

		}

	}

}
