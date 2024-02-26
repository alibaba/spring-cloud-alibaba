# Spring Cloud Alibaba

[![CircleCI](https://circleci.com/gh/alibaba/spring-cloud-alibaba/tree/2022.x.svg?style=svg)](https://circleci.com/gh/alibaba/spring-cloud-alibaba/tree/2022.x)
[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba.cloud/spring-cloud-alibaba-dependencies.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.alibaba.cloud%20AND%20a:spring-cloud-alibaba-dependencies)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![actions](https://github.com/alibaba/spring-cloud-alibaba/workflows/Integration%20Testing/badge.svg)](https://github.com/alibaba/spring-cloud-alibaba/actions)
[![Leaderboard](https://img.shields.io/badge/SCA-Check%20Your%20Contribution-orange)](https://opensource.alibaba.com/contribution_leaderboard/details?projectValue=sca)

A project maintained by Alibaba.

See the [中文文档](https://github.com/alibaba/spring-cloud-alibaba/blob/2022.x/README-zh.md) for Chinese readme.

Spring Cloud Alibaba provides a one-stop solution for distributed application development. It contains all the components required to develop distributed applications, making it easy for you to develop your applications using Spring Cloud.

With Spring Cloud Alibaba, you only need to add some annotations and a small amount of configurations to connect Spring Cloud applications to the distributed solutions of Alibaba, and build a distributed application system with Alibaba middleware.


## Features

* **Flow control and service degradation**: Flow control for HTTP services is supported by default. You can also customize flow control and service degradation rules using annotations. The rules can be changed dynamically.
* **Service registration and discovery**: Service can be registered and clients can discover the instances using Spring-managed beans. Load balancing is consistent with that supported by the corresponding Spring Cloud.
* **Distributed configuration**: Support for externalized configuration in a distributed system, auto refresh when configuration changes.
* **Event-driven**: Support for building highly scalable event-driven microservices connected with shared messaging systems.
* **Distributed Transaction**: Support for distributed transaction solution with high performance and ease of use.
* **Alibaba Cloud Object Storage**: Massive, secure, low-cost, and highly reliable cloud storage services. Support for storing and accessing any type of data in any application, anytime, anywhere.
* **Alibaba Cloud SchedulerX**: Accurate, highly reliable, and highly available scheduled job scheduling services with response time within seconds.
* **Alibaba Cloud SMS**: A messaging service that covers the globe, Alibaba SMS provides convenient, efficient, and intelligent communication capabilities that help businesses quickly contact their customers.

For more features, please refer to [Roadmap](https://github.com/alibaba/spring-cloud-alibaba/blob/2022.x/Roadmap.md).

In addition to the above-mentioned features, for the needs of enterprise users' scenarios, [Microservices Engine (MSE)](https://www.aliyun.com/product/aliware/mse?spm=github.spring.com.topbar) of Spring Cloud Alibaba's enterprise version provides an enterprise-level microservices governance center, which includes more powerful governance capabilities such as Grayscale Release, Service Warm-up, Lossless Online and Offline and Outlier Ejection. At the same time, it also provides a variety of products and solutions such as enterprise-level Nacos registration / configuration center, enterprise-level cloud native gateway.


## Components

**[Sentinel](https://github.com/alibaba/Sentinel)**: Sentinel takes "traffic flow" as the breakthrough point, and provides solutions in areas such as flow control, concurrency, circuit breaking, and load protection to protect service stability.

**[Nacos](https://github.com/alibaba/Nacos)**: An easy-to-use dynamic service discovery, configuration and service management platform for building cloud native applications.

**[RocketMQ](https://rocketmq.apache.org/)**: A distributed messaging and streaming platform with low latency, high performance and reliability, trillion-level capacity and flexible scalability.

**[Seata](https://github.com/apache/incubator-seata)**: A distributed transaction solution with high performance and ease of use for microservices architecture.

**[Alibaba Cloud OSS](https://www.aliyun.com/product/oss)**: An encrypted and secure cloud storage service which stores, processes and accesses massive amounts of data from anywhere in the world.

**[Alibaba Cloud SMS](https://www.aliyun.com/product/sms)**: A messaging service that covers the globe, Alibaba SMS provides convenient, efficient, and intelligent communication capabilities that help businesses quickly contact their customers.

**[Alibaba Cloud SchedulerX](https://www.aliyun.com/aliware/schedulerx?spm=5176.10695662.784137.1.4b07363dej23L3)**: Accurate, highly reliable, and highly available scheduled job scheduling services with response time within seconds.

For more features please refer to [Roadmap](https://github.com/alibaba/spring-cloud-alibaba/blob/2022.x/Roadmap.md).

## How to build
* **2022.x branch**: Corresponds to Spring Cloud 2022 & Spring Boot 3.0.x, JDK 17 or later versions are supported.
* **2021.x branch**: Corresponds to Spring Cloud 2021 & Spring Boot 2.6.x. JDK 1.8 or later versions are supported.
* **2020.0 branch**: Corresponds to Spring Cloud 2020 & Spring Boot 2.4.x. JDK 1.8 or later versions are supported.
* **2.2.x branch**: Corresponds to Spring Cloud Hoxton & Spring Boot 2.2.x. JDK 1.8 or later versions are supported.
* **greenwich branch**: Corresponds to Spring Cloud Greenwich & Spring Boot 2.1.x. JDK 1.8 or later versions are supported.
* **finchley branch**: Corresponds to Spring Cloud Finchley & Spring Boot 2.0.x. JDK 1.8 or later versions are supported.
* **1.x branch**: Corresponds to Spring Cloud Edgware & Spring Boot 1.x, JDK 1.7 or later versions are supported.

Spring Cloud uses Maven for most build-related activities, and you should be able to get off the ground quite quickly by cloning the project you are interested in and typing:
```bash
./mvnw install
```

## How to Use

### Add maven dependency 

These artifacts are available from Maven Central and Spring Release repository via BOM:
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>2022.0.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
add the module in  `dependencies`. If you want to choose an older version, you can refer to the [Release Notes](https://sca.aliyun.com/zh-cn/docs/next/overview/version-explain).

## Examples

A `spring-cloud-alibaba-examples` module is included in our project for you to get started with Spring Cloud Alibaba quickly. It contains an example, and you can refer to the readme file in the example project for a quick walkthrough.

Examples：

[Sentinel Example](https://github.com/alibaba/spring-cloud-alibaba/tree/2022.x/spring-cloud-alibaba-examples/sentinel-example/sentinel-core-example/readme.md)

[Nacos Config Example](https://github.com/alibaba/spring-cloud-alibaba/blob/2022.x/spring-cloud-alibaba-examples/nacos-example/readme.md#spring-cloud-alibaba-nacos-config)

[Nacos Discovery Example](https://github.com/alibaba/spring-cloud-alibaba/blob/2022.x/spring-cloud-alibaba-examples/nacos-example/readme.md#spring-cloud-alibaba-nacos-discovery)

[RocketMQ Example](https://github.com/alibaba/spring-cloud-alibaba/blob/2022.x/spring-cloud-alibaba-examples/rocketmq-example/readme.md)

[Alibaba Cloud OSS Example](https://github.com/alibaba/aliyun-spring-boot/tree/master/aliyun-spring-boot-samples/aliyun-oss-spring-boot-sample)

## Version control guidelines
The version number of the project is in the form of x.x.x, where x is a number, starting from 0, and is not limited to the range 0~9. When the project is in the incubator phase, the version number is 0.x.x.

As the interfaces and annotations of Spring Boot 1 and Spring Boot 2 have been changed significantly in the Actuator module, and spring-cloud-commons is also changed quite a lot from 1.x.x to 2.0.0, we take the same version rule as SpringBoot version number.

* 1.5.x for Spring Boot 1.5.x
* 2.0.x for Spring Boot 2.0.x
* 2.1.x for Spring Boot 2.1.x
* 2.2.x for Spring Boot 2.2.x
* 2020.x for Spring Boot 2.4.x
* 2021.x for Spring Boot 2.6.x
* 2022.x for Spring Boot 3.0.x

## Code of Conduct
This project is a sub-project of Spring Cloud, it adheres to the Contributor Covenant [code of conduct](https://github.com/spring-cloud/spring-cloud-build/blob/master/docs/src/main/asciidoc/code-of-conduct.adoc). By participating, you are expected to uphold this code. Please report unacceptable behavior to spring-code-of-conduct@pivotal.io.

## Code Conventions and Housekeeping
None of these is essential for a pull request, but they will all help. They can also be added after the original pull request but before a merge.

Use the Spring Framework code format conventions. If you use Eclipse you can import formatter settings using the eclipse-code-formatter.xml file from the Spring Cloud Build project. If using IntelliJ, you can use the Eclipse Code Formatter Plugin to import the same file.

Make sure all new .java files to have a simple Javadoc class comment with at least an @author tag identifying you, and preferably at least a paragraph on what the class is for.

Add the ASF license header comment to all new .java files (copy from existing files in the project)

Add yourself as an @author to the .java files that you modify substantially (more than cosmetic changes).

Add some Javadocs and, if you change the namespace, some XSD doc elements.

A few unit tests would help a lot as well —— someone has to do it.

If no-one else is using your branch, please rebase it against the current 2022.x (or other target branch in the main project).

When writing a commit message please follow these conventions, if you are fixing an existing issue please add Fixes gh-XXXX at the end of the commit message (where XXXX is the issue number).

## Contact Us
Mailing list is recommended for discussing almost anything related to spring-cloud-alibaba. 

spring-cloud-alibaba@googlegroups.com: You can ask questions here if you encounter any problem when using or developing spring-cloud-alibaba.
