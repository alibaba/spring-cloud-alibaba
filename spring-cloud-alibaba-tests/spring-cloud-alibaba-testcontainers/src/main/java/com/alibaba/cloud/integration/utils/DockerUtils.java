/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.cloud.integration.utils;

import com.alibaba.cloud.integration.docker.ContainerExecException;
import com.alibaba.cloud.integration.docker.ContainerExecResult;
import com.alibaba.cloud.integration.docker.ContainerExecResultBytes;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DockerUtils {
		private static final Logger LOG = LoggerFactory.getLogger(DockerUtils.class);
		
		private static File getTargetDirectory(String containerId) {
				String base = System.getProperty("maven.buildDirectory");
				if (base == null) {
						base = "target";
				}
				File directory = new File(base + "/container-logs/" + containerId);
				if (!directory.exists() && !directory.mkdirs()) {
						LOG.error("Error creating directory for container logs.");
				}
				return directory;
		}
		
		public static void dumpContainerLogToTarget(DockerClient dockerClient,
				String containerId) {
				final String containerName = getContainerName(dockerClient, containerId);
				File output = getUniqueFileInTargetDirectory(containerName, "docker",
						".log");
				try (OutputStream os = new BufferedOutputStream(
						new FileOutputStream(output))) {
						CompletableFuture<Boolean> future = new CompletableFuture<>();
						dockerClient.logContainerCmd(containerName).withStdOut(true)
								.withStdErr(true).withTimestamps(true)
								.exec(new ResultCallback<Frame>() {
										@Override public void close() {
										}
										
										@Override public void onStart(
												Closeable closeable) {
										}
										
										@Override public void onNext(Frame object) {
												try {
														os.write(object.getPayload());
												}
												catch (IOException e) {
														onError(e);
												}
										}
										
										@Override public void onError(
												Throwable throwable) {
												future.completeExceptionally(throwable);
										}
										
										@Override public void onComplete() {
												future.complete(true);
										}
								});
						future.get();
				}
				catch (RuntimeException | ExecutionException | IOException e) {
						LOG.error("Error dumping log for {}", containerName, e);
				}
				catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						LOG.info("Interrupted dumping log from container {}",
								containerName, ie);
				}
		}
		
		private static File getUniqueFileInTargetDirectory(String containerName,
				String prefix, String suffix) {
				return getUniqueFileInDirectory(getTargetDirectory(containerName), prefix,
						suffix);
		}
		
		private static File getUniqueFileInDirectory(File directory, String prefix,
				String suffix) {
				File file = new File(directory, prefix + suffix);
				int i = 0;
				while (file.exists()) {
						LOG.info("{} exists, incrementing", file);
						file = new File(directory, prefix + "_" + (i++) + suffix);
				}
				return file;
		}
		
		private static String getContainerName(DockerClient dockerClient,
				String containerId) {
				final InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(
						containerId).exec();
				// docker api returns names prefixed with "/", it's part of it's legacy design,
				// this removes it to be consistent with what docker ps shows.
				return inspectContainerResponse.getName().replace("/", "");
		}
		
		public static void dumpContainerDirToTargetCompressed(DockerClient dockerClient,
				String containerId, String path) {
				final String containerName = getContainerName(dockerClient, containerId);
				final String baseName = path.replace("/", "-").replaceAll("^-", "");
				File output = getUniqueFileInTargetDirectory(containerName, baseName,
						".tar.gz");
				try (InputStream dockerStream = dockerClient.copyArchiveFromContainerCmd(
						containerId, path).exec();
						OutputStream os = new GZIPOutputStream(
								new BufferedOutputStream(new FileOutputStream(output)))) {
						IOUtils.copy(dockerStream, os);
				}
				catch (RuntimeException | IOException e) {
						if (!(e instanceof NotFoundException)) {
								LOG.error("Error reading dir from container {}",
										containerName, e);
						}
				}
		}
		
		public static void dumpContainerLogDirToTarget(DockerClient docker,
				String containerId, String path) {
				File targetDirectory = getTargetDirectory(containerId);
				try (InputStream dockerStream = docker.copyArchiveFromContainerCmd(
						containerId, path).exec();
						TarArchiveInputStream stream = new TarArchiveInputStream(
								dockerStream)) {
						TarArchiveEntry entry = stream.getNextTarEntry();
						while (entry != null) {
								if (entry.isFile()) {
										File output = new File(targetDirectory,
												entry.getName().replace("/", "-"));
										Files.copy(stream, output.toPath(),
												StandardCopyOption.REPLACE_EXISTING);
								}
								entry = stream.getNextTarEntry();
						}
				}
				catch (RuntimeException | IOException e) {
						LOG.error("Error reading logs from container {}", containerId, e);
				}
		}
		
		public static String getContainerIP(DockerClient docker, String containerId) {
				for (Map.Entry<String, ContainerNetwork> e : docker.inspectContainerCmd(
								containerId).exec().getNetworkSettings().getNetworks()
						.entrySet()) {
						return e.getValue().getIpAddress();
				}
				throw new IllegalArgumentException(
						"Container " + containerId + " has no networks");
		}
		
		public static ContainerExecResult runCommand(DockerClient docker,
				String containerId, String... cmd)
				throws ContainerExecException, ExecutionException, InterruptedException {
				try {
						return runCommandAsync(docker, containerId, cmd).get();
				}
				catch (ExecutionException e) {
						if (e.getCause() instanceof ContainerExecException) {
								throw (ContainerExecException) e.getCause();
						}
						throw e;
				}
		}
		
		public static ContainerExecResult runCommandAsUser(String userId,
				DockerClient docker, String containerId, String... cmd)
				throws ContainerExecException, ExecutionException, InterruptedException {
				try {
						return runCommandAsyncAsUser(userId, docker, containerId,
								cmd).get();
				}
				catch (ExecutionException e) {
						if (e.getCause() instanceof ContainerExecException) {
								throw (ContainerExecException) e.getCause();
						}
						throw e;
				}
		}
		
		public static CompletableFuture<ContainerExecResult> runCommandAsyncAsUser(
				String userId, DockerClient dockerClient, String containerId,
				String... cmd) {
				String execId = dockerClient.execCreateCmd(containerId).withCmd(cmd)
						.withAttachStderr(true).withAttachStdout(true).withUser(userId)
						.exec().getId();
				return runCommandAsync(execId, dockerClient, containerId, cmd);
		}
		
		public static CompletableFuture<ContainerExecResult> runCommandAsync(
				DockerClient dockerClient, String containerId, String... cmd) {
				String execId = dockerClient.execCreateCmd(containerId).withCmd(cmd)
						.withAttachStderr(true).withAttachStdout(true).exec().getId();
				return runCommandAsync(execId, dockerClient, containerId, cmd);
		}
		
		private static CompletableFuture<ContainerExecResult> runCommandAsync(
				String execId, DockerClient dockerClient, String containerId,
				String... cmd) {
				CompletableFuture<ContainerExecResult> future = new CompletableFuture<>();
				final String containerName = getContainerName(dockerClient, containerId);
				String cmdString = String.join(" ", cmd);
				StringBuilder stdout = new StringBuilder();
				StringBuilder stderr = new StringBuilder();
				dockerClient.execStartCmd(execId).withDetach(false)
						.exec(new ResultCallback<Frame>() {
								@Override public void close() {
								}
								
								@Override public void onStart(Closeable closeable) {
										LOG.info("DOCKER.exec({}:{}): Executing...",
												containerName, cmdString);
								}
								
								@Override public void onNext(Frame object) {
										LOG.info("DOCKER.exec({}:{}): {}", containerName,
												cmdString, object);
										if (StreamType.STDOUT == object.getStreamType()) {
												stdout.append(
														new String(object.getPayload(),
																UTF_8));
										}
										else if (StreamType.STDERR
												== object.getStreamType()) {
												stderr.append(
														new String(object.getPayload(),
																UTF_8));
										}
								}
								
								@Override public void onError(Throwable throwable) {
										future.completeExceptionally(throwable);
								}
								
								@Override public void onComplete() {
										LOG.info("DOCKER.exec({}:{}): Done",
												containerName, cmdString);
										
										InspectExecResponse resp = waitForExecCmdToFinish(
												dockerClient, execId);
										int retCode = resp.getExitCode();
										ContainerExecResult result = ContainerExecResult.of(
												retCode, stdout.toString(),
												stderr.toString());
										LOG.info("DOCKER.exec({}:{}): completed with {}",
												containerName, cmdString, retCode);
										
										if (retCode != 0) {
												LOG.error(
														"DOCKER.exec({}:{}): completed with non zero return code: {}\nstdout: {}\nstderr:"
																+ " {}", containerName,
														cmdString, result.getExitCode(),
														result.getStdout(),
														result.getStderr());
												future.completeExceptionally(
														new ContainerExecException(
																cmdString, containerId,
																result));
										}
										else {
												future.complete(result);
										}
								}
						});
				return future;
		}
		
		public static ContainerExecResultBytes runCommandWithRawOutput(
				DockerClient dockerClient, String containerId, String... cmd)
				throws ContainerExecException {
				CompletableFuture<Boolean> future = new CompletableFuture<>();
				String execId = dockerClient.execCreateCmd(containerId).withCmd(cmd)
						.withAttachStderr(true).withAttachStdout(true).exec().getId();
				final String containerName = getContainerName(dockerClient, containerId);
				String cmdString = String.join(" ", cmd);
				ByteArrayOutputStream stdout = new ByteArrayOutputStream();
				ByteArrayOutputStream stderr = new ByteArrayOutputStream();
				dockerClient.execStartCmd(execId).withDetach(false)
						.exec(new ResultCallback<Frame>() {
								@Override public void close() {
								}
								
								@Override public void onStart(Closeable closeable) {
										LOG.info("DOCKER.exec({}:{}): Executing...",
												containerName, cmdString);
								}
								
								@Override public void onNext(Frame object) {
										try {
												if (StreamType.STDOUT
														== object.getStreamType()) {
														stdout.write(object.getPayload());
												}
												else if (StreamType.STDERR
														== object.getStreamType()) {
														stderr.write(object.getPayload());
												}
										}
										catch (IOException e) {
												throw new UncheckedIOException(e);
										}
								}
								
								@Override public void onError(Throwable throwable) {
										future.completeExceptionally(throwable);
								}
								
								@Override public void onComplete() {
										LOG.info("DOCKER.exec({}:{}): Done",
												containerName, cmdString);
										future.complete(true);
								}
						});
				future.join();
				
				InspectExecResponse resp = waitForExecCmdToFinish(dockerClient, execId);
				int retCode = resp.getExitCode();
				
				ContainerExecResultBytes result = ContainerExecResultBytes.of(retCode,
						stdout.toByteArray(), stderr.toByteArray());
				LOG.info("DOCKER.exec({}:{}): completed with {}", containerName,
						cmdString, retCode);
				
				if (retCode != 0) {
						throw new ContainerExecException(cmdString, containerId, null);
				}
				return result;
		}
		
		public static CompletableFuture<Integer> runCommandAsyncWithLogging(
				DockerClient dockerClient, String containerId, String... cmd) {
				CompletableFuture<Integer> future = new CompletableFuture<>();
				String execId = dockerClient.execCreateCmd(containerId).withCmd(cmd)
						.withAttachStderr(true).withAttachStdout(true).exec().getId();
				final String containerName = getContainerName(dockerClient, containerId);
				String cmdString = String.join(" ", cmd);
				dockerClient.execStartCmd(execId).withDetach(false)
						.exec(new ResultCallback<Frame>() {
								@Override public void close() {
								}
								
								@Override public void onStart(Closeable closeable) {
										LOG.info("DOCKER.exec({}:{}): Executing...",
												containerName, cmdString);
								}
								
								@Override public void onNext(Frame object) {
										LOG.info("DOCKER.exec({}:{}): {}", containerName,
												cmdString, object);
								}
								
								@Override public void onError(Throwable throwable) {
										future.completeExceptionally(throwable);
								}
								
								@Override public void onComplete() {
										LOG.info("DOCKER.exec({}:{}): Done",
												containerName, cmdString);
										InspectExecResponse resp = waitForExecCmdToFinish(
												dockerClient, execId);
										int retCode = resp.getExitCode();
										LOG.info("DOCKER.exec({}:{}): completed with {}",
												containerName, cmdString, retCode);
										future.complete(retCode);
								}
						});
				return future;
		}
		
		private static InspectExecResponse waitForExecCmdToFinish(
				DockerClient dockerClient, String execId) {
				InspectExecResponse resp = dockerClient.inspectExecCmd(execId).exec();
				while (resp.isRunning()) {
						try {
								Thread.sleep(200);
						}
						catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
								throw new RuntimeException(ie);
						}
						resp = dockerClient.inspectExecCmd(execId).exec();
				}
				return resp;
		}
		
		public static Optional<String> getContainerCluster(DockerClient docker,
				String containerId) {
				return Optional.ofNullable(
						docker.inspectContainerCmd(containerId).exec().getConfig()
								.getLabels().get("cluster"));
		}
}
