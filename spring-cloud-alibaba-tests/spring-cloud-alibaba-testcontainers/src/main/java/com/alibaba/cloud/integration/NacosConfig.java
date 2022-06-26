package com.alibaba.cloud.integration;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter public class NacosConfig {
		
		private String dataId;
		
		private String namespace;
		
		private String group;
		
		private String type;
		
}
