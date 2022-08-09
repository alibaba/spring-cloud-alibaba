package com.alibaba.cloud.integration.docker;

import lombok.Data;

@Data(staticConstructor = "of")
public class ContainerExecResult {

    /** exception exit code.**/
    private final int exitCode;
    /**container log stdout. **/
    private final String stdout;
    /**container log stderr output. **/
    private final String stderr;
}
