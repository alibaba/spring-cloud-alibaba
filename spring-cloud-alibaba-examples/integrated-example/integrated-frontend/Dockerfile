FROM openjdk:8-jdk-alpine as builder

LABEL author="yuluo" \
	email="yuluo829@aliyun.com"

ADD ./integrated-frontend/target/integrated-frontend-*.jar /app.jar

RUN sh -c 'touch /app.jar'

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
