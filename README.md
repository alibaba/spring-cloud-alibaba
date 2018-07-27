# Spring Cloud Alibaba

See the [中文文档](https://github.com/spring-cloud-incubator/spring-cloud-alibabacloud/blob/master/README-zh.md) for Chinese readme.

Spring Cloud Alibaba provides a one-stop solution for distributed application development. It contains all the components required to develop distributed applications, making it easy for you to develop your applications using Spring Cloud.

With Spring Cloud Alibaba，you only need to add some annotations and a small amount of configurations to connect Spring Cloud applications to the distributed solutions of Alibaba, and build a distributed application system with Alibaba middleware.


## Features

* **Flow control and service degradation**：Flow control for HTTP services is supported by default. You can also customize flow control and service degradation rules using annotations. The rules can be changed dynamically.

For more features, please refer to [Roadmap](https://github.com/spring-cloud-incubator/spring-cloud-alibabacloud/blob/master/Roadmap.md).

## Components

**[Sentinel](https://github.com/alibaba/Sentinel)**: Sentinel takes "traffic flow" as the breakthrough point, and provides solutions in areas such as flow control, concurrency, circuit breaking, and load protection to protect service stability.

For more features please refer to [Roadmap](https://github.com/spring-cloud-incubator/spring-cloud-alibabacloud/blob/master/Roadmap.md).

## How to build

* **master branch**: Corresponds to Spring Boot 2.x. JDK 1.8 or later versions are supported.
* **support_spring_boot_1 branch**: Corresponds to Spring Boot 1.x，JDK 1.7 or later versions are supported.

Spring Cloud uses Maven for most build-related activities, and you should be able to get off the ground quite quickly by cloning the project you are interested in and typing:

	./mvnw install


## How to use

A `spring-cloud-alibaba-examples` module is included in our project for you to get started with Spring Cloud Alibaba quickly. It contains an example, and you can refer to the readme file in the example project for a quick walkthrough.

Examples：

[Sentinel example](https://github.com/spring-cloud-incubator/spring-cloud-alibabacloud/blob/master/spring-cloud-alibaba-examples/sentinel-example/readme.md)


## Version control guidelines
The version number of the project is in the form of x.x.x, where x is a number, starting from 0, and is not limited to the range 0~9. When the project is in the incubator phase, the first version number is fixed to 0, that is, the version number is 0.x.x.

As the interfaces and annotations of Spring Boot 1 and Spring Boot 2 have been changed significantly in the Actuator module, and spring-cloud-commons is also changed quite a lot from 1.x.x to 2.0.0, we maintain two different branches to support Spring Boot 1 and Spring Boot 2:
* 0.1.x for Spring Boot 1
* 0.2.x for Spring Boot 2

During the incubation period，the version management of the project will follow these rules：
* Functional updates will be reflected in the 3rd number of the version, for example, the next version of 0.1.0 will be 0.1.1 .
* Emergency fixes for bugs that block the main business process will be reflected in version numbers such as 0.1.1.fix. However, it is our duty to improve the test regression process to avoid such scenarios.



## Contact Us
Mailing list is recommended for discussing almost anything related to spring-cloud-alibaba. 

spring-cloud-alibaba@googlegroups.com:You can ask questions here if you encounter any problem when using or developing spring-cloud-alibaba.