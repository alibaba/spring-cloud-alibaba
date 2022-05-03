# 如何运行

1. 运行 [OpenSergo Dashboard](https://github.com/opensergo/opensergo-dashboard/blob/develop/README.zh-Hans.md)。

2. 编译 ```opensergo/opensergo-runner-example``` 子项目
```
    cd opensergo/opensergo-runner-example
    mvn clean compile package -DskipTests
```
3.在本地数据库```test```数据库执行```schema.sql```

4.运行项目，然后可以在Opensergo看到对应的应用实例
```
java -jar ./target/opensergo-runner-example.jar
```