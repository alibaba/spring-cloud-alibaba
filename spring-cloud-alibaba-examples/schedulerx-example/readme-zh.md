# SchedulerX Example

## 项目说明

[SchedulerX](https://www.aliyun.com/aliware/schedulerx) 是阿里巴巴自研的基于Akka架构的分布式任务调度平台（兼容开源XXL-JOB/ElasticJob），支持Cron定时、一次性任务、任务编排、分布式，具有高可用、可视化、低延时等能力。


## 示例

### 如何接入

1. 登录阿里云分布式任务调度[Schedulerx控制台](https://schedulerx2.console.aliyun.com)，点击开通服务
	
2. pom增加依赖spring-cloud-starter-alibaba-schedulerx
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-schedulerx</artifactId>
</dependency>
```

3. 配置application.yml
```yaml
spring:
   schedulerx2:
      endpoint: acm.aliyun.com   #请填写不同regin的endpoint
      namespace: 433d8b23-06e9-xxxx-xxxx-90d4d1b9a4af #region内全局唯一，建议使用UUID生成
      namespaceName: 学仁测试
      appName: myTest
      groupId: myTest.group      #同一个命名空间下需要唯一
      appKey: myTest123@alibaba  #应用的key，不要太简单，注意保管好
      regionId: public           #填写对应的regionId
      aliyunAccessKey: xxxxxxx   #阿里云账号的ak
      aliyunSecretKey: xxxxxxx   #阿里云账号的sk
      alarmChannel: sms,ding     #报警通道：短信和钉钉
      jobs: 
         simpleJob: 
            jobModel: standalone
            className: com.aliyun.schedulerx.example.processor.SimpleJob
            cron: 0/30 * * * * ?   # cron表达式
            jobParameter: hello
            overwrite: true 
         shardingJob: 
            jobModel: sharding
            className: ccom.aliyun.schedulerx.example.processor.ShardingJob
            oneTime: 2022-06-02 12:00:00   # 一次性任务表达式
            jobParameter: 0=Beijing,1=Shanghai,2=Guangzhou
            overwrite: true
         broadcastJob:   # 不填写cron和oneTime，表示api任务
            jobModel: broadcast
            className: com.aliyun.schedulerx.example.processor.BroadcastJob
            jobParameter: hello
            overwrite: true
         mapReduceJob: 
            jobModel: mapreduce
            className: com.aliyun.schedulerx.example.processor.MapReduceJob
            cron: 0 * * * * ?
            jobParameter: 100
            overwrite: true
      alarmUsers:     #报警联系人
         user1:
            userName: 张三
            userPhone: 12345678900
         user2:
            userName: 李四
            ding: https://oapi.dingtalk.com/robot/send?access_token=xxxxx
```
	
4. 实现任务接口，以单机任务为例，更多任务模型请看examples
```java
package com.alibaba.cloud.examples.schedulerx.job;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.JavaProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class SimpleJob extends JavaProcessor {

	private static final Logger logger = LoggerFactory.getLogger("schedulerx");

	@Override
	public ProcessResult process(JobContext context) throws Exception {
		System.out.println("this is process, para=" + context.getJobParameters());
		logger.info("hello schedulerx!");
		return new ProcessResult(true);
	}

	@Override
	public void kill(JobContext context) {
	}
}
```	  