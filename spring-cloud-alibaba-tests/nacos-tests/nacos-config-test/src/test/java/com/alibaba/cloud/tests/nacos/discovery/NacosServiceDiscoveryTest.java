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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 4 * TIME_OUT)
@SpringBootTest(classes = NacosDiscoveryPropertiesServerAddressBothLevelTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=app",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.server-addr=127.0.0.1:8848" })
public class NacosServiceDiscoveryTest {

	private static final String serviceName = "DEFAULT";
	private final String host = "127.0.0.1";
	private final int port = 8888;
	@Autowired
	private NacosConfigProperties properties;
	private NacosConfigManager nacosConfigManager;
	@Mock
	private NacosServiceDiscovery serviceDiscovery;
	@Mock
	private NacosServiceInstance serviceInstance;

	@BeforeAll
	public static void setUp() {

	}

	@BeforeEach
	public void prepare() {
		nacosConfigManager = new NacosConfigManager(properties);
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

	@Test
	public void testGetInstances() throws NacosException {
		ArrayList<Instance> instances = new ArrayList<>();

		HashMap<String, String> map = new HashMap<>();
		map.put("test-key", "test-value");
		map.put("secure", "true");

		when(serviceDiscovery.getInstances(serviceName))
				.thenReturn(singletonList(serviceInstance));

		Instance instance = serviceInstance(serviceName, true, host, port, map);

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);
		NacosServiceManager nacosServiceManager = mock(NacosServiceManager.class);

		NamingService namingService = NamingFactory
				.createNamingService(properties.getServerAddr());

		namingService.registerInstance(serviceName, instance);

		when(nacosServiceManager.getNamingService()).thenReturn(namingService);
		when(nacosDiscoveryProperties.getGroup()).thenReturn("DEFAULT");
		when(namingService.selectInstances(eq(serviceName), eq("DEFAULT"), eq(true)))
				.thenReturn(instances);

		NacosServiceDiscovery serviceDiscovery = new NacosServiceDiscovery(
				nacosDiscoveryProperties, nacosServiceManager);

		List<ServiceInstance> serviceInstances = serviceDiscovery
				.getInstances(serviceName);

		assertThat(serviceInstances.size()).isEqualTo(1);

		ServiceInstance serviceInstance = serviceInstances.get(0);

		assertThat(serviceInstance.getServiceId()).isEqualTo(serviceName);
		assertThat(serviceInstance.getHost()).isEqualTo(host);
		assertThat(serviceInstance.getPort()).isEqualTo(port);
		assertThat(serviceInstance.isSecure()).isEqualTo(true);
		assertThat(serviceInstance.getUri().toString())
				.isEqualTo(getUri(serviceInstance));
		assertThat(serviceInstance.getMetadata().get("test-key")).isEqualTo("test-value");
	}

	@Test
	public void testGetServices() throws NacosException {
		ListView<String> nacosServices = new ListView<>();

		nacosServices.setData(new LinkedList<>());

		nacosServices.getData().add(serviceName + "1");
		nacosServices.getData().add(serviceName + "2");
		nacosServices.getData().add(serviceName + "3");

		NacosDiscoveryProperties nacosDiscoveryProperties = mock(
				NacosDiscoveryProperties.class);
		NacosServiceManager nacosServiceManager = mock(NacosServiceManager.class);

		NamingService namingService = mock(NamingService.class);

		when(nacosServiceManager.getNamingService()).thenReturn(namingService);
		when(nacosDiscoveryProperties.getGroup()).thenReturn("DEFAULT");
		when(namingService.getServicesOfServer(eq(1), eq(Integer.MAX_VALUE),
				eq("DEFAULT"))).thenReturn(nacosServices);

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
			return "https://" + host + ":" + port;
		}

		return "http://" + host + ":" + port;
	}

}
