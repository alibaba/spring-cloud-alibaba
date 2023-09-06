FROM openjdk:8-jdk-alpine as builder

LABEL author="yuluo" \
	email="yuluo829@aliyun.com"

ADD ./integrated-praise-provider/target/integrated-praise-provider-*.jar /app.jar

RUN sh -c 'touch /app.jar'
EXPOSE 8015

ENTRYPOINT ["java", "-jar","/app.jar"]