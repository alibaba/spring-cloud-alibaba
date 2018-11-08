# ANS Provider Example

## 项目说明

本项目展示了，在Spring Cloud规范下，如何以最简单且免费的方式，发布一个服务。

## 示例

### 准备工作

ANS支持直接使用免费的轻量版配置中心，进行开发和调试工作。本示例也是基于轻量版配置中心的，因此我们需要首先安装和启动轻量版配置中心。

1. [下载轻量版配置中心](https://edas-public.oss-cn-hangzhou.aliyuncs.com/install_package/LCC/2018-11-01/edas-lite-configcenter.tar.gz?file=edas-lite-configcenter.tar.gz)

2. 解压 edas-lite-configcenter.tar.gz ，然后执行以下命令。

        cd edas-config-center && sh startup.sh

出现以下内容说明轻量版配置中心启动成功。

        Edas-config-center has been started successfully.
        You can see more details in logs/config-center.log.
 
        
### 启动应用

直接运行main class，即`ProviderApplication`。

### 查看效果

进入页面 http://127.0.0.1:8080，在左侧"服务列表"页面中，可以看到一条名为`ans-provider`的服务。


如果您对 Spring Cloud ANS Starter 有任何建议或想法，欢迎提交 issue 中或者通过其他社区渠道向我们反馈。

