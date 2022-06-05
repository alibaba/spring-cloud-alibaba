package com.alibaba.cloud.integration.test;

import com.alibaba.cloud.integration.NacosConfig;
import com.alibaba.cloud.integration.UserProperties;
import com.alibaba.cloud.integration.common.NacosBootTester;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.alibaba.cloud.integration.common.nacos.Const.DEFAULT_IMAGE_NAME;

@Slf4j
public class NacosContainerTest extends NacosBootTester {

	private NacosContainer nacosContainer;
	private static final String image = "freemanlau/nacos:1.4.2";

	@Override
	public void vaildateUpdateState(NacosConfig nacosConfig, NacosConfigProperties properties, UserProperties userProperties) throws NacosException {
		super.vaildateUpdateState(nacosConfig, properties, userProperties);
	}

	@Override
	protected String uploadFile(UserProperties userProperties) {

		try{


				String content =
					"configdata:\n" +
							"  user:\n" +
							"    age: 22\n" +
							"    name: freeman1123\n" +
							"    map:\n" +
							"      hobbies:\n" +
							"        - art\n" +
							"        - programming\n" +
							"        - movie\n" +
							"      intro: Hello, I'm freeman\n" +
							"      extra: yo~\n" +
							"    users:\n" +
							"      - name: dad\n" +
							"        age: 20\n" +
							"      - name: mom\n" +
							"        age: 18";
			return content;
		}catch (Exception ex){
			log.error("Nacos pulish failed");
			return null;
		}
	}



	@Before
	public  void setUp() throws Exception{
		nacosContainer = new NacosContainer(DEFAULT_IMAGE_NAME,image);
		nacosContainer.start();
	}

	@After
	public void cleanup() throws Exception{

	}

	@Test
	public void testNacosStartUp() throws Exception{
		NacosConfigProperties nacosConfigProperties = new NacosConfigProperties();
		UserProperties userProperties = new UserProperties();
		NacosConfig nacosConfig = NacosConfig.builder().
				build();
		vaildateUpdateState(nacosConfig,nacosConfigProperties,userProperties);
	}


}
