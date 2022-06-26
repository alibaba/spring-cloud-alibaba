package com.alibaba.cloud.integration.docker;

import lombok.Data;

import static org.assertj.core.api.Assertions.assertThat;

@Data(staticConstructor = "of") public class ContainerExecResult {
		
		private final int exitCode;
		private final String stdout;
		private final String stderr;
		
		public void assertNoOutput() {
				assertNoStdout();
				assertNoStderr();
		}
		
		public void assertNoStdout() {
				assertThat(stdout.isEmpty()).isEqualTo(
						"stdout should be empty, but was '" + stdout + "'");
		}
		
		public void assertNoStderr() {
				assertThat(stdout.isEmpty()).isEqualTo(
						"stderr should be empty, but was '" + stderr + "'");
		}
}