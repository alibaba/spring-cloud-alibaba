FROM openjdk:8-jdk-alpine as builder

ARG SERVICE_VERSION

LABEL author="yuluo" \
	email="yuluo829@aliyun.com" \
	version=$SERVICE_VERSION

ADD ./integrated-storage/target/integrated-storage-*.jar /app.jar

RUN sh -c 'touch /app.jar'
EXPOSE 8011

ENTRYPOINT ["java", "-jar","/app.jar"]