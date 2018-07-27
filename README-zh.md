# Spring Cloud Alibaba

Spring Cloud Alibaba 致力于提供分布式应用服务开发的一站式解决方案。此项目包含开发分布式应用服务的必需组件，方便开发者通过 Spring Cloud 编程模型轻松使用这些组件来开发分布式应用服务。

依托 Spring Cloud Alibaba，您只需要添加一些注解和少量配置，就可以将 Spring Cloud 应用接入阿里分布式应用解决方案，通过阿里中间件来迅速搭建分布式应用系统。


## 主要功能

* **服务限流降级**：默认支持为 HTTP 服务的提供限流保护，也支持添加注解实现方法的自定义限流降级，且支持动态修改限流降级规则。

更多功能请参考 [Roadmap](https://github.com/spring-cloud-incubator/spring-cloud-alibabacloud/blob/master/Roadmap-zh.md)。

## 组件:

**[Sentinel](https://github.com/alibaba/Sentinel)**：把流量作为切入点，从流量控制、熔断降级、系统负载保护等多个维度保护服务的稳定性。

更多组件请参考 [Roadmap](https://github.com/spring-cloud-incubator/spring-cloud-alibabacloud/blob/master/Roadmap-zh.md)。

## 如何构建

* master 分支对应的是 Spring Boot 2.x，最低支持 JDK 1.8。
* support_spring_boot_1 分支对应的是 Spring Boot 1.x，最低支持 JDK 1.7。

Spring Cloud 使用 Maven 来构建，最快的使用方式是将本项目clone到本地，然后执行以下命令：

	./mvnw install

执行完毕后，项目将被安装到本地 Maven 仓库。

## 如何使用

为了演示如何使用，Spring Cloud Alibaba 项目包含了一个子模块`spring-cloud-alibaba-examples`。此模块中提供了演示用的 example ，您可以阅读对应的 example 工程下的 readme 文档，根据里面的步骤来体验。

Example 列表：

[sentinel example](https://github.com/spring-cloud-incubator/spring-cloud-alibabacloud/spring-cloud-alibaba-examples/sentinel-example/readme-zh.md)


## 版本管理规范
项目的版本号格式为 x.x.x 的形式，其中 x 的数值类型为数字，从0开始取值，且不限于 0~9 这个范围。项目处于孵化器阶段时，第一位版本号固定使用0，即版本号为 0.x.x 的格式。

由于 Spring Boot 1 和 Spring Boot 2 在 Actuator 模块的接口和注解有很大的变更，且 spring-cloud-commons 从 1.x.x 版本升级到 2.0.0 版本也有较大的变更，因此我们使用了两个不同分支来分别支持 Spring Boot 1 和 Spring Boot 2:
* 0.1.x 版本适用于 Spring Boot 1
* 0.2.x 版本适用于 Spring Boot 2

项目孵化阶段，项目版本升级机制如下：
* 功能改动的升级会增加第三位版本号的数值，例如 0.1.0 的下一个版本为0.1.1。
* 如果遇到阻碍主业务流程的 bug，需要进行少量修改进行紧急修复，会出现类似于 0.1.1.fix 这样的版本。我们会尽量完善测试回归流程，避免此类场景出现。



## 社区交流

### 邮件列表

spring-cloud-alibaba@googlegroups.com，欢迎通过此邮件列表讨论与 spring-cloud-alibaba 相关的一切。

### QQ 群

QQ群号 294650787