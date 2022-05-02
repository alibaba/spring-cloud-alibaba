package com.alibaba.cloud.integration.docker;

import lombok.Data;

@Data(staticConstructor = "of")
public class ContainerExecResultBytes {

	private final int exitCode;
	private final byte[] stdout;
	private final byte[] stderr;

}
