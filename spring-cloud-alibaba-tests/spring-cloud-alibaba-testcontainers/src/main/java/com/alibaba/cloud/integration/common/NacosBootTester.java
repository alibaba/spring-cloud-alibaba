package com.alibaba.cloud.integration.common;

import com.alibaba.cloud.integration.NacosConfig;
import com.alibaba.cloud.integration.UserProperties;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

public abstract class NacosBootTester<ServiceContainerT extends GenericContainer> {

	private Logger logger = LoggerFactory.getLogger(NacosBootTester.class);

	public void vaildateUpdateState(NacosConfig nacosConfig, NacosConfigProperties properties, UserProperties userProperties) throws NacosException {

		ConfigService configService = NacosConfigManager.createConfigService(properties);
		String content = uploadFile(userProperties);
		try{
			configService.publishConfig(nacosConfig.getDataId(),nacosConfig.getGroup(),content,nacosConfig.getType());
		}finally {
			logger.info("nacos 配置文件已经上传完成");
		}
	}

	/**
	 * nacos update yaml
	 */
	protected abstract String uploadFile(UserProperties userProperties);

}
