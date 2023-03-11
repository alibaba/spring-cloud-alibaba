FROM openjdk:8-jdk-alpine as builder

LABEL author="yuluo" \
		email="yuluo829@aliyun.com" \
		version="2.2.10-RC1"

ADD ./integrated-praise-consumer/target/integrated-praise-consumer-2.2.10-RC1.jar /app.jar

RUN sh -c 'touch /app.jar'
EXPOSE 8014

ENTRYPOINT ["java", "-jar","/app.jar"]