package com.alibaba.cloud.integration.test.rocketmq;

import com.alibaba.cloud.integration.common.ChaosContainer;
import com.alibaba.cloud.integration.utils.DockerUtils;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j public abstract class RocketmqContainer<SelfT extends RocketmqContainer<SelfT>>
		extends ChaosContainer<SelfT> {
		
		public static final int INVALID_PORT = -1;
		public static final int ZK_PORT = 2181;
		public static final int CS_PORT = 2184;
		public static final int BOOKIE_PORT = 3181;
		public static final int BROKER_PORT = 6650;
		public static final int BROKER_HTTP_PORT = 8080;
		
		public static final String DEFAULT_IMAGE_NAME = System.getenv()
				.getOrDefault("TEST_IMAGE_NAME", "");
		
		/**
		 * For debugging purposes, it is useful to have the ability to leave containers running.
		 * This mode can be activated by setting environment variables
		 * CONTAINERS_LEAVE_RUNNING=true and TESTCONTAINERS_REUSE_ENABLE=true
		 * After debugging, one can use this command to kill all containers that were left running:
		 * docker kill $(docker ps -q --filter "label=markcontainer=true")
		 */
		public static final boolean CONTAINERS_LEAVE_RUNNING = Boolean.parseBoolean(
				System.getenv("CONTAINERS_LEAVE_RUNNING"));
		
		private final String hostname;
		private final String serviceName;
		private final String serviceEntryPoint;
		private final int servicePort;
		private final int httpPort;
		private final String httpPath;
		
		public RocketmqContainer(String clusterName, String hostname, String serviceName,
				String serviceEntryPoint, int servicePort, int httpPort) {
				this(clusterName, hostname, serviceName, serviceEntryPoint, servicePort,
						httpPort, "/metrics");
		}
		
		public RocketmqContainer(String clusterName, String hostname, String serviceName,
				String serviceEntryPoint, int servicePort, int httpPort,
				String httpPath) {
				this(clusterName, hostname, serviceName, serviceEntryPoint, servicePort,
						httpPort, httpPath, DEFAULT_IMAGE_NAME);
		}
		
		public RocketmqContainer(String clusterName, String hostname, String serviceName,
				String serviceEntryPoint, int servicePort, int httpPort, String httpPath,
				String pulsarImageName) {
				super(clusterName, pulsarImageName);
				this.hostname = hostname;
				this.serviceName = serviceName;
				this.serviceEntryPoint = serviceEntryPoint;
				this.servicePort = servicePort;
				this.httpPort = httpPort;
				this.httpPath = httpPath;
				
				configureLeaveContainerRunning(this);
		}
		
		public static void configureLeaveContainerRunning(GenericContainer<?> container) {
				if (CONTAINERS_LEAVE_RUNNING) {
						// use Testcontainers reuse containers feature to leave the container running
						container.withReuse(true);
						// add label that can be used to find containers that are left running.
						container.withLabel("markcontainer", "true");
						// add a random label to prevent reuse of containers
						container.withLabel("markcontainer.random",
								UUID.randomUUID().toString());
				}
		}
		
		@Override protected void beforeStop() {
				super.beforeStop();
				if (null != getContainerId()) {
						DockerUtils.dumpContainerDirToTargetCompressed(getDockerClient(),
								getContainerId(), "/var/log/pulsar");
				}
		}
		
		@Override public void stop() {
				if (CONTAINERS_LEAVE_RUNNING) {
						log.warn("Ignoring stop due to CONTAINERS_LEAVE_RUNNING=true.");
						return;
				}
				super.stop();
		}
		
		@Override public String getContainerName() {
				return clusterName + "-" + hostname;
		}
		
		@Override protected void configure() {
				super.configure();
				if (httpPort > 0) {
						addExposedPorts(httpPort);
				}
				if (servicePort > 0) {
						addExposedPort(servicePort);
				}
		}
		
		protected void beforeStart() {
		}
		
		protected void afterStart() {
		}
		
		@Override public void start() {
				if (httpPort > 0 && servicePort < 0) {
						this.waitStrategy = new HttpWaitStrategy().forPort(httpPort)
								.forStatusCode(200).forPath(httpPath)
								.withStartupTimeout(Duration.of(300, SECONDS));
				}
				else if (httpPort > 0 || servicePort > 0) {
						this.waitStrategy = new HostPortWaitStrategy().withStartupTimeout(
								Duration.of(300, SECONDS));
				}
				this.withCreateContainerCmdModifier(createContainerCmd -> {
						createContainerCmd.withHostName(hostname);
						createContainerCmd.withName(getContainerName());
						createContainerCmd.withEntrypoint(serviceEntryPoint);
				});
				
				beforeStart();
				super.start();
				afterStart();
				log.info("[{}] Start pulsar service {} at container {}",
						getContainerName(), serviceName, getContainerId());
		}
		
		@Override public boolean equals(Object o) {
				if (!(o instanceof RocketmqContainer)) {
						return false;
				}
				
				RocketmqContainer another = (RocketmqContainer) o;
				return getContainerId().equals(another.getContainerId()) && super.equals(
						another);
		}
		
		@Override public int hashCode() {
				return 31 * super.hashCode() + Objects.hash(getContainerId());
		}
}
