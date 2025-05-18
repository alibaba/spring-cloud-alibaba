package com.anmory.springcloudalibabainitialzr.controller;

import com.anmory.springcloudalibabainitialzr.model.ProjectConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-18 下午10:48
 */

@RestController
public class ProjectConfigController {

    @RequestMapping("/generate")
    public ResponseEntity<Resource> generate(@RequestBody ProjectConfig projectConfig) throws IOException {
        // 1. 生成文件内容
        String pomContent = generatePom(projectConfig);
        String mainClass = generateMainClass(projectConfig);
        String testClass = generateTestClass(projectConfig);

        // 2. 将文件打包成 ZIP
        ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zip)) {
            // 添加 pom.xml 文件
            ZipEntry pomEntry = new ZipEntry(projectConfig.getProjectName() + "/pom.xml");
            zos.putNextEntry(pomEntry);
            zos.write(pomContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 添加主类文件
            String mainClassPath = projectConfig.getProjectName() + "/src/main/java/" +
                    projectConfig.getGroupId().replace('.', '/') + "/" + projectConfig.getArtifactId() +
                    '/' + projectConfig.getProjectName() + "Application.java";
            ZipEntry mainClassEntry = new ZipEntry(mainClassPath);
            zos.putNextEntry(mainClassEntry);
            zos.write(mainClass.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 添加测试类文件
            String testClassPath = projectConfig.getProjectName() + "/src/test/java/" +
                    projectConfig.getGroupId().replace('.', '/') + "/" + projectConfig.getArtifactId() +
                    '/' + projectConfig.getProjectName() + "ApplicationTests.java";
            ZipEntry testClassEntry = new ZipEntry(testClassPath);
            zos.putNextEntry(testClassEntry);
            zos.write(testClass.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        // 3. 创建 ByteArrayResource 用于返回 ZIP 文件
        ByteArrayResource resource = new ByteArrayResource(zip.toByteArray());

        // 4. 设置响应头，触发浏览器下载
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + projectConfig.getProjectName() + ".zip");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zip.size())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private String generateMainClass(ProjectConfig projectConfig) {
        return """
                package %s;
                
                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;
                
                @SpringBootApplication
                public class %sApplication {
                
                    public static void main(String[] args) {
                        SpringApplication.run(%sApplication.class, args);
                    }
                
                }
                """.formatted(projectConfig.getPackageName(),
                projectConfig.getProjectName(),
                projectConfig.getProjectName());
    }

    private String generateTestClass(ProjectConfig projectConfig) {
        return """
                package %s;
                
                import org.junit.jupiter.api.Test;
                import org.springframework.boot.test.context.SpringBootTest;
                
                @SpringBootTest
                class %sApplicationTests {
                
                    @Test
                    void contextLoads() {
                        // Test method to verify Spring application context loads successfully
                    }
                
                }
                """.formatted(projectConfig.getPackageName(),
                projectConfig.getProjectName());
    }

    private String generatePom(ProjectConfig projectConfig) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                
                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <packaging>%s</packaging>
                
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>%s</version>
                        <relativePath/> <!-- lookup parent from repository -->
                    </parent>
                    <properties>
                        <maven.compiler.source>%s</maven.compiler.source>
                        <maven.compiler.target>%s</maven.compiler.target>
                        <java.version>%s</java.version>
                        <spring-cloud.version>%s</spring-cloud.version>
                        <spring-cloud-alibaba.version>%s</spring-cloud-alibaba.version>
                    </properties>
                
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>com.alibaba.cloud</groupId>
                                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                                <version>${spring-cloud-alibaba.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                            <dependency>
                                <groupId>org.springframework.cloud</groupId>
                                <artifactId>spring-cloud-dependencies</artifactId>
                                <version>${spring-cloud.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
                """.formatted(projectConfig.getGroupId(),
                projectConfig.getArtifactId(),
                projectConfig.getPackaging(),
                projectConfig.getSpringBootVersion(),
                projectConfig.getJavaVersion(),
                projectConfig.getJavaVersion(),
                projectConfig.getJavaVersion(),
                projectConfig.getSpringCloudVersion(),
                projectConfig.getSpringCloudAlibabaVersion());
    }
}
