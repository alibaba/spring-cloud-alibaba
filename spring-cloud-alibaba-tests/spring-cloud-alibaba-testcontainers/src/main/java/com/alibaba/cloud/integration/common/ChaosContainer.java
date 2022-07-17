package com.alibaba.cloud.integration.common;

import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.alibaba.cloud.integration.docker.ContainerExecResult;
import com.alibaba.cloud.integration.utils.DockerUtils;
import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

@Slf4j
public class ChaosContainer<SelfT extends ChaosContainer<SelfT>>
		extends GenericContainer<SelfT> {

	protected final String clusterName;

	protected ChaosContainer(String clusterName, String image) {
		super(image);
		this.clusterName = clusterName;
	}
	
	/**
	 * Initialize container startup parameters
	 */
	@Override
	protected void configure() {
		super.configure();
		addEnv("MALLOC_ARENA_MAX", "1");
	}
	
	/**
	 * Do some preprocessing before the container stops
	 */
	protected void beforeStop() {
		if (null == getContainerId()) {
			return;
		}

		// dump the container log
		DockerUtils.dumpContainerLogToTarget(getDockerClient(), getContainerId());
	}
	
	/**
	 * container stopped
	 */
	@Override
	public void stop() {
		beforeStop();
		super.stop();
	}

	protected void tailContainerLog() {
		withLogConsumer(item -> log.info(item.getUtf8String()));
	}

	public void putFile(String path, byte[] contents) throws Exception {
		String base64contents = Base64.getEncoder().encodeToString(contents);
		String cmd = String.format("echo %s | base64 -d > %s", base64contents, path);
		execCmd("bash", "-c", cmd);
	}

	public ContainerExecResult execCmd(String... commands) throws Exception {
		DockerClient client = this.getDockerClient();
		String dockerId = this.getContainerId();
		return DockerUtils.runCommand(client, dockerId, commands);
	}

	public CompletableFuture<ContainerExecResult> execCmdAsync(String... commands)
			throws Exception {
		DockerClient client = this.getDockerClient();
		String dockerId = this.getContainerId();
		return DockerUtils.runCommandAsync(client, dockerId, commands);
	}

	public ContainerExecResult execCmdAsUser(String userId, String... commands)
			throws Exception {
		DockerClient client = this.getDockerClient();
		String dockerId = this.getContainerId();
		return DockerUtils.runCommandAsUser(userId, client, dockerId, commands);
	}

	public CompletableFuture<ContainerExecResult> execCmdAsyncAsUser(String userId,
			String... commands) throws Exception {
		DockerClient client = this.getDockerClient();
		String dockerId = this.getContainerId();
		return DockerUtils.runCommandAsyncAsUser(userId, client, dockerId, commands);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ChaosContainer)) {
			return false;
		}

		ChaosContainer another = (ChaosContainer) o;
		return clusterName.equals(another.clusterName) && super.equals(another);
	}

	@Override
	public int hashCode() {
		return 31 * super.hashCode() + Objects.hash(clusterName);
	}

}
