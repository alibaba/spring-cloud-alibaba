# SchedulerX Simple Task Example

## 项目说明

本项目展示了，在Spring Cloud体系中，如何快如接入SchedulerX，使用任务调度服务。

SchedulerX 是阿里中间件团队开发的一款分布式任务调度产品。它为您提供秒级、精准、高可靠、高可用的定时（基于 Cron 表达式）任务调度服务。同时提供分布式的任务执行模型，如网格任务。网格任务支持海量子任务均匀分配到所有 Worker（schedulerx-client）上执行。

## 示例

### 准备工作

1. 请先[注册阿里云账号](https://account.aliyun.com/register/register.htm?spm=5176.8142029.388261.26.e9396d3eEIv28g&oauth_callback=https%3A%2F%2Fwww.aliyun.com%2F)

2. SchedulerX集成到了EDAS组件中心，因此需要[开通EDAS服务](https://common-buy.aliyun.com/?spm=5176.11451019.0.0.6f5965c0Uq5tue&commodityCode=edaspostpay#/buy)

3. 到[EDAS组件中心](https://edas.console.aliyun.com/#/edasTools)开通SchedulerX组件，即分布式任务管理。

4. 进入[SchedulerX分组管理](https://edas.console.aliyun.com/#/schedulerXGroup?regionNo=cn-test)页面，选择上方"测试"区域，点击右上角"新建分组"，创建一个分组。
 
5. 将"分组ID"的值填写到`application.properties`文件中`key`为`spring.cloud.alicloud.scx.group-id`对应的value值，即如下所示。

        spring.cloud.alicloud.scx.group-id=111-1-1-1111
        
6. 进入[SchedulerX任务列表](https://edas.console.aliyun.com/#/edasSchedulerXJob?regionNo=cn-test)页面，选择上方"测试"区域，点击右上角"新建Job"，创建一个Job，即如下所示。

        Job分组：测试——111-1-1-1111
        Job处理接口：org.springframework.cloud.alibaba.cloud.examples.SimpleTask
        类型：简单Job单机版
        定时表达式：默认选项——0 * * * * ?
        Job描述：无
        自定义参数：无
        
### 启动应用

直接运行main class，即`ScxApplication`。

### 查看效果

观察应用的控制台日志输出，可以看到每一分钟会打印一次如下日志。

```
    -----------Hello world---------------
```

如果您对 Spring Cloud SchedulerX Starter 有任何建议或想法，欢迎提交 issue 中或者通过其他社区渠道向我们反馈。

