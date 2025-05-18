package com.anmory.springcloudalibabainitialzr.model;

import lombok.Data;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-18 下午10:47
 */

@Data
public class ProjectConfig {
    private String buildTool;
    private String language;
    private String springBootVersion;
    private String springCloudVersion;
    private String springCloudAlibabaVersion;
    private String groupId;
    private String artifactId;
    private String projectName;
    private String description;
    private String packageName;
    private String packaging;
    private String javaVersion;
}
