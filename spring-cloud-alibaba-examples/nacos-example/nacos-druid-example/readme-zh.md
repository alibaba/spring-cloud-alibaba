# Nacos Proxy Druid动态数据源

## 项目说明

本项目介绍如何使用 Spring Cloud Alibaba Nacos Config 代理Druid数据源，实现应用的数据源配置运行时无损轮转 。

[Nacos](https://github.com/alibaba/Nacos) 是阿里巴巴开源的一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。

## 购买 MSE Nacos企业版

商业化MSE Nacos数据源管理功能，集合KMS实现了对应用访问数据库所需的参数加密托管，包括账号密码，数据库连接池参数等，同时应用侧结合druid实现应用运行时数据源的无损轮转。

### 

参照 https://help.aliyun.com/zh/rds/apsaradb-rds-for-mysql/spring-application-rds-data-source-account-password-run-time-rotation 完成数据源创建。

## 应用侧接入示例

### Spring Cloud Alibaba Nacos Config

nacos-druid-example已经完成了基础的访问数据库的接口实例创建以及在application.properties的参数配置。

通过一下脚本创建工程代码访问的数据库表

```
初始化表
CREATE TABLE `demo_entity` (
`id` bigint NOT NULL AUTO_INCREMENT,
`name` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
`content` longtext,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=199492 DEFAULT CHARSET=utf8mb3

初始化数据
INSERT INTO `demo_entity` (`id`,`name`,`content`) VALUES (123,'testname',null);

```

在MSE Nacos企业版中创建好数据源之后，可以参照应用侧接入中的指引，对application.properties中的配置进行替换即可。

```
# import datasource config to init druid datasource.
spring.config.import[0]=optional:nacos:{替换为数据源配置的DataId}?group=nacos-datasource

# nacos server address, replace it with your own
spring.cloud.nacos.config.server-addr={替换为nacos企业版接入地址，保证本地可正常链接9848端口}
# nacos namespace, replace it with your own
spring.cloud.nacos.config.namespace={替换为nacos企业版数据源所在命名空间ID}

# nacos kms regionId , replace it with your own
spring.cloud.nacos.config.kms_region_id={替换为nacos企业版实例关联KMS的RegionId}

# nacos proxy druid switch and data id
spring.nacos.config.proxy.druid.enabled=true
spring.nacos.config.proxy.druid.data-id={替换为数据源配置的DataId}

```

除以上配置外，需要通过环境变量或者-D JVM参数设置访问Nacos及KMS的凭证

```
-Dspring.cloud.nacos.config.access-key=LTA****************
-Dspring.cloud.nacos.config.secret-key=ot***************
```

按要求设置参数后，运行NacosSpringBootDemoApplication即可。
