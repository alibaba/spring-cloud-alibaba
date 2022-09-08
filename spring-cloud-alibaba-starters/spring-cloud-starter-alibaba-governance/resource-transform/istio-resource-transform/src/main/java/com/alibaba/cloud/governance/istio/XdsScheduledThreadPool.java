package com.alibaba.cloud.governance.istio;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class XdsScheduledThreadPool extends ScheduledThreadPoolExecutor {

	public XdsScheduledThreadPool(XdsConfigProperties xdsConfigProperties) {
		this(xdsConfigProperties.getPollingPoolSize());
	}

	public XdsScheduledThreadPool(int corePoolSize) {
		super(corePoolSize);
	}

}
