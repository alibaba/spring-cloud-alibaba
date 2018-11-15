# ACM Local Example

## 项目说明

本项目展示了，在Spring Cloud规范下，如何以最简单且免费的方式，使用ACM产品，将配置统一管理。

应用配置管理（Application Configuration Management，简称 ACM），其前身为淘宝内部配置中心 Diamond，是一款应用配置中心产品。基于该应用配置中心产品，您可以在微服务、DevOps、大数据等场景下极大地减轻配置管理的工作量的同时，保证配置的安全合规。更多 ACM 相关的信息，请参考 [ACM官网](https://www.aliyun.com/product/acm)。

## 示例

### 准备工作

ACM支持直接使用免费的轻量版配置中心，进行开发和调试工作。本示例也是基于轻量版配置中心的，因此我们需要首先安装和启动轻量版配置中心。

1. [下载轻量版配置中心](https://edas-public.oss-cn-hangzhou.aliyuncs.com/install_package/LCC/2018-11-01/edas-lite-configcenter.tar.gz?file=edas-lite-configcenter.tar.gz)

2. 解压 edas-lite-configcenter.tar.gz ，然后执行以下命令。

        cd edas-config-center && sh startup.sh
        
出现以下内容说明轻量版配置中心启动成功。

        Edas-config-center has been started successfully.
        You can see more details in logs/config-center.log.
 
3. 进入页面 http://127.0.0.1:8080，在左侧"配置列表"页面中，点击"添加"按钮，添加如下配置。

        Group：DEFAULT_GROUP
        DataId：acm-local.properties
        Content：user.id=xiaolongzuo
        
### 启动应用

直接运行main class，即`AcmApplication`。

### 查看效果

1. 使用`curl`可以看到在配置中心配置的user.id。

        curl http://127.0.0.1:18089/
        
2. 进入页面 http://127.0.0.1:8080，在左侧"配置列表"页面中，更改user.id的值以后，再次使用`curl`命令，可以看到配置变化。


如果您对 Spring Cloud ACM Starter 有任何建议或想法，欢迎提交 issue 中或者通过其他社区渠道向我们反馈。

