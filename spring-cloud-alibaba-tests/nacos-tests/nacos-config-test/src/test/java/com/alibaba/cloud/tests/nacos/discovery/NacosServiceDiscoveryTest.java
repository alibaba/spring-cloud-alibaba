package com.alibaba.cloud.tests.nacos.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 6 * TIME_OUT)
@SpringBootTest(classes = NacosDiscoveryPropertiesServerAddressBothLevelTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=app",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.discovery.group=DEFAULT_GROUP",
		"spring.cloud.nacos.server-addr=127.0.0.1:8848" })
public class NacosServiceDiscoveryTest {

	private static final String serviceName = "service-test";

	@Autowired
	private NacosConfigProperties properties;
	@Autowired
	private NacosDiscoveryProperties discoveryProperties;

	private NacosConfigManager nacosConfigManager;

	@Mock
	private NacosServiceInstance serviceInstance;

	@BeforeAll
	public static void setUp() {

	}

	public static Instance serviceInstance(String serviceName, boolean isHealthy,
			String host, int port, Map<String, String> metadata) {
		Instance instance = new Instance();
		instance.setIp(host);
		instance.setPort(port);
		instance.setServiceName(serviceName);
		instance.setHealthy(isHealthy);
		instance.setMetadata(metadata);
		return instance;
	}

	private static Map<String, String> buildMetadata() {
		HashMap<String, String> map = new HashMap<>();
		map.put("test-key", "test-value");
		map.put("secure", "true");
		return map;
	}

	@BeforeEach
	public void prepare() {
		nacosConfigManager = new NacosConfigManager(properties);
	}

	@Test
	public void testGetInstances() throws NacosException {
		ArrayList<Instance> instances = new ArrayList<>();

		Instance instance = serviceInstance(serviceName, true, "127.0.0.1", 8888,
				buildMetadata());

		NacosServiceManager nacosServiceManager = new NacosServiceManager();
		nacosServiceManager.setNacosDiscoveryProperties(discoveryProperties);

		NamingService namingService = NamingFactory
				.createNamingService(properties.getServerAddr());

		namingService.registerInstance(serviceName, "DEFAULT_GROUP", instance);

		NacosServiceDiscovery serviceDiscovery = new NacosServiceDiscovery(
				discoveryProperties, nacosServiceManager);

		List<ServiceInstance> serviceInstances = serviceDiscovery
				.getInstances(serviceName);

		assertThat(serviceInstances.size()).isEqualTo(1);

		ServiceInstance serviceInstance = serviceInstances.get(0);

		assertThat(serviceInstance.getServiceId()).isEqualTo(serviceName);
		assertThat(serviceInstance.getHost()).isEqualTo("127.0.0.1");
		assertThat(serviceInstance.getPort()).isEqualTo(8888);
		assertThat(serviceInstance.isSecure()).isEqualTo(true);
		assertThat(serviceInstance.getUri().toString())
				.isEqualTo(getUri(serviceInstance));
		assertThat(serviceInstance.getMetadata().get("test-key")).isEqualTo("test-value");
	}

	@Test
	public void testGetServices() throws NacosException {
		ListView<String> nacosServices = new ListView<>();

		nacosServices.setData(new LinkedList<>());

		nacosServices.getData().add(serviceName + "3");
		nacosServices.getData().add(serviceName + "2");
		nacosServices.getData().add(serviceName + "1");
		nacosServices.setCount(3);

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);
		NacosServiceManager nacosServiceManager = new NacosServiceManager();
		nacosServiceManager.setNacosDiscoveryProperties(discoveryProperties);

		NamingService namingService = NamingFactory
				.createNamingService(properties.getServerAddr());

		Instance instance1 = serviceInstance(serviceName, true, "127.0.0.1", 8888,
				buildMetadata());
		Instance instance2 = serviceInstance(serviceName, true, "127.0.0.1", 8889,
				buildMetadata());
		Instance instance3 = serviceInstance(serviceName, true, "127.0.0.1", 8890,
				buildMetadata());

		namingService.registerInstance(serviceName + "1", "DEFAULT_GROUP", instance1);
		namingService.registerInstance(serviceName + "2", "DEFAULT_GROUP", instance2);
		namingService.registerInstance(serviceName + "3", "DEFAULT_GROUP", instance3);

		ListView<String> atucal = namingService.getServicesOfServer(1, Integer.MAX_VALUE,
				"DEFAULT_GROUP");
		Assertions.assertEquals(atucal.getData().toString(),
				nacosServices.getData().toString());

		NacosServiceDiscovery serviceDiscovery = new NacosServiceDiscovery(
				nacosDiscoveryProperties, nacosServiceManager);

		List<String> services = serviceDiscovery.getServices();

		assertThat(services.size()).isEqualTo(3);
		assertThat(services.contains(serviceName + "1"));
		assertThat(services.contains(serviceName + "2"));
		assertThat(services.contains(serviceName + "3"));
	}

	private String getUri(ServiceInstance instance) {

		if (instance.isSecure()) {
			return "https://127.0.0.1:8888";
		}

		return "http://127.0.0.1:8888";
	}

}
