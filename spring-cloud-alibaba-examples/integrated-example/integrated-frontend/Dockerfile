FROM openjdk:17-jdk-alpine AS builder

LABEL author="yuluo" \
	email="yuluo829@aliyun.com"

ADD ./integrated-frontend/target/integrated-frontend-*.jar /app.jar

RUN sh -c 'touch /app.jar'

EXPOSE 8010

ENTRYPOINT ["java", "-jar", "/app.jar"]
