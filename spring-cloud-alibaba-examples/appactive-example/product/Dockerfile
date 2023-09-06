FROM openjdk:8-jdk-alpine

WORKDIR /app
COPY target/product-0.2.1.jar /app
# 设置文件夹操作权限
RUN chown -R root:root /app/* && \
    chmod a+rw -R  /app/*
#EXPOSE 8089
USER root:root

ARG UNITFLAG=default

ENV TZ=Asia/Shanghai \
   UNITFLAG=$UNITFLAG \
   LANG="en_US.UTF-8"

ENTRYPOINT ["sh", "-c"]
CMD ["java -jar /app/product-0.2.1.jar"]
