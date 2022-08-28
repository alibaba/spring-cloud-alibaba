# Spring Cloud Alibaba容器化部署最佳实践 | docker-compose容器版本

## 准备工作

在运行docker-compose版本的demo时，请确保您的机器上已经具备如下的基础环境
- docker
- docker-compose

**注意**:请为docker分配足够的内存空间，以防出现容器启动失败等其他意外情况

## 快速启动

- 拉取最新的spring-cloud-alibaba git代码
- 进入`spring-cloud-alibaba-examples/integrated-example`目录下，将如下的各个模块进行maven package打包
  - `integrated-storage`
  - `integrated-account`
  - `integrated-order`
  - `integrated-praise-provider`
  - `integrated-praise-consumer`
  - `integrated-gateway`
  - `integrated-business`
- 回到`integrated-example`根目录中，输入`docker-compose up -d`一键启动所有容器

