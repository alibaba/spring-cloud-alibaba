package com.alibaba.cloud.integration.docker;

import lombok.Data;

@Data(staticConstructor = "of")
public class ContainerExecResultBytes {

    /** exception exit code.**/
    private final int exitCode;
    /**container log stdout. **/
    private final byte[] stdout;
    /**container log stderr output. **/
    private final byte[] stderr;
}
